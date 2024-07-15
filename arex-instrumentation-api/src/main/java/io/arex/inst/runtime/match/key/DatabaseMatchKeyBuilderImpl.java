package io.arex.inst.runtime.match.key;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.DatabaseUtils;

import java.util.*;

public class DatabaseMatchKeyBuilderImpl implements MatchKeyBuilder {

    private static final char SQL_BATCH_TERMINAL_CHAR = ';';
    private static final int INDEX_NOT_FOUND = -1;
    private static final int UPPER_LOWER_CASE_DELTA_VALUE = 32;
    /**
     * table name for ms-sql-server and mysql, which valid format as follow: ms-sql-server example:
     * 1,dbo.[tableName] 2,[orderDb].dbo.[tableName] 3,tableName mysql example:
     * 1,`orderDb`.`tableName' 2,`tableName' 3, tableName
     * <p>
     * table name for inner join as short,as follow: SELECT * FROM db.`tableNameA` a, tableNameB` b
     * WHERE a.id = b.id
     * <p>
     * for example: SELECT * FROM tableNameA a INNER JOIN `tableNameB` b ON a.id = b.id; SELECT * FROM
     * tableNameA a LEFT JOIN db.tableNameB b ON a.id = b.id; SELECT * FROM tableNameA a RIGHT JOIN
     * tableNameB b ON a.id = b.id;
     */
    private static final List<String> SQL_TABLE_KEYS = Arrays.asList("from", "join", "update", "into");

    @Override
    public boolean isSupported(MockCategoryType categoryType) {
        return MockCategoryType.DATABASE.getName().equals(categoryType.getName());
    }

    /**
     * category + dbName + tableName + operationName
     */
    @Override
    public int getFuzzyMatchKey(Mocker mocker) {
        String operationName = mocker.getOperationName();
        Mocker.Target request = mocker.getTargetRequest();
        String dbName = DatabaseUtils.parseDbName(operationName, request.attributeAsString(ArexConstants.DB_NAME));
        return StringUtil.encodeAndHash(
                mocker.getCategoryType().getName(),
                dbName,
                getTableName(operationName, request.getBody()),
                operationName);
    }


    /**
     * sql + parameters
     */
    @Override
    public int getAccurateMatchKey(Mocker mocker) {
        Mocker.Target request = mocker.getTargetRequest();
        String sql = request.getBody();
        String parameters = request.attributeAsString(ArexConstants.DB_PARAMETERS);
        return StringUtil.encodeAndHash(
                sql,
                parameters);
    }

    /**
     * sql + parameters
     */
    @Override
    public String getEigenBody(Mocker mocker) {
        String parameters = mocker.getTargetRequest().attributeAsString(ArexConstants.DB_PARAMETERS);
        Map<String, String> objectNode = new HashMap<>();
        objectNode.put(ArexConstants.DB_SQL, mocker.getTargetRequest().getBody());
        if (StringUtil.isNotEmpty(parameters)) {
            objectNode.put(ArexConstants.DB_PARAMETERS, parameters);
        }
        return Serializer.serialize(objectNode);
    }

    private String getTableName(String operationName, String sqlText) {
        List<String> tableNames = DatabaseUtils.parseTableNames(operationName);
        if (CollectionUtil.isNotEmpty(tableNames)) {
            return String.join(",", tableNames);
        }
        return findTableName(sqlText);
    }

    private String findTableName(String sqlText) {
        int sourceCount = sqlText.length();
        if (sourceCount > ArexConstants.DB_SQL_MAX_LEN) {
            return StringUtil.EMPTY;
        }
        List<String> tableNames = new ArrayList<>();
        for (int i = 0; i < SQL_TABLE_KEYS.size(); i++) {
            String key = SQL_TABLE_KEYS.get(i);
            int targetCount = key.length();
            int fromIndex = 0;
            int index = findIndexWholeIgnoreCase(sqlText, sourceCount, key, targetCount, fromIndex);
            while (index != INDEX_NOT_FOUND) {
                fromIndex = index + targetCount;
                int skipWhitespaceCount = skipWhitespace(sqlText, fromIndex, sourceCount);
                fromIndex += skipWhitespaceCount;
                String tableName = readTableValue(sqlText, fromIndex, sourceCount);
                int tableNameLength = tableName.length();
                fromIndex += tableNameLength;
                tableNames.add(tableName);
                index = findIndexWholeIgnoreCase(sqlText, sourceCount, key, targetCount, fromIndex);
            }
        }
        return String.join(",", tableNames);
    }

    private static String readTableValue(String sqlText, int readFromIndex, int sourceCount) {
        final int valueBeginIndex = readFromIndex;
        for (; readFromIndex < sourceCount; readFromIndex++) {
            if (readShouldTerminal(sqlText.charAt(readFromIndex))) {
                break;
            }
        }
        return sqlText.substring(valueBeginIndex, readFromIndex);
    }

    private static int findIndexWholeIgnoreCase(String source, int sourceCount, String target,
                                                int targetCount,
                                                int fromIndex) {
        if (fromIndex >= sourceCount) {
            return INDEX_NOT_FOUND;
        }
        char first = target.charAt(0);
        int max = sourceCount - targetCount;
        for (int i = fromIndex; i <= max; i++) {
            if (firstCharacterWordBoundaryNotMatch(source, first, i)) {
                while (++i <= max && firstCharacterWordBoundaryNotMatch(source, first, i)) {}
            }
            //  Found first character, now look at the rest of target
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = 1; j < end && equalsIgnoreCase(source.charAt(j), target.charAt(k)); j++, k++) {}
                if (j == end && isWordBoundary(source, j)) {
                    // Found whole string
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }

    private static boolean readShouldTerminal(char src) {
        return src == SQL_BATCH_TERMINAL_CHAR || isWhitespace(src);
    }

    private static int skipWhitespace(String sqlText, int fromIndex, int sourceCount) {
        int skipWhitespaceCount = 0;
        for (; fromIndex < sourceCount && isWhitespace(sqlText.charAt(fromIndex));
             fromIndex++, skipWhitespaceCount++) {}
        return skipWhitespaceCount;
    }

    private static boolean isWhitespace(char src) {
        return Character.isWhitespace(src);
    }

    private static boolean firstCharacterWordBoundaryNotMatch(final String source, char first, int position) {
        return !(equalsIgnoreCase(source.charAt(position), first) && isWordBoundary(source,
                position - 1));
    }

    private static boolean isWordBoundary(final String source, int positionOfPrevOrNext) {
        if (positionOfPrevOrNext < 0 || positionOfPrevOrNext >= source.length()) {
            return true;
        }
        return isWhitespace(source.charAt(positionOfPrevOrNext));
    }

    private static boolean equalsIgnoreCase(char src, char target) {
        return src == target || Math.abs(src - target) == UPPER_LOWER_CASE_DELTA_VALUE;
    }
}

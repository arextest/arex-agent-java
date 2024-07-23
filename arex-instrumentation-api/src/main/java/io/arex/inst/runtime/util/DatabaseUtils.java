package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.thirdparty.util.sqlparser.JSqlParserUtil;
import io.arex.agent.thirdparty.util.sqlparser.TableSchema;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.model.ArexConstants;

import java.util.*;

public class DatabaseUtils {

    private static final String DELIMITER = "@";

    public static String parseDbName(String operationName, String dbName) {
        if (StringUtil.isNotEmpty(dbName)) {
            return dbName;
        }

        if (StringUtil.isEmpty(operationName)) {
            return operationName;
        }
        int index = operationName.indexOf('@');
        if (index == -1) {
            return operationName;
        }
        return operationName.substring(0, index);
    }

    /**
     * @param operationName eg: db1@table1,table2@select@operation1;db2@table3,table4@select@operation2;
     * @return tableNames eg: ["table1,table2", "table3,table4"]
     */
    public static List<String> parseTableNames(String operationName) {
        if (StringUtil.isEmpty(operationName)) {
            return Collections.emptyList();
        }
        int countMatches = StringUtil.countMatches(operationName, "@");
        if (countMatches < 2) {
            return Collections.emptyList();
        }

        String[] operations = StringUtil.split(operationName, ';');
        List<String> tableList = new ArrayList<>(operations.length);
        for (String operation : operations) {
            String[] subOperation = StringUtil.split(operation, '@', true);
            if (subOperation.length < 2 || StringUtil.isEmpty(subOperation[1])) {
                continue;
            }
            tableList.add(subOperation[1]);
        }
        return tableList;
    }

    /**
     * format: dbName@tableNames@action@operationName
     * eg: db1@table1,table2@select@operation1;db2@table3,table4@select@operation2;
     */
    public static String regenerateOperationName(String dbName, String operationName, String sqlText) {
        if (StringUtil.isEmpty(sqlText) || operationName.contains(DELIMITER) || disableSqlParse()) {
            return operationName;
        }

        String[] sqlArray = StringUtil.split(sqlText, ';');
        List<String> operationNames = new ArrayList<>(sqlArray.length);
        for (String sql : sqlArray) {
            if (StringUtil.isEmpty(sql) || sql.length() > ArexConstants.DB_SQL_MAX_LEN
                    || StringUtil.startWith(sql.toLowerCase(), "exec sp")) {
                // if exceed the threshold, too large may be due parse stack overflow
                continue;
            }
            try{
                TableSchema tableSchema = JSqlParserUtil.parse(sql);
                tableSchema.setDbName(dbName);
                operationNames.add(regenerateOperationName(tableSchema, operationName));
            } catch (Throwable ignore) {
                // ignore error
            }
        }
        if (CollectionUtil.isEmpty(operationNames)) {
            return operationName;
        }
        // ensure that the order of multiple SQL statements is the same
        operationNames.sort(String::compareTo);
        return StringUtil.join(operationNames, ";");
    }

    private static String regenerateOperationName(TableSchema tableSchema, String originOperationName) {
        return new StringBuilder(100).append(StringUtil.defaultString(tableSchema.getDbName())).append(DELIMITER)
                .append(StringUtil.defaultString(StringUtil.join(tableSchema.getTableNames(), ","))).append(DELIMITER)
                .append(StringUtil.defaultString(tableSchema.getAction())).append(DELIMITER)
                .append(originOperationName)
                .toString();
    }

    public static boolean disableSqlParse() {
        return Config.get().getBoolean(ArexConstants.DISABLE_SQL_PARSE, Boolean.getBoolean(ArexConstants.DISABLE_SQL_PARSE));
    }
}


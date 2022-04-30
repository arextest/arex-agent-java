package io.arex.cli.storage;

import io.arex.foundation.model.*;
import io.arex.foundation.util.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class H2SqlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2SqlParser.class);

    private static String diff_service_entrance_sql =
            "SELECT a.caseId, b.replayId, a.response as recordResponse, b.response as replayResponse " +
                    "FROM record_servlet_entrance a LEFT JOIN replay_servlet_entrance b " +
                    "ON a.caseId = b.caseId AND a.path = b.path AND a.request = b.request " +
                    "WHERE a.caseId = '%s' and b.replayId = '%s'";

    private static String diff_database_sql =
            "SELECT a.dbname as recordDbname, b.dbname as replayDbname, " +
                    "a.parameters as recordParameters, b.parameters as replayParameters, " +
                    "a.sql as recordSql, b.sql as replaySql " +
                    "FROM record_database a LEFT JOIN replay_database b " +
                    "ON a.caseId = b.caseId AND a.parameters = b.parameters AND a.sql = b.sql " +
                    "WHERE a.caseId = '%s' and b.replayId = '%s'";

    private static Map<String, String> schemaMap = new HashMap<>();

    public static Map<String, String> parseSchema() {
        try {
            String schemaSql = IOUtils.toString(
                    H2StorageService.class.getResourceAsStream("/db/h2/schema.txt"));
            String[] schemaArray = schemaSql.split("--");
            for (String schemas : schemaArray) {
                if (StringUtils.isBlank(schemas)) {
                    continue;
                }
                String[] sqlArray = schemas.split("\n");
                String tableName = "";
                StringBuilder schema = new StringBuilder();
                for (String sql : sqlArray) {
                    if (StringUtils.isBlank(sql)) {
                        continue;
                    }
                    if (sql.startsWith("CREATE TABLE")) {
                        tableName = StringUtils.substringBetween(sql, "EXISTS ", "(");
                    }
                    schema.append(sql);
                    if (sql.equals(");")) {
                        break;
                    } else {
                        schema.append("\n");
                    }
                }
                schemaMap.put(tableName, schema.toString());
            }
        } catch (Throwable e) {
            LOGGER.warn("h2database parseSchema error", e);
        }
        return schemaMap;
    }

    public static String generateInsertSql(List<Object> entitys, String tableName, String mockerInfo) {
        String schema = schemaMap.get(tableName);
        if (StringUtils.isBlank(schema)) {
            return null;
        }
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ").append(tableName).append(" VALUES");
        try {
            String[] sqlArray = schema.split("\n");
            String[] fieldArray = Arrays.copyOfRange(sqlArray, 2, sqlArray.length-1);
            String fieldName;
            for (Object entity : entitys) {
                sqlBuilder.append("(DEFAULT,");
                for (int i = 0; i < fieldArray.length; i++) {
                    fieldName = StringUtils.substringBefore(fieldArray[i], " ");
                    if (fieldName.equalsIgnoreCase("dataCreateTime")) {
                        sqlBuilder.append("CURRENT_TIMESTAMP");
                    } else if (fieldName.equalsIgnoreCase("mockerInfo")) {
                        sqlBuilder.append("'").append(mockerInfo == null ? "" : mockerInfo).append("'");
                    } else {
                        Object fieldVal = FieldUtils.readField(entity, fieldName, true);
                        sqlBuilder.append("'").append(fieldVal == null ? "" : fieldVal).append("'");
                    }
                    if (i < fieldArray.length-1) {
                        sqlBuilder.append(",");
                    }
                }
                sqlBuilder.append("),");
            }
        } catch (Throwable e) {
            LOGGER.warn("h2database generateInsertSql error", e);
        }
        return sqlBuilder.substring(0, sqlBuilder.length()-1);
    }

    public static String generateSelectSql(AbstractMocker mocker, MockDataType type, int count) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
        String tableName = type.name() + "_" + mocker.getCategory().name();
        switch (mocker.getCategory()) {
            case SERVLET_ENTRANCE:
                ServletMocker servletMocker = (ServletMocker)mocker;
                sqlBuilder.append(tableName).append(" WHERE 1 = 1");
                if (StringUtils.isNotBlank(servletMocker.getCaseId())) {
                    sqlBuilder.append(" AND CASEID = '").append(servletMocker.getCaseId()).append("'");
                }
                if (StringUtils.isNotBlank(servletMocker.getPath())) {
                    sqlBuilder.append(" AND PATH = '").append(servletMocker.getPath()).append("'");
                }
                if (StringUtils.isNotBlank(servletMocker.getRequest())) {
                    sqlBuilder.append(" AND REQUEST = '").append(servletMocker.getRequest()).append("'");
                }
                break;
            case DATABASE:
                DatabaseMocker databaseMocker = (DatabaseMocker)mocker;
                sqlBuilder.append(tableName).append(" WHERE 1 = 1");
                if (StringUtils.isNotBlank(databaseMocker.getCaseId())) {
                    sqlBuilder.append(" AND CASEID = '").append(databaseMocker.getCaseId()).append("'");
                }
                if (StringUtils.isNotBlank(databaseMocker.getParameters())) {
                    sqlBuilder.append(" AND PARAMETERS = '").append(databaseMocker.getParameters()).append("'");
                }
                if (StringUtils.isNotBlank(databaseMocker.getSql())) {
                    sqlBuilder.append(" AND SQL = '").append(databaseMocker.getSql()).append("'");
                }
                break;
        }
        sqlBuilder.append(" ORDER BY CREATETIME DESC");
        if (count > 0) {
            sqlBuilder.append(" LIMIT ").append(count);
        }
        return sqlBuilder.toString();
    }

    public static String generateCompareSql(MockerCategory category, String recordId, String replayId) {
        switch (category) {
            case SERVLET_ENTRANCE:
                return String.format(diff_service_entrance_sql, recordId, replayId);
            case DATABASE:
                return String.format(diff_database_sql, recordId,  replayId);
        }
        return null;
    }

    public static String generateSelectDiffSql(DiffMocker mocker) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
        String tableName = "DIFF_" + mocker.getCategory().name();
        sqlBuilder.append(tableName).append(" WHERE 1 = 1");
        if (StringUtils.isNotBlank(mocker.getCaseId())) {
            sqlBuilder.append(" AND CASEID = '").append(mocker.getCaseId()).append("'");
        }
        if (StringUtils.isNotBlank(mocker.getReplayId())) {
            sqlBuilder.append(" AND REPLAYID = '").append(mocker.getReplayId()).append("'");
        }
        return sqlBuilder.toString();
    }
}

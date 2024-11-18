package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.thirdparty.util.sqlparser.JSqlParserUtil;
import io.arex.agent.thirdparty.util.sqlparser.TableSchema;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;

import java.util.*;

public class DatabaseUtils {

    private static final String DELIMITER = "@";

    /**
     * format: dbName@tableNames@action@operationName
     * eg: db1@table1,table2@select@operation1;db2@table3,table4@select@operation2;
     */
    public static String regenerateOperationName(String dbName, String operationName, String sqlText) {
        if (disableSqlParse() || StringUtil.isEmpty(sqlText) || operationName.contains(DELIMITER)) {
            return operationName;
        }

        String[] sqlArray = StringUtil.split(sqlText, ';');
        List<String> operationNames = new ArrayList<>(sqlArray.length);
        for (String sql : sqlArray) {
            if (StringUtil.isEmpty(sql) || sql.length() > ArexConstants.DB_SQL_MAX_LEN
                    || StringUtil.startWith(sql.toLowerCase(), "exec sp")) {
                // if exceed the threshold, too large may be due parse stack overflow
                LogManager.warn("sql.parse.fail", "invalid sql, too large or sp");
                continue;
            }
            try{
                TableSchema tableSchema = JSqlParserUtil.parse(sql);
                tableSchema.setDbName(dbName);
                operationNames.add(regenerateOperationName(tableSchema, operationName));
            } catch (Throwable ignore) {
                // ignore parse failure
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


package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.thirdparty.util.sqlparser.JSqlParserUtil;
import io.arex.agent.thirdparty.util.sqlparser.TableSchema;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
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
        if (StringUtil.isEmpty(sqlText) || operationName.contains(DELIMITER) || disableSqlParse()) {
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
            } catch (Throwable e) {
                // may be thrown error
                LogManager.warn("parse sql error", StringUtil.format("sql: %s", sql), e);
            }
        }
        if (CollectionUtil.isEmpty(operationNames)) {
            return operationName;
        }
        // ensure that the order of multiple SQL statements is the same
        operationNames.sort(String::compareTo);
        return StringUtil.join(operationNames, ";");
    }

    /**
     * compatible with the old version, if the excludeMockTemplate config not contains '@', it means that not need generate
     */
    private static boolean needRegenerate(String dbName) {
        Map<String, Set<String>> excludeMockTemplate = ContextManager.currentContext().getExcludeMockTemplate();
        if (excludeMockTemplate == null) {
            return false;
        }
        Set<String> operationSet = excludeMockTemplate.get(dbName);
        if (CollectionUtil.isEmpty(operationSet)) {
            return false;
        }
        for (String operation : operationSet) {
            if (operation != null && operation.contains(DELIMITER)) {
                return true;
            }
        }
        return false;
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


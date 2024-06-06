package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.thirdparty.util.sqlparser.JSqlParserUtil;
import io.arex.agent.thirdparty.util.sqlparser.TableSchema;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;

import java.util.*;

public class DatabaseUtils {

    private static final String DELIMITER = "@";

    private static final int THRESHOLD = 50000;

    /**
     * eg: db1@table1,table2@select@operation1;db2@table3,table4@select@operation2;
     */
    public static String regenerateOperationName(String dbName, String operationName, String sqlText) {
        if (StringUtil.isEmpty(sqlText) || !needRegenerate(dbName)) {
            return operationName;
        }

        String[] sqls = StringUtil.split(sqlText, ';');
        List<String> operationNames = new ArrayList<>(sqls.length);
        for (String sql : sqls) {
            System.out.println(sql.length());
            if (StringUtil.isEmpty(sql) || sql.length() > THRESHOLD) {
                // if exceed the threshold, too large may be due parse stack overflow
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
}


package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.thirdparty.util.sqlparser.JSqlParserUtil;
import io.arex.agent.thirdparty.util.sqlparser.TableSchema;

import java.util.ArrayList;
import java.util.List;

public class DatabaseUtils {

    private static final String DELIMITER = "@";

    /**
     * eg: db1@table1,table2@select@operation1;db2@table3,table4@select@operation2;
     */
    public static String regenerateOperationName(String dbName, String operationName, String sqlText) {
        // If the operation name already contains @, it means that it has already been generated and does not need to be generated again
        if (StringUtil.isEmpty(sqlText) || StringUtil.isEmpty(operationName) || operationName.contains(DELIMITER)) {
            return operationName;
        }

        String[] sqls = StringUtil.split(sqlText, ';');
        List<String> operationNames = new ArrayList<>(sqls.length);
        for (String sql : sqls) {
            if (StringUtil.isEmpty(sql)) {
                continue;
            }

            TableSchema tableSchema = null;
            try{
                tableSchema = JSqlParserUtil.parse(sql);
            } catch (Throwable e) {
                // ignore
            }
            if (tableSchema == null) {
                continue;
            }
            tableSchema.setDbName(dbName);
            operationNames.add(regenerateOperationName(tableSchema, operationName));
        }
        return StringUtil.join(operationNames, ";");
    }

    private static String regenerateOperationName(TableSchema tableSchema, String originOperationName) {
        return new StringBuilder(100).append(StringUtil.defaultString(tableSchema.getDbName())).append(DELIMITER)
                .append(StringUtil.defaultString(StringUtil.join(tableSchema.getTableNames(), ","))).append(DELIMITER)
                .append(StringUtil.defaultString(tableSchema.getAction())).append(DELIMITER)
                .append(originOperationName)
                .toString();
    }
}


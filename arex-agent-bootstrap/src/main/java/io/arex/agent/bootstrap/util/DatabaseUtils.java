package io.arex.agent.bootstrap.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseUtils {
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
}

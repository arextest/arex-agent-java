package io.arex.agent.thirdparty.util.sqlparser;

import java.util.List;

public class TableSchema {
    String dbName;
    /**
     * Joint query sql statement parses out multiple table names
     */
    List<String> tableNames;
    /**
     * eg: query/insert/update/delete
     */
    String action;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public List<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}

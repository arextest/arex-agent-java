package io.arex.agent.thirdparty.util.sqlparser;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.*;
import java.util.regex.Pattern;

public class JSqlParserUtil {

    private static final Pattern PATTERN = Pattern.compile("(\\s+|\"\\?\"|\\[|\\])");

    /**
     * parse table and action from sql
     * @param sql sql
     * @return table schema info
     */
    public static TableSchema parse(String sql) throws Exception {
        sql = PATTERN.matcher(sql).replaceAll(" ");

        Statement statement = CCJSqlParserUtil.parse(sql);

        List<String> tableNameList = new TablesNamesFinder().getTableList(statement);
        // sort table name
        if (tableNameList != null && tableNameList.size() > 1) {
            Collections.sort(tableNameList);
        }

        TableSchema tableSchema = new TableSchema();
        tableSchema.setAction(getAction(statement));
        tableSchema.setTableNames(tableNameList);
        return tableSchema;
    }

    private static String getAction(Statement statement) {
        if (statement instanceof Select) {
            return "Select";
        }
        return statement.getClass().getSimpleName();
    }
}


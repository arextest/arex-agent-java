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
    public static TableSchema parse(String sql) {
        TableSchema tableSchema = new TableSchema();
        try {
            sql = PATTERN.matcher(sql).replaceAll(" ");

            Statement statement = CCJSqlParserUtil.parse(sql);
            tableSchema.setAction(getAction(statement));

            List<String> tableNameList = new TablesNamesFinder().getTableList(statement);
            // sort table name
            if (tableNameList != null && !tableNameList.isEmpty()) {
                Collections.sort(tableNameList);
            }
            tableSchema.setTableNames(tableNameList);
        } catch (Throwable e) {
            // ignore error
        }
        return tableSchema;
    }

    private static String getAction(Statement statement) {
        if (statement instanceof Select) {
            return "Select";
        }
        return statement.getClass().getSimpleName();
    }
}


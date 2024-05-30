package io.arex.agent.thirdparty.util.parse.sqlparse;

import com.fasterxml.jackson.databind.JsonNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.action.AbstractParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.action.ActionFactory;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * sql parse
 *
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class SqlParseManager {

    private static SqlParseManager INSTANCE;

    private SqlParseManager() {
    }

    public static SqlParseManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SqlParseManager();
        }
        return INSTANCE;
    }

    public JsonNode parse(String sql) throws JSQLParserException {
        if (sql == null || sql.isEmpty()) {
            return null;
        }

        Statement statement = CCJSqlParserUtil.parse(sql);
        AbstractParse parse = ActionFactory.selectParse(statement);
        return parse.parse(statement);
    }

    public Map<String, String> parseTableAndAction(String sql) throws JSQLParserException {
        if (sql == null || sql.isEmpty()) {
            return null;
        }

        Map<String, String> result = new HashMap<>(2);

        Statement statement = CCJSqlParserUtil.parse(sql);
        result.put(DbParseConstants.ACTION, getAction(statement));

        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableNames = tablesNamesFinder.getTableList(statement);
        if (tableNames != null && !tableNames.isEmpty()) {
            result.put(DbParseConstants.TABLE, String.join(",", tableNames));
        }
        return result;
    }

    private String getAction(Statement statement) {
        if (statement instanceof Select) {
            return DbParseConstants.SELECT;
        } else if (statement instanceof Execute) {
            return DbParseConstants.EXECUTE;
        } else if (statement instanceof Delete) {
            return DbParseConstants.DELETE;
        } else if (statement instanceof Insert) {
            return DbParseConstants.INSERT;
        } else if (statement instanceof Replace) {
            return DbParseConstants.REPLACE;
        } else if (statement instanceof Update) {
            return DbParseConstants.UPDATE;
        } else {
            return "";
        }
    }
}

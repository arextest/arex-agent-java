package io.arex.agent.thirdparty.util.parse.sqlparse;

import com.fasterxml.jackson.databind.JsonNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.action.ActionFactory;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

/**
 * sql parse
 *
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class SqlParse {
    public JsonNode parse(String sql) throws JSQLParserException {
        if (sql == null || sql.isEmpty()) {
            return null;
        }

        Statement statement = CCJSqlParserUtil.parse(sql);
        Parse matchParse = ActionFactory.selectParse(statement);
        return matchParse.parse(statement);
    }
}

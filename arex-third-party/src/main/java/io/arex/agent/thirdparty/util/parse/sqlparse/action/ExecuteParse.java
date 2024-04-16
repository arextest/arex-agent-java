package io.arex.agent.thirdparty.util.parse.sqlparse.action;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.CommonParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.ExpressionParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.statement.execute.Execute;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class ExecuteParse extends AbstractParse<Execute> {

    public ExecuteParse() {
        super(DbParseConstants.EXECUTE);
    }

    @Override
    public ObjectNode parse(Execute parseObj) {

        // execute name parse
        CommonParse.parseName(parseObj.getName(), sqlObjectNode);

        // expressions parse
        ExpressionParse.parse(parseObj.getExprList(), sqlObjectNode);

        return sqlObjectNode;
    }
}

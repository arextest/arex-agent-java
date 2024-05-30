package io.arex.agent.thirdparty.util.parse.sqlparse.action;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.CommonParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.ExpressionParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.JoinParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.OrderByParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.TableParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.statement.delete.Delete;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class DeleteParse extends AbstractParse<Delete> {

    public DeleteParse() {
        super(DbParseConstants.DELETE);
    }
    @Override
    public ObjectNode parse(Delete parseObj) {

        // tables parse
        TableParse.parseDelTable(parseObj.getTables(), parseObj.getTable(), sqlObjectNode);

        // join parse
        JoinParse.parse(parseObj.getJoins(), sqlObjectNode);

        // where parse
        ExpressionParse.parseWhere(parseObj.getWhere(), sqlObjectNode);

        // orderBy parse
        OrderByParse.parse(parseObj.getOrderByElements(), sqlObjectNode);

        // limit parse
        CommonParse.parseLimit(parseObj.getLimit(), sqlObjectNode);

        return sqlObjectNode;
    }
}

package io.arex.agent.thirdparty.util.parse.sqlparse.action;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.CommonParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.ExpressionParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.ItemParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.JoinParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.OrderByParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.TableParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.UpdateSetParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.statement.update.Update;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class UpdateParse extends AbstractParse<Update> {

    public UpdateParse() {
        super(DbParseConstants.UPDATE);
    }


    @Override
    public ObjectNode parse(Update parseObj) {
        // table parse
        TableParse.parse(parseObj.getTable(), sqlObjectNode);

        // startJoins parse
        JoinParse.startJoinsParse(parseObj.getStartJoins(), sqlObjectNode);

        // from parse
        ItemParse.parseFromItem(parseObj.getFromItem(), sqlObjectNode);

        // joins parse
        JoinParse.parse(parseObj.getJoins(), sqlObjectNode);

        // updateSet parse
        UpdateSetParse.parse(parseObj.getUpdateSets(), sqlObjectNode);

        // where parse
        ExpressionParse.parseWhere(parseObj.getWhere(), sqlObjectNode);

        // order parse
        OrderByParse.parse(parseObj.getOrderByElements(), sqlObjectNode);

        // limit parse
        CommonParse.parseLimit(parseObj.getLimit(), sqlObjectNode);

        return sqlObjectNode;
    }
}

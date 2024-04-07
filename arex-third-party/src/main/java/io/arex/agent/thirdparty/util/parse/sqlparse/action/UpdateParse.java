package io.arex.agent.thirdparty.util.parse.sqlparse.action;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.Parse;
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
public class UpdateParse implements Parse<Update> {

    private final ObjectNode updateObject;

    public UpdateParse() {
        updateObject = JacksonHelperUtil.getObjectNode();
        updateObject.put(DbParseConstants.ACTION, DbParseConstants.UPDATE);
    }


    @Override
    public ObjectNode parse(Update parseObj) {
        // table parse
        TableParse.parse(parseObj.getTable(), updateObject);

        // startJoins parse
        JoinParse.startJoinsParse(parseObj.getStartJoins(), updateObject);

        // from parse
        ItemParse.parseFromItem(parseObj.getFromItem(), updateObject);

        // joins parse
        JoinParse.parse(parseObj.getJoins(), updateObject);

        // updateSet parse
        UpdateSetParse.parse(parseObj.getUpdateSets(), updateObject);

        // where parse
        ExpressionParse.parseWhere(parseObj.getWhere(), updateObject);

        // order parse
        OrderByParse.parse(parseObj.getOrderByElements(), updateObject);

        // limit parse
        CommonParse.parseLimit(parseObj.getLimit(), updateObject);

        return updateObject;
    }
}

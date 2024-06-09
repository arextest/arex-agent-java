package io.arex.agent.compare.handler.parse.sqlparse.action;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;

import io.arex.agent.compare.handler.parse.sqlparse.Parse;
import io.arex.agent.compare.handler.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.compare.handler.parse.sqlparse.select.ArexExpressionVisitorAdapter;
import io.arex.agent.compare.handler.parse.sqlparse.select.ArexFromItemVisitorAdapter;
import io.arex.agent.compare.handler.parse.sqlparse.select.ArexOrderByVisitorAdapter;
import io.arex.agent.compare.handler.parse.sqlparse.select.utils.JoinParseUtil;
import io.arex.agent.compare.utils.JacksonHelperUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

/**
 * the example of parsed update sql: { "set": { "`url`": "?", },
 * "action": "update", "where": { "andOr": [], "columns": { "`id` = ?": 0 } }, "table": "`picture`"
 * }
 */
public class UpdateParse implements Parse<Update> {

    @Override
    public ObjectNode parse(Update parseObj) {
        ObjectNode sqlObject = JacksonHelperUtil.getObjectNode();
        sqlObject.put(DbParseConstants.ACTION, DbParseConstants.UPDATE);

        // table parse
        Table table = parseObj.getTable();
        if (table != null) {
            sqlObject.put(DbParseConstants.TABLE, table.getFullyQualifiedName());
        }

        // startJoins parse
        List<Join> startJoins = parseObj.getStartJoins();
        if (startJoins != null && !startJoins.isEmpty()) {
            ArrayNode joinArr = JacksonHelperUtil.getArrayNode();
            startJoins.forEach(item -> {
                joinArr.add(JoinParseUtil.parse(item));
            });
            sqlObject.set(DbParseConstants.START_JOINS, joinArr);
        }

        // from parse
        FromItem fromItem = parseObj.getFromItem();
        if (fromItem != null) {
            ObjectNode fromObj = JacksonHelperUtil.getObjectNode();
            ArexFromItemVisitorAdapter arexFromItemVisitorAdapter = new ArexFromItemVisitorAdapter(
                    fromObj);
            fromItem.accept(arexFromItemVisitorAdapter);
            sqlObject.set(DbParseConstants.FROM, fromObj);
        }

        // joins parse
        List<Join> joins = parseObj.getJoins();
        if (joins != null && !joins.isEmpty()) {
            ArrayNode joinArr = JacksonHelperUtil.getArrayNode();
            joins.forEach(item -> {
                joinArr.add(JoinParseUtil.parse(item));
            });
            sqlObject.set(DbParseConstants.JOIN, joinArr);
        }

        // updateSet parse
        List<UpdateSet> updateSets = parseObj.getUpdateSets();
        if (updateSets != null && !updateSets.isEmpty()) {
            ObjectNode setObj = JacksonHelperUtil.getObjectNode();
            for (UpdateSet updateSet : updateSets) {
                ArrayList<Column> columns = updateSet.getColumns();
                ArrayList<Expression> expressions = updateSet.getExpressions();
                setObj.put(columns.get(0).toString(), expressions.get(0).toString());
            }
            sqlObject.set(DbParseConstants.COLUMNS, setObj);
        }

        // where parse
        Expression where = parseObj.getWhere();
        if (where != null) {
            ObjectNode whereObj = JacksonHelperUtil.getObjectNode();
            whereObj.set(DbParseConstants.AND_OR, JacksonHelperUtil.getArrayNode());
            whereObj.set(DbParseConstants.COLUMNS, JacksonHelperUtil.getObjectNode());
            where.accept(new ArexExpressionVisitorAdapter(whereObj));
            sqlObject.set(DbParseConstants.WHERE, whereObj);
        }

        // order parse
        List<OrderByElement> orderByElements = parseObj.getOrderByElements();
        if (orderByElements != null && !orderByElements.isEmpty()) {
            ObjectNode orderByObj = JacksonHelperUtil.getObjectNode();
            ArexOrderByVisitorAdapter arexOrderByVisitorAdapter = new ArexOrderByVisitorAdapter(
                    orderByObj);
            orderByElements.forEach(item -> {
                item.accept(arexOrderByVisitorAdapter);
            });
            sqlObject.set(DbParseConstants.ORDER_BY, orderByObj);
        }

        // limit parse
        Limit limit = parseObj.getLimit();
        if (limit != null) {
            sqlObject.put(DbParseConstants.LIMIT, limit.toString());
        }
        return sqlObject;
    }
}

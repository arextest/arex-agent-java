package io.arex.agent.compare.handler.parse.sqlparse.action;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

import io.arex.agent.compare.handler.parse.sqlparse.Parse;
import io.arex.agent.compare.handler.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.compare.handler.parse.sqlparse.select.ArexExpressionVisitorAdapter;
import io.arex.agent.compare.handler.parse.sqlparse.select.ArexOrderByVisitorAdapter;
import io.arex.agent.compare.handler.parse.sqlparse.select.utils.JoinParseUtil;
import io.arex.agent.compare.utils.JacksonHelperUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;

/**
 * the example of parsed delete sql: { "action": "DELETE", "table":
 * "Exam", "where": { "andor": [ "and", "and" ], "columns": { "a.rnk <= 3": "", "a.per_id in (select
 * per_id from colle_subject)": "" } } }
 */
public class DeleteParse implements Parse<Delete> {

    @Override
    public ObjectNode parse(Delete parseObj) {
        ObjectNode sqlObject = JacksonHelperUtil.getObjectNode();
        sqlObject.put(DbParseConstants.ACTION, DbParseConstants.DELETE);

        // tables parse
        List<Table> tables = parseObj.getTables();
        if (tables != null && !tables.isEmpty()) {
            ObjectNode delTableObj = JacksonHelperUtil.getObjectNode();
            tables.forEach(item -> {
                delTableObj.put(item.getFullyQualifiedName(), DbParseConstants.EMPTY);
            });
            sqlObject.set(DbParseConstants.DEL_TABLES, delTableObj);
        }

        // table parse
        Table table = parseObj.getTable();
        if (table != null) {
            sqlObject.put(DbParseConstants.TABLE, table.getFullyQualifiedName());
        }

        // join parse
        List<Join> joins = parseObj.getJoins();
        if (joins != null && !joins.isEmpty()) {
            ArrayNode joinArr = JacksonHelperUtil.getArrayNode();
            joins.forEach(item -> {
                joinArr.add(JoinParseUtil.parse(item));
            });
            sqlObject.set(DbParseConstants.JOIN, joinArr);
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

        // orderby parse
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

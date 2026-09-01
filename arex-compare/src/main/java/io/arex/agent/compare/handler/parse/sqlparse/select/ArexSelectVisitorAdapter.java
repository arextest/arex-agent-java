package io.arex.agent.compare.handler.parse.sqlparse.select;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

import io.arex.agent.compare.handler.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.compare.handler.parse.sqlparse.select.utils.JoinParseUtil;
import io.arex.agent.compare.utils.JacksonHelperUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.First;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OptimizeFor;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.Skip;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.Wait;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;

public class ArexSelectVisitorAdapter implements SelectVisitor {

    private ObjectNode sqlObj;

    public ArexSelectVisitorAdapter(ObjectNode object) {
        sqlObj = object;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        // distinct parse
        Distinct distinct = plainSelect.getDistinct();
        if (distinct != null) {
            sqlObj.put(DbParseConstants.DISTINCT, distinct.toString());
        }

        // skip parse
        Skip skip = plainSelect.getSkip();
        if (skip != null) {
            sqlObj.put(DbParseConstants.SKIP, skip.toString());
        }

        // top parse
        Top top = plainSelect.getTop();
        if (top != null) {
            sqlObj.put(DbParseConstants.TOP, top.toString());
        }

        // first parse
        First first = plainSelect.getFirst();
        if (first != null) {
            sqlObj.put(DbParseConstants.FIRST, first.toString());
        }

        // selectItems parse
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        if (selectItems != null && !selectItems.isEmpty()) {
            ObjectNode columnsObj = JacksonHelperUtil.getObjectNode();
            ArexSelectItemVisitorAdapter arexSelectItemVisitorAdapter = new ArexSelectItemVisitorAdapter(
                    columnsObj);
            selectItems.forEach(selectItem -> {
                selectItem.accept(arexSelectItemVisitorAdapter);
            });
            sqlObj.set(DbParseConstants.COLUMNS, columnsObj);
        }

        // into parse
        List<Table> intoTables = plainSelect.getIntoTables();
        if (intoTables != null && !intoTables.isEmpty()) {
            sqlObj.put(DbParseConstants.INTO, intoTables.toString());
        }

        // fromItem parse
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem != null) {
            ObjectNode fromObj = JacksonHelperUtil.getObjectNode();
            ArexFromItemVisitorAdapter arexFromItemVisitorAdapter = new ArexFromItemVisitorAdapter(
                    fromObj);
            fromItem.accept(arexFromItemVisitorAdapter);
            sqlObj.set(DbParseConstants.FROM, fromObj);
        }

        // jonis parse
        List<Join> joins = plainSelect.getJoins();
        if (joins != null && !joins.isEmpty()) {
            ArrayNode joinArr = JacksonHelperUtil.getArrayNode();
            joins.forEach(item -> {
                joinArr.add(JoinParseUtil.parse(item));
            });
            sqlObj.put(DbParseConstants.JOIN, joinArr);
        }

        // where parse
        Expression where = plainSelect.getWhere();
        if (where != null) {
            // JSONObject whereObj = new JSONObject();
            ObjectNode whereObj = JacksonHelperUtil.getObjectNode();
            whereObj.set(DbParseConstants.AND_OR, JacksonHelperUtil.getArrayNode());
            whereObj.set(DbParseConstants.COLUMNS, JacksonHelperUtil.getObjectNode());
            ArexExpressionVisitorAdapter arexExpressionVisitorAdapter = new ArexExpressionVisitorAdapter(
                    whereObj);
            where.accept(arexExpressionVisitorAdapter);
            sqlObj.set(DbParseConstants.WHERE, whereObj);
        }

        // group by parse
        GroupByElement groupBy = plainSelect.getGroupBy();
        if (groupBy != null) {
            sqlObj.put(DbParseConstants.GROUP_BY, groupBy.toString());
        }

        // having parse
        Expression having = plainSelect.getHaving();
        if (having != null) {
            ObjectNode havingObj = JacksonHelperUtil.getObjectNode();
            havingObj.put(DbParseConstants.AND_OR, JacksonHelperUtil.getArrayNode());
            havingObj.put(DbParseConstants.COLUMNS, JacksonHelperUtil.getObjectNode());
            ArexExpressionVisitorAdapter arexExpressionVisitorAdapter = new ArexExpressionVisitorAdapter(
                    havingObj);
            having.accept(arexExpressionVisitorAdapter);
            sqlObj.put(DbParseConstants.HAVING, havingObj);
        }

        // order by parse
        List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
        if (orderByElements != null && !orderByElements.isEmpty()) {
            ObjectNode orderByObj = JacksonHelperUtil.getObjectNode();
            ArexOrderByVisitorAdapter arexOrderByVisitorAdapter = new ArexOrderByVisitorAdapter(
                    orderByObj);
            orderByElements.forEach(item -> {
                item.accept(arexOrderByVisitorAdapter);
            });
            sqlObj.put(DbParseConstants.ORDER_BY, orderByObj);
        }

        // fetch parse
        Fetch fetch = plainSelect.getFetch();
        if (fetch != null) {
            sqlObj.put(DbParseConstants.FETCH, fetch.toString());
        }
        // optimizeFor parse
        OptimizeFor optimizeFor = plainSelect.getOptimizeFor();
        if (optimizeFor != null) {
            sqlObj.put(DbParseConstants.OPTIMIZE_FOR, optimizeFor.toString());
        }

        // limit parse
        Limit limit = plainSelect.getLimit();
        if (limit != null) {
            sqlObj.put(DbParseConstants.LIMIT, limit.toString());
        }

        // offset parse
        Offset offset = plainSelect.getOffset();
        if (offset != null) {
            sqlObj.put(DbParseConstants.OFFSET, offset.toString());
        }

        // forUpdate parse
        boolean forUpdate = plainSelect.isForUpdate();
        if (forUpdate) {
            sqlObj.put(DbParseConstants.FOR_UPDATE, true);
        }

        // forUpdateTable parse
        Table forUpdateTable = plainSelect.getForUpdateTable();
        if (forUpdateTable != null) {
            sqlObj.put(DbParseConstants.FOR_UPDATE_TABLE, forUpdateTable.toString());
        }

        // noWait parse
        boolean noWait = plainSelect.isNoWait();
        if (noWait) {
            sqlObj.put(DbParseConstants.NO_WAIT, true);
        }

        // wait parse
        Wait wait = plainSelect.getWait();
        if (wait != null) {
            sqlObj.put(DbParseConstants.WAIT, wait.toString());
        }

    }

    @Override
    public void visit(SetOperationList setOperationList) {
        sqlObj.put("setOperationList", setOperationList.toString());
    }

    @Override
    public void visit(WithItem withItem) {
        sqlObj.put("withItem", withItem.toString());
    }

    @Override
    public void visit(ValuesStatement valuesStatement) {
        sqlObj.put("valuesStatement", valuesStatement.toString());
    }
}

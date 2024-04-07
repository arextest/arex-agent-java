package io.arex.agent.thirdparty.util.parse.sqlparse.adapter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.CommonParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.ExpressionParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.ItemParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.JoinParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.OrderByParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.TableParse;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class ArexSelectVisitorAdapter implements SelectVisitor {

    private ObjectNode sqlObj;

    public ArexSelectVisitorAdapter(ObjectNode object) {
        sqlObj = object;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        // distinct parse
        CommonParse.parseDistinct(plainSelect.getDistinct(), sqlObj);

        // skip parse
        CommonParse.parseSkip(plainSelect.getSkip(), sqlObj);

        // top parse
        CommonParse.parseTop(plainSelect.getTop(), sqlObj);

        // first parse
        CommonParse.parseFirst(plainSelect.getFirst(), sqlObj);

        // selectItems parse
        ItemParse.parseSelectItem(plainSelect.getSelectItems(), sqlObj);

        // into parse
        CommonParse.parseInto(plainSelect.getIntoTables(), sqlObj);

        // fromItem parse
        ItemParse.parseFromItem(plainSelect.getFromItem(), sqlObj);

        // jonis parse
        JoinParse.parse(plainSelect.getJoins(), sqlObj);

        // where parse
        ExpressionParse.parseWhere(plainSelect.getWhere(), sqlObj);

        // group by parse
        CommonParse.parseGroupByElement(plainSelect.getGroupBy(), sqlObj);

        // having parse
        ExpressionParse.parseHaving(plainSelect.getHaving(), sqlObj);

        // order by parse
        OrderByParse.parse(plainSelect.getOrderByElements(), sqlObj);

        // fetch parse
        CommonParse.parseFetch(plainSelect.getFetch(), sqlObj);

        // optimizeFor parse
        CommonParse.parseOptimizeFor(plainSelect.getOptimizeFor(), sqlObj);

        // limit parse
        CommonParse.parseLimit(plainSelect.getLimit(), sqlObj);

        // offset parse
        CommonParse.parseOffset(plainSelect.getOffset(), sqlObj);

        // forUpdate parse
        CommonParse.parseForUpdate(plainSelect.isForUpdate(), sqlObj);

        // forUpdateTable parse
        TableParse.parseForUpdateTable(plainSelect.getForUpdateTable(), sqlObj);

        // noWait parse
        CommonParse.parseNoWait(plainSelect.isNoWait(), sqlObj);

        // wait parse
        CommonParse.parseWait(plainSelect.getWait(), sqlObj);

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

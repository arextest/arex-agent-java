package io.arex.agent.compare.handler.parse.sqlparse.select;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.compare.handler.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;

/**
 * Created by rchen9 on 2023/1/6.
 */
public class ArexSelectItemVisitorAdapter implements SelectItemVisitor {

    private ObjectNode sqlObject;

    public ArexSelectItemVisitorAdapter(ObjectNode object) {
        sqlObject = object;
    }

    @Override
    public void visit(AllColumns allColumns) {
        sqlObject.put(allColumns.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        sqlObject.put(allTableColumns.toString(), DbParseConstants.EMPTY);
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        sqlObject.put(selectExpressionItem.toString(), DbParseConstants.EMPTY);
    }
}

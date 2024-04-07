package io.arex.agent.thirdparty.util.parse.sqlparse.adapter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
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

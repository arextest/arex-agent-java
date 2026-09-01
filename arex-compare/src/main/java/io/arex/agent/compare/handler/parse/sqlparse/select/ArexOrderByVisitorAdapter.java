package io.arex.agent.compare.handler.parse.sqlparse.select;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.compare.handler.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;

public class ArexOrderByVisitorAdapter implements OrderByVisitor {

    private ObjectNode sqlObject;

    public ArexOrderByVisitorAdapter(ObjectNode object) {
        sqlObject = object;
    }

    @Override
    public void visit(OrderByElement orderBy) {
        sqlObject.put(orderBy.toString(), DbParseConstants.EMPTY);
    }
}

package io.arex.agent.thirdparty.util.parse.sqlparse.adapter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
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

package io.arex.agent.thirdparty.util.parse.sqlparse.adapter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.util.ExpressionExtractor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.List;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class ArexItemsListVisitorAdapter implements ItemsListVisitor {
    private ArrayNode sqlArr;

    public ArexItemsListVisitorAdapter(ArrayNode array) {
        sqlArr = array;
    }

    @Override
    public void visit(SubSelect subSelect) {

    }

    @Override
    public void visit(ExpressionList expressionList) {
        List<Expression> expressions = expressionList.getExpressions();
        if (!expressions.isEmpty() && expressions.get(0) instanceof RowConstructor) {
            for (Expression expression : expressions) {
                ExpressionList exprList = ((RowConstructor) expression).getExprList();
                ArrayNode arrayNode = JacksonHelperUtil.getArrayNode();
                for (Expression expressionItem : exprList.getExpressions()) {
                    arrayNode.add(ExpressionExtractor.extract((expressionItem)));
                }
                sqlArr.add(arrayNode);
            }
        } else {
            ArrayNode arrayNode = JacksonHelperUtil.getArrayNode();
            for (Expression expression : expressions) {
                arrayNode.add(ExpressionExtractor.extract((expression)));
            }
            sqlArr.add(arrayNode);
        }
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {

    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        List<ExpressionList> expressionLists = multiExprList.getExpressionLists();
        for (ExpressionList expressionList : expressionLists) {
            ArrayNode arrayNode = JacksonHelperUtil.getArrayNode();
            List<Expression> expressions = expressionList.getExpressions();
            for (Expression expression : expressions) {
                arrayNode.add(ExpressionExtractor.extract((expression)));
            }
            sqlArr.add(arrayNode);
        }
    }
}

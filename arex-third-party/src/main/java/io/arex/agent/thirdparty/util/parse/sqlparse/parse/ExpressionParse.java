package io.arex.agent.thirdparty.util.parse.sqlparse.parse;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.adapter.ArexExpressionVisitorAdapter;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

import java.util.List;

/**
 * @author niyan
 * @date 2024/4/7
 * @since 1.0.0
 */
public class ExpressionParse {


    public static void parse(List<Expression> expressions, List<Column> columns, ObjectNode sqlObject) {
        if (expressions != null && !expressions.isEmpty()) {
            ArrayNode sqlColumnArr = JacksonHelperUtil.getArrayNode();
            ObjectNode setColumnObj = JacksonHelperUtil.getObjectNode();
            ArrayNode values = JacksonHelperUtil.getArrayNode();
            for (Expression expression : expressions) {
                values.add(expression.toString());
            }
            for (int i = 0; i < columns.size(); i++) {
                Object value = "?";
                if (i < values.size()) {
                    value = values.get(i);
                }
                setColumnObj.putPOJO(columns.get(i).toString(), value);
            }
            sqlColumnArr.add(setColumnObj);
            sqlObject.set(DbParseConstants.COLUMNS, sqlColumnArr);
        }
    }

    public static void parse(ExpressionList expressionList, ObjectNode sqlObject) {
        // expressions parse
        if (expressionList != null) {
            List<Expression> expressions = expressionList.getExpressions();
            if (expressions != null && !expressions.isEmpty()) {
                ArrayNode sqlColumnArr = JacksonHelperUtil.getArrayNode();

                ObjectNode setColumnObj = JacksonHelperUtil.getObjectNode();
                setColumnObj.set(DbParseConstants.AND_OR, JacksonHelperUtil.getArrayNode());
                setColumnObj.set(DbParseConstants.COLUMNS, JacksonHelperUtil.getObjectNode());
                for (Expression expression : expressions) {
                    expression.accept(new ArexExpressionVisitorAdapter(setColumnObj));
                }
                sqlColumnArr.add(setColumnObj.get(DbParseConstants.COLUMNS));
                sqlObject.set(DbParseConstants.COLUMNS, sqlColumnArr);
            }
        }
    }

    public static void parseHaving(Expression having, ObjectNode sqlObject) {
        if (having != null) {
            ObjectNode havingObj = JacksonHelperUtil.getObjectNode();
            havingObj.set(DbParseConstants.AND_OR, JacksonHelperUtil.getArrayNode());
            havingObj.set(DbParseConstants.COLUMNS, JacksonHelperUtil.getObjectNode());
            ArexExpressionVisitorAdapter arexExpressionVisitorAdapter = new ArexExpressionVisitorAdapter(
                    havingObj);
            having.accept(arexExpressionVisitorAdapter);
            sqlObject.set(DbParseConstants.HAVING, havingObj);
        }
    }

    public static void parseWhere(Expression where, ObjectNode sqlObject) {
        if (where != null) {
            ObjectNode whereObj = JacksonHelperUtil.getObjectNode();
            whereObj.set(DbParseConstants.AND_OR, JacksonHelperUtil.getArrayNode());
            whereObj.set(DbParseConstants.COLUMNS, JacksonHelperUtil.getObjectNode());

            where.accept(new ArexExpressionVisitorAdapter(whereObj));
            sqlObject.set(DbParseConstants.WHERE, whereObj);
        }
    }
}

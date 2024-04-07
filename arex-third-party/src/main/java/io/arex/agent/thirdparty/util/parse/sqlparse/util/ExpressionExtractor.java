package io.arex.agent.thirdparty.util.parse.sqlparse.util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class ExpressionExtractor {

    public static String extract(Expression expression) {
        if (expression == null) {
            return "";
        }
        if (expression instanceof StringValue) {
            StringValue stringValue = (StringValue) expression;
            return stringValue.getValue();
        }
        return expression.toString();
    }
}

package io.arex.agent.compare.handler.parse.sqlparse.select.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import java.util.List;

import io.arex.agent.compare.handler.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.compare.utils.JacksonHelperUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;

public class JoinParseUtil {

    public static ObjectNode parse(Join parseObj) {

        ObjectNode res = JacksonHelperUtil.getObjectNode();
        // join type parse
        res.put(DbParseConstants.TYPE, getJOINType(parseObj));

        // rightItem parse
        FromItem rightItem = parseObj.getRightItem();
        if (rightItem != null) {
            res.put(DbParseConstants.TABLE, rightItem.toString());
        }

        // onExpressions parse
        Collection<Expression> onExpressions = parseObj.getOnExpressions();
        if (onExpressions != null && !onExpressions.isEmpty()) {
            ObjectNode onObj = JacksonHelperUtil.getObjectNode();
            onExpressions.forEach(item -> {
                onObj.put(item.toString(), DbParseConstants.EMPTY);
            });
            res.put(DbParseConstants.ON, onObj);
        }

        // usingColumns parse
        List<Column> usingColumns = parseObj.getUsingColumns();
        if (usingColumns != null && !usingColumns.isEmpty()) {
            ObjectNode usingObj = JacksonHelperUtil.getObjectNode();
            usingColumns.forEach(item -> {
                usingObj.put(item.toString(), DbParseConstants.EMPTY);
            });
            res.put(DbParseConstants.USING, usingObj);
        }

        return res;
    }

    private static String getJOINType(Join parseObj) {
        StringBuilder builder = new StringBuilder();
        if (parseObj.isSimple() && parseObj.isOuter()) {
            builder.append("OUTER JOIN");
        } else if (parseObj.isSimple()) {
            builder.append("");
        } else {
            if (parseObj.isNatural()) {
                builder.append("NATURAL ");
            }

            if (parseObj.isRight()) {
                builder.append("RIGHT ");
            } else if (parseObj.isFull()) {
                builder.append("FULL ");
            } else if (parseObj.isLeft()) {
                builder.append("LEFT ");
            } else if (parseObj.isCross()) {
                builder.append("CROSS ");
            }

            if (parseObj.isOuter()) {
                builder.append("OUTER ");
            } else if (parseObj.isInner()) {
                builder.append("INNER ");
            } else if (parseObj.isSemi()) {
                builder.append("SEMI ");
            }

            if (parseObj.isStraight()) {
                builder.append("STRAIGHT_JOIN ");
            } else if (parseObj.isApply()) {
                builder.append("APPLY ");
            } else {
                builder.append("JOIN");
            }
        }
        return builder.toString();
    }
}

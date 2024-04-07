package io.arex.agent.thirdparty.util.parse.sqlparse.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;

import java.util.Collection;


/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class JoinParseUtil {
    /**
     * Parse JOIN object
     * @param joinParseObj join object
     * @return JOIN object
     */
    public static ObjectNode parse(Join joinParseObj) {
        ObjectNode joinObj = JacksonHelperUtil.getObjectNode();
        // join type parse
        joinObj.put(DbParseConstants.TYPE, getJOINType(joinParseObj));

        // rightItem parse
        FromItem rightItem = joinParseObj.getRightItem();
        if (rightItem != null) {
            joinObj.put(DbParseConstants.TABLE, rightItem.toString());
        }

        // onExpressions parse
        Collection<Expression> onExpressions = joinParseObj.getOnExpressions();
        if (onExpressions != null && !onExpressions.isEmpty()) {
            ObjectNode onObj = JacksonHelperUtil.getObjectNode();
            onExpressions.forEach(item -> {
                onObj.put(item.toString(), DbParseConstants.EMPTY);
            });
            joinObj.put(DbParseConstants.ON, onObj);
        }

        // usingColumns parse
        Collection<Column> usingColumns = joinParseObj.getUsingColumns();
        if (usingColumns != null && !usingColumns.isEmpty()) {
            ObjectNode usingObj = JacksonHelperUtil.getObjectNode();
            usingColumns.forEach(item -> {
                usingObj.put(item.toString(), DbParseConstants.EMPTY);
            });
            joinObj.put(DbParseConstants.USING, usingObj);
        }

        return joinObj;
    }


    /**
     * Get JOIN type
     * @param parseObj join object
     * @return JOIN type
     */
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

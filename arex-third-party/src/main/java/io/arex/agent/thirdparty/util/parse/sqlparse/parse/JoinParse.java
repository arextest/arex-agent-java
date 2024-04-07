package io.arex.agent.thirdparty.util.parse.sqlparse.parse;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.thirdparty.util.parse.sqlparse.util.JoinParseUtil;
import net.sf.jsqlparser.statement.select.Join;

import java.util.List;

/**
 * @author niyan
 * @date 2024/4/7
 * @since 1.0.0
 */
public class JoinParse {

    public static void parse(List<Join> joins, ObjectNode sqlObject) {
        if (joins != null && !joins.isEmpty()) {
            ArrayNode joinArr = JacksonHelperUtil.getArrayNode();
            joins.forEach(item -> {
                joinArr.add(JoinParseUtil.parse(item));
            });
            sqlObject.set(DbParseConstants.JOIN, joinArr);
        }
    }

    public static void startJoinsParse(List<Join> startJoins, ObjectNode sqlObject) {
        if (startJoins != null && !startJoins.isEmpty()) {
            ArrayNode joinArr = JacksonHelperUtil.getArrayNode();
            startJoins.forEach(item -> {
                joinArr.add(JoinParseUtil.parse(item));
            });
            sqlObject.set(DbParseConstants.START_JOINS, joinArr);
        }
    }
}

package io.arex.agent.thirdparty.util.parse.sqlparse.util;

import com.fasterxml.jackson.databind.JsonNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author niyan
 * @date 2024/4/7
 * @since 1.0.0
 */
public class ParseUtil {


    public static Object parseTable(JsonNode result) {
        switch (result.get(DbParseConstants.ACTION).asText()) {
            case DbParseConstants.SELECT:
                return parseSelectTableName(result);
            case DbParseConstants.DELETE:
                return parseDeleteTable(result);
            case DbParseConstants.INSERT:
                return parseInsertTable(result);
            case DbParseConstants.REPLACE:
                return parseReplayTable(result);
            case DbParseConstants.UPDATE:
                return parseUpdateTable(result);
            default:
                return null;
        }

    }

    public static String parseSelectTableName(JsonNode result) {
        if (result.has(DbParseConstants.FROM)) {
            JsonNode jsonNode = result.get(DbParseConstants.FROM).get(DbParseConstants.TABLE);
            if (jsonNode.isContainerNode()) {
                return parseSelectTableName(jsonNode);
            }
            return jsonNode.asText();
        }
        return null;
    }

    public static List<String> parseDeleteTable(JsonNode result) {
        JsonNode jsonNode = result.get(DbParseConstants.DEL_TABLES);
        if (jsonNode.isContainerNode()) {
            List<String> tableNames = new ArrayList<>();
            jsonNode.fieldNames().forEachRemaining(tableNames::add);
            return tableNames;
        }
        return Collections.singletonList(jsonNode.asText());
    }

    public static String parseInsertTable(JsonNode result) {
        return parseCommonTableName(result);
    }

    public static String parseReplayTable(JsonNode result) {
        return parseCommonTableName(result);
    }

    public static String parseUpdateTable(JsonNode result) {
        return parseCommonTableName(result);
    }


    public static String parseCommonTableName(JsonNode result) {
        if (result.has(DbParseConstants.TABLE)) {
            JsonNode jsonNode = result.get(DbParseConstants.TABLE);
            if (jsonNode.isContainerNode()) {
                return parseCommonTableName(jsonNode);
            }
            return jsonNode.asText();
        }
        return null;
    }

}

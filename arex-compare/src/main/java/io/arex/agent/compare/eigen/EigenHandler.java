package io.arex.agent.compare.eigen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.compare.handler.parse.JSONParse;
import io.arex.agent.compare.handler.parse.ObjectParse;
import io.arex.agent.compare.handler.parse.sqlparse.SqlParse;
import io.arex.agent.compare.model.RulesConfig;
import io.arex.agent.compare.model.eigen.EigenResult;

import java.util.Objects;

public class EigenHandler {

    private static ObjectParse objectParse = new ObjectParse();
    private static JSONParse jsonParse = new JSONParse();
    private static SqlParse sqlParse = new SqlParse();

    private static EigenMapCalculate eigenMapCalculate = new EigenMapCalculate();

    public EigenResult doHandler(RulesConfig rulesConfig) {
        Object obj;
        try {
            // if it is not json, it will return null
            obj = objectParse.msgToObj(rulesConfig.getBaseMsg());

            if (obj instanceof JsonNode) {
                jsonParse.getJSONParseResult(obj, rulesConfig);
            }

            if (Objects.equals(rulesConfig.getCategoryType(), MockCategoryType.DATABASE.getName())
                    && obj instanceof ObjectNode) {
                sqlParse.sqlParse((ObjectNode) obj, rulesConfig.isNameToLower());
            }
        } catch (Throwable e) {
            obj = null;
        }

        return eigenMapCalculate.doCalculate(obj, rulesConfig);
    }

}

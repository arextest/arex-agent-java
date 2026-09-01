package io.arex.agent.compare.handler.parse;

import io.arex.agent.compare.model.RulesConfig;
import io.arex.agent.compare.model.log.NodeEntity;
import io.arex.agent.compare.utils.NameConvertUtil;

import java.util.List;
import java.util.Map;

public class JSONParse {
    public Map<List<NodeEntity>, String> getJSONParseResult(Object obj, RulesConfig rulesConfig) {

        StringAndCompressParse stringAndCompressParse = new StringAndCompressParse();
        stringAndCompressParse.setNameToLower(rulesConfig.isNameToLower());
        stringAndCompressParse.getJSONParse(obj, obj);
        // Convert field names in JSONObject to lowercase
        if (rulesConfig.isNameToLower()) {
            NameConvertUtil.nameConvert(obj);
        }
        return stringAndCompressParse.getOriginal();
    }
}

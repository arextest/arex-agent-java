package io.arex.agent.compare.utils;

import io.arex.agent.compare.model.RulesConfig;
import io.arex.agent.compare.model.eigen.EigenOptions;

import java.util.ArrayList;

public class EigenOptionsToRulesConvert {

    public static RulesConfig convert(String msg, EigenOptions eigenOptions) {
        RulesConfig rulesConfig = new RulesConfig();
        rulesConfig.setBaseMsg(msg);
        rulesConfig.setNameToLower(true);
        copyOptionsToRules(eigenOptions, rulesConfig);
        configToLower(rulesConfig);
        return rulesConfig;
    }

    private static void copyOptionsToRules(EigenOptions eigenOptions, RulesConfig rulesConfig) {
        if (eigenOptions == null) {
            return;
        }
        rulesConfig.setCategoryType(eigenOptions.getCategoryType());
        rulesConfig.setExclusions(eigenOptions.getExclusions() == null ? null
                : new ArrayList<>(eigenOptions.getExclusions()));
        rulesConfig.setIgnoreNodeSet(eigenOptions.getIgnoreNodes());
    }

    private static void configToLower(RulesConfig rulesConfig) {
        rulesConfig.setExclusions(FieldToLowerUtil.listListToLower(rulesConfig.getExclusions()));
        rulesConfig.setIgnoreNodeSet(FieldToLowerUtil.setToLower(rulesConfig.getIgnoreNodeSet()));
    }
}

package io.arex.agent.compare.sdk;

import io.arex.agent.compare.eigen.EigenCalculateHandler;
import io.arex.agent.compare.model.eigen.EigenOptions;
import io.arex.agent.compare.model.RulesConfig;
import io.arex.agent.compare.model.eigen.EigenResult;
import io.arex.agent.compare.utils.EigenOptionsToRulesConvert;
import io.arex.agent.compare.utils.LogUtil;

public class EigenCalculateSDK {
    private static final EigenCalculateHandler eigenHandler = new EigenCalculateHandler();

    public EigenResult calculateEigen(String msg, EigenOptions eigenOptions) {
        EigenResult eigenResult = null;
        try {
            RulesConfig rulesConfig = EigenOptionsToRulesConvert.convert(msg, eigenOptions);
            eigenResult = eigenHandler.doHandler(rulesConfig);
        } catch (Throwable e) {
            LogUtil.warn("calculate eigen value fail", e);
        }
        return eigenResult;
    }
}

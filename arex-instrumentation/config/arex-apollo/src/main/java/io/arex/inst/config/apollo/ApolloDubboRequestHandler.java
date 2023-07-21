package io.arex.inst.config.apollo;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.request.RequestHandler;

import java.util.Map;

@AutoService(RequestHandler.class)
public class ApolloDubboRequestHandler implements RequestHandler<Map<String, String>, Map<String, String>> {
    @Override
    public String name() {
        return MockCategoryType.DUBBO_PROVIDER.getName();
    }

    @Override
    public void preHandle(Map<String, String> request) {
        String recordId = request.get(ArexConstants.RECORD_ID);
        if (StringUtil.isEmpty(recordId)) {
            return;
        }
        String configVersion = request.get(ArexConstants.CONFIG_DEPENDENCY);
        ApolloConfigHelper.initReplayState(recordId, configVersion);

        if (StringUtil.isEmpty(configVersion)) {
            return;
        }
        /*
        Does not include increment config, as Apollo has not yet created an instance of this configuration
        it will be replay in io.arex.inst.config.apollo.ApolloConfigHelper.getReplayConfig
         */
        ApolloConfigHelper.replayAllConfigs();
    }

    @Override
    public void postHandle(Map<String, String> request, Map<String, String> response) {
        if (postInvalid(request, response)) {
            return;
        }
        /*
        The reason for recording in postHandle() is to ensure that both full and increment configurations can be recorded,
        because some configurations are initialized only when the code is executed in a specific scenario,
        it may not record these configurations during preHandle()
         */
        ApolloConfigHelper.recordAllConfigs();
        request.put(ArexConstants.CONFIG_VERSION, ApolloConfigExtractor.getCurrentRecordConfigVersion());
    }

    private boolean postInvalid(Map<String, String> request, Map<String, String> response) {
        if (request == null) {
            return true;
        }
        return !ContextManager.needRecord();
    }
}

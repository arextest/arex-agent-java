package io.arex.inst.config.apollo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.request.RequestHandler;

@AutoService(RequestHandler.class)
public class ApolloServletRequestHandler implements RequestHandler<HttpServletRequest, HttpServletResponse> {
    @Override
    public String name() {
        return MockCategoryType.SERVLET.getName();
    }

    @Override
    public void preHandle(HttpServletRequest request) {
        String recordId = request.getHeader(ArexConstants.RECORD_ID);
        if (StringUtil.isEmpty(recordId)) {
            return;
        }
        String configVersion = request.getHeader(ArexConstants.CONFIG_DEPENDENCY);
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
    public void postHandle(HttpServletRequest request, HttpServletResponse response) {
        if (postInvalid(request, response)) {
            return;
        }
        /*
        The reason for recording in postHandle() is to ensure that both full and increment configurations can be recorded,
        because some configurations are initialized only when the code is executed in a specific scenario,
        it may not record these configurations during preHandle()
         */
        ApolloConfigHelper.recordAllConfigs();
    }

    private boolean postInvalid(HttpServletRequest request, HttpServletResponse response) {
        if (request == null || response == null) {
            return true;
        }
        if (response.getHeader(ArexConstants.RECORD_ID) != null) {
            return true;
        }
        return !ContextManager.needRecord();
    }
}

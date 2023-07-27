package io.arex.inst.config.apollo;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.request.RequestHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@AutoService(RequestHandler.class)
public class ApolloServletV5RequestHandler implements RequestHandler<HttpServletRequest, HttpServletResponse> {
    @Override
    public String name() {
        return MockCategoryType.SERVLET.getName();
    }

    @Override
    public void preHandle(HttpServletRequest request) {
        ApolloConfigHelper.initAndRecord(
                () -> request.getHeader(ArexConstants.RECORD_ID),
                () -> request.getHeader(ArexConstants.CONFIG_DEPENDENCY));
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
        request.setAttribute(ArexConstants.CONFIG_VERSION, ApolloConfigExtractor.getCurrentRecordConfigVersion());
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

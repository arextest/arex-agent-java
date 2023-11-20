package io.arex.inst.runtime.request;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.MockUtils;


@AutoService(RequestHandler.class)
public class MergeRecordServletRequestHandler implements RequestHandler<Object, Object> {
    @Override
    public String name() {
        return MockCategoryType.SERVLET.getName();
    }

    @Override
    public void preHandle(Object request) {}

    @Override
    public void handleAfterCreateContext(Object request) {
        if (!ContextManager.needReplay() || !Config.get().getBoolean(ArexConstants.MERGE_RECORD_ENABLE, true)) {
            return;
        }
        // init replay and cached dynamic class
        MockUtils.mergeReplay();
    }

    @Override
    public void postHandle(Object request, Object response) {}
}

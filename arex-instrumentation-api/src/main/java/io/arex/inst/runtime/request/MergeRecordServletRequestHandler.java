package io.arex.inst.runtime.request;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.inst.runtime.util.MergeRecordReplayUtil;


@AutoService(RequestHandler.class)
public class MergeRecordServletRequestHandler implements RequestHandler<Object, Object> {
    @Override
    public String name() {
        return MockCategoryType.SERVLET.getName();
    }

    @Override
    public void preHandle(Object request) {
        // no need implement
    }

    @Override
    public void handleAfterCreateContext(Object request) {
        // init replay and cached dynamic class
        MergeRecordReplayUtil.mergeReplay();
    }

    @Override
    public void postHandle(Object request, Object response) {
        // no need implement
    }
}

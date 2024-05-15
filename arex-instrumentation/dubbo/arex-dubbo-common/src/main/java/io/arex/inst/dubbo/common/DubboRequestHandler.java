package io.arex.inst.dubbo.common;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.inst.runtime.request.RequestHandler;
import io.arex.inst.runtime.util.ReplayUtil;


@AutoService(RequestHandler.class)
public class DubboRequestHandler implements RequestHandler<Object, Object> {
    @Override
    public String name() {
        return MockCategoryType.DUBBO_PROVIDER.getName();
    }

    @Override
    public void preHandle(Object request) {
        // no need implement
    }

    @Override
    public void handleAfterCreateContext(Object request) {
        // init replay and cached dynamic class
        ReplayUtil.replayAllMocker();
    }

    @Override
    public void postHandle(Object request, Object response) {
        // no need implement
    }
}

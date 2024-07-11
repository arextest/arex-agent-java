package io.arex.inst.dubbo.common;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.inst.runtime.request.RequestHandler;


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
        // no need implement
    }

    @Override
    public void postHandle(Object request, Object response) {
        // no need implement
    }
}

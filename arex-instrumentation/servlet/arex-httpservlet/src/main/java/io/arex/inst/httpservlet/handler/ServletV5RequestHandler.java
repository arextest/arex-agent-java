package io.arex.inst.httpservlet.handler;

import com.google.auto.service.AutoService;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.request.RequestHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@AutoService(RequestHandler.class)
public class ServletV5RequestHandler implements RequestHandler<HttpServletRequest, HttpServletResponse> {
    @Override
    public String name() {
        return ArexConstants.SERVLET_V5;
    }

    @Override
    public void preHandle(HttpServletRequest request) {
        // no need implement
    }

    @Override
    public void handleAfterCreateContext(HttpServletRequest request) {
        // no need implement
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response) {
        // no need implement
    }
}

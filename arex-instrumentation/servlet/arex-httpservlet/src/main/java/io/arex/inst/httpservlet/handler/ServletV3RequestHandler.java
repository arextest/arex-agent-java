package io.arex.inst.httpservlet.handler;

import com.google.auto.service.AutoService;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.request.RequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@AutoService(RequestHandler.class)
public class ServletV3RequestHandler implements RequestHandler<HttpServletRequest, HttpServletResponse> {
    @Override
    public String name() {
        return ArexConstants.SERVLET_V3;
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

package io.arex.inst.httpservlet.convert.impl;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.convert.HttpMessageConverter;

public class ApplicationJsonBodyConverter<HttpServletRequest, HttpServletResponse> implements HttpMessageConverter<HttpServletRequest, HttpServletResponse> {

    private static final String CONTENT_TYPE = "application/json";

    @Override
    public boolean match(HttpServletRequest request, HttpServletResponse response, ServletAdapter adapter) {
        if (request == null || response ==null || adapter == null) {
            return false;
        }
        String contentType = adapter.getContentType(request);
        return StringUtil.isNotEmpty(contentType) && contentType.contains(CONTENT_TYPE);
    }

    @Override
    public byte[] getRequest(HttpServletRequest request, ServletAdapter adapter) {
        if (request == null || adapter == null) {
            return new byte[0];
        }
        return adapter.getRequestBytes(request);
    }

    @Override
    public byte[] getResponse(HttpServletResponse response, ServletAdapter adapter) {
        return new byte[0];
    }
}





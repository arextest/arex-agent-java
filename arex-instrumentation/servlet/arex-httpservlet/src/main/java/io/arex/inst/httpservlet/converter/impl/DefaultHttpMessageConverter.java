package io.arex.inst.httpservlet.converter.impl;

import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.converter.HttpMessageConverter;

public class DefaultHttpMessageConverter<HttpServletRequest, HttpServletResponse> implements
    HttpMessageConverter<HttpServletRequest, HttpServletResponse> {

    private DefaultHttpMessageConverter() {
    }

    private static final HttpMessageConverter INSTANCE = new DefaultHttpMessageConverter<>();

    public static HttpMessageConverter getInstance() {
        return INSTANCE;
    }


    @Override
    public boolean support(HttpServletRequest request, ServletAdapter<HttpServletRequest, HttpServletResponse> adapter) {
        return false;
    }

    @Override
    public byte[] getRequest(HttpServletRequest request, ServletAdapter<HttpServletRequest, HttpServletResponse> adapter) {
        return adapter.getRequestBytes(request);
    }

    @Override
    public byte[] getResponse(HttpServletResponse response,
        ServletAdapter<HttpServletRequest, HttpServletResponse> adapter) {
        return new byte[0];
    }
}

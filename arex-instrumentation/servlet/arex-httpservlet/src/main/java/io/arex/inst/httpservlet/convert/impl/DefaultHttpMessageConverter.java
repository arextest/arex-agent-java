package io.arex.inst.httpservlet.convert.impl;

import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.convert.HttpMessageConverter;

public class DefaultHttpMessageConverter<HttpServletRequest, HttpServletResponse> implements HttpMessageConverter<HttpServletRequest, HttpServletResponse> {

    private DefaultHttpMessageConverter() {

    }

    private static class SingletonHolder {
        private static final DefaultHttpMessageConverter INSTANCE = new DefaultHttpMessageConverter();
    }

    public static DefaultHttpMessageConverter getInstance() {
        return SingletonHolder.INSTANCE;
    }


    @Override
    public boolean match(HttpServletRequest request, HttpServletResponse response, ServletAdapter adapter) {
        return false;
    }

    @Override
    public byte[] getRequest(HttpServletRequest request, ServletAdapter adapter) {
        return new byte[0];
    }

    @Override
    public byte[] getResponse(HttpServletResponse response, ServletAdapter adapter) {
        return new byte[0];
    }
}

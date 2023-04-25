package io.arex.inst.httpservlet.convert.impl;

import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.convert.HttpMessageConverter;

public class DefaultHttpMessageConverter<HttpServletRequest, HttpServletResponse> implements HttpMessageConverter<HttpServletRequest, HttpServletResponse> {

    private DefaultHttpMessageConverter() {

    }

    private volatile static DefaultHttpMessageConverter instance;


    public static final DefaultHttpMessageConverter getInstance() {
        if (instance == null) {
            synchronized (DefaultHttpMessageConverter.class) {
                if (instance == null) {
                    instance = new DefaultHttpMessageConverter();
                }
            }
        }
        return instance;
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

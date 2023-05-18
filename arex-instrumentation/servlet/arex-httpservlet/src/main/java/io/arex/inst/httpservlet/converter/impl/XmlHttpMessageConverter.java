package io.arex.inst.httpservlet.converter.impl;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.converter.HttpMessageConverter;


public class XmlHttpMessageConverter<HttpServletRequest, HttpServletResponse> implements
    HttpMessageConverter<HttpServletRequest, HttpServletResponse> {

    private static final String CONTENT_TYPE = "application/xml";

    @Override
    public boolean support(HttpServletRequest request, ServletAdapter<HttpServletRequest, HttpServletResponse> adapter) {
        String contentType = adapter.getContentType(request);
        return StringUtil.isNotEmpty(contentType) && contentType.contains(CONTENT_TYPE);
    }

    @Override
    public byte[] getRequest(HttpServletRequest request,
        ServletAdapter<HttpServletRequest, HttpServletResponse> adapter) {
        return adapter.getRequestBytes(request);
    }

    @Override
    public byte[] getResponse(HttpServletResponse response,
        ServletAdapter<HttpServletRequest, HttpServletResponse> adapter) {
        return new byte[0];
    }

}

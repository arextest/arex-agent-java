package io.arex.inst.httpservlet.convert.impl;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.convert.BodyConverters;


public class ApplicationXmlBodyConvert<HttpServletRequest, HttpServletResponse> implements BodyConverters<HttpServletRequest, HttpServletResponse> {

    private static final String CONTENT_TYPE = "application/xml";

    @Override
    public boolean match(HttpServletRequest httpServletRequest, ServletAdapter adapter) {
        if (httpServletRequest == null || adapter == null) {
            return false;
        }
        String contentType = adapter.getContentType(httpServletRequest);
        return StringUtil.isNotEmpty(contentType) && contentType.contains(CONTENT_TYPE);
    }

    @Override
    public String getBody(HttpServletRequest httpServletRequest, ServletAdapter adapter) {
        return null;
    }
}
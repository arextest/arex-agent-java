package io.arex.inst.httpservlet.convert.impl;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.convert.BodyConverters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;

public class ApplicationJsonBodyConvert implements BodyConverters<HttpServletRequest, HttpServletResponse> {

    private static final String CONTENT_TYPE = "application/json";

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
        if (httpServletRequest == null || adapter == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(adapter.getRequestBytes(httpServletRequest));
    }
}

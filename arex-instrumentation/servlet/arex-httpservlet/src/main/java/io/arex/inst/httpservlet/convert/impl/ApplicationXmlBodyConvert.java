package io.arex.inst.httpservlet.convert.impl;

import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.convert.BodyConverters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApplicationXmlBodyConvert implements BodyConverters<HttpServletRequest, HttpServletResponse> {

    private static final String CONTENT_TYPE = "application/xml";

    @Override
    public boolean match(HttpServletRequest httpServletRequest, ServletAdapter adapter) {
        return false;
    }

    @Override
    public String getBody(HttpServletRequest httpServletRequest, ServletAdapter adapter) {
        return null;
    }
}
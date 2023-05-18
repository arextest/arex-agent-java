package io.arex.inst.httpservlet.converter;

import io.arex.inst.httpservlet.adapter.ServletAdapter;

public interface HttpMessageConverter<HttpServletRequest, HttpServletResponse> {

    boolean support(HttpServletRequest request, ServletAdapter<HttpServletRequest, HttpServletResponse> adapter);

    byte[] getRequest(HttpServletRequest request, ServletAdapter<HttpServletRequest, HttpServletResponse> adapter);

    byte[] getResponse(HttpServletResponse response, ServletAdapter<HttpServletRequest, HttpServletResponse> adapter);
}

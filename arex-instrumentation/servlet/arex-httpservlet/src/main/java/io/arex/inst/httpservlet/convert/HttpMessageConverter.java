package io.arex.inst.httpservlet.convert;

import io.arex.inst.httpservlet.adapter.ServletAdapter;

public interface HttpMessageConverter<HttpServletRequest, HttpServletResponse> {

    boolean match(HttpServletRequest request,HttpServletResponse response, ServletAdapter adapter);
    byte[] getRequest(HttpServletRequest request, ServletAdapter adapter);
    byte[] getResponse(HttpServletResponse request, ServletAdapter adapter);
}

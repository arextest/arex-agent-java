package io.arex.inst.httpservlet.convert;

import io.arex.inst.httpservlet.adapter.ServletAdapter;

public interface BodyConverters<HttpServletRequest, HttpServletResponse> {

    boolean match(HttpServletRequest request, ServletAdapter adapter);

    String getBody(HttpServletRequest request, ServletAdapter adapter);
}

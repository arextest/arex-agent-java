package io.arex.inst.httpservlet.adapter;

import org.springframework.web.context.request.NativeWebRequest;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

/**
 * ServletAdapter
 */
public interface ServletAdapter<HttpServletRequest, HttpServletResponse> {

    @Nullable
    HttpServletRequest getNativeRequest(NativeWebRequest nativeWebRequest);

    String getRequestHeader(HttpServletRequest httpServletRequest, String name);

    String getResponseHeader(HttpServletResponse httpServletResponse, String name);

    void setResponseHeader(HttpServletResponse httpServletResponse, String name, String value);

    HttpServletRequest wrapRequest(HttpServletRequest httpServletRequest);

    HttpServletResponse wrapResponse(HttpServletResponse httpServletResponse);

    int getStatus(HttpServletResponse httpServletResponse);

    boolean isAsyncStarted(HttpServletRequest httpServletRequest);

    Object getAttribute(HttpServletRequest httpServletRequest, String name);

    void setAttribute(HttpServletRequest httpServletRequest, String name, Object o);

    void removeAttribute(HttpServletRequest httpServletRequest, String name);

    boolean wrapped(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);

    void copyBodyToResponse(HttpServletResponse httpServletResponse) throws IOException;

    void addListener(ServletAdapter<HttpServletRequest, HttpServletResponse> adapter,
        HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);

    String getContentType(HttpServletRequest httpServletRequest);

    String getFullUrl(HttpServletRequest httpServletRequest);

    String getRequestURI(HttpServletRequest httpServletRequest);

    String getMethod(HttpServletRequest httpServletRequest);

    Enumeration<String> getRequestHeaderNames(HttpServletRequest httpServletRequest);

    Collection<String> getResponseHeaderNames(HttpServletResponse httpServletResponse);

    byte[] getRequestBytes(HttpServletRequest httpServletRequest);

    byte[] getResponseBytes(HttpServletResponse httpServletResponse);

    HttpServletRequest asHttpServletRequest(Object servletRequest);

    HttpServletResponse asHttpServletResponse(Object servletResponse);

    boolean markProcessed(HttpServletRequest httpServletRequest, String mark);
}

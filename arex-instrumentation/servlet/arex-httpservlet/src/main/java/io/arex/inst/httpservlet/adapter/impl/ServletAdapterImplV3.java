package io.arex.inst.httpservlet.adapter.impl;

import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.wrapper.CachedBodyRequestWrapperV3;
import io.arex.inst.httpservlet.wrapper.CachedBodyResponseWrapperV3;
import io.arex.inst.httpservlet.listener.ServletAsyncListenerV3;
import org.springframework.web.context.request.NativeWebRequest;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

/**
 * ServletAdapterImplV3
 */
public class ServletAdapterImplV3 implements ServletAdapter<HttpServletRequest, HttpServletResponse> {
    private static final ServletAdapterImplV3 INSTANCE = new ServletAdapterImplV3();

    public static ServletAdapterImplV3 getInstance() {
        return INSTANCE;
    }

    @Nullable
    @Override
    public HttpServletRequest getNativeRequest(NativeWebRequest nativeWebRequest) {
        return nativeWebRequest.getNativeRequest(HttpServletRequest.class);
    }

    @Override
    public String getRequestHeader(HttpServletRequest httpServletRequest, String name) {
        return httpServletRequest.getHeader(name);
    }

    @Override
    public HttpServletRequest wrapRequest(HttpServletRequest httpServletRequest) {
        return new CachedBodyRequestWrapperV3(httpServletRequest);
    }

    @Override
    public HttpServletResponse wrapResponse(HttpServletResponse httpServletResponse) {
        return new CachedBodyResponseWrapperV3(httpServletResponse);
    }

    @Override
    public void setResponseHeader(HttpServletResponse httpServletResponse, String name, String value) {
        httpServletResponse.setHeader(name, value);
    }

    @Override
    public int getStatus(HttpServletResponse httpServletResponse) {
        return httpServletResponse.getStatus();
    }

    @Override
    public boolean isAsyncStarted(HttpServletRequest httpServletRequest) {
        return httpServletRequest.isAsyncStarted();
    }

    @Override
    public Object getAttribute(HttpServletRequest httpServletRequest, String name) {
        return httpServletRequest.getAttribute(name);
    }

    @Override
    public void setAttribute(HttpServletRequest httpServletRequest, String name, Object o) {
        httpServletRequest.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(HttpServletRequest httpServletRequest, String name) {
        httpServletRequest.removeAttribute(name);
    }

    @Override
    public boolean wrapped(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return httpServletRequest instanceof CachedBodyRequestWrapperV3
            && httpServletResponse instanceof CachedBodyResponseWrapperV3;
    }

    @Override
    public void copyBodyToResponse(HttpServletResponse httpServletResponse) throws IOException {
        ((CachedBodyResponseWrapperV3) httpServletResponse).copyBodyToResponse();
    }

    @Override
    public void addListener(ServletAdapter<HttpServletRequest, HttpServletResponse> adapter,
        HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        httpServletRequest.getAsyncContext()
            .addListener(new ServletAsyncListenerV3(adapter), httpServletRequest, httpServletResponse);
    }

    @Override
    public String getContentType(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getContentType();
    }

    @Override
    public String getFullUrl(HttpServletRequest httpServletRequest) {
        StringBuilder fullUrl = new StringBuilder(httpServletRequest.getRequestURI());
        String queryString = httpServletRequest.getQueryString();

        if (queryString == null) {
            return fullUrl.toString();
        } else {
            return fullUrl.append('?').append(queryString).toString();
        }
    }

    @Override
    public String getRequestURI(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getRequestURI();
    }

    @Override
    public String getResponseHeader(HttpServletResponse httpServletResponse, String name) {
        return httpServletResponse.getHeader(name);
    }

    @Override
    public String getMethod(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getMethod();
    }

    @Override
    public Enumeration<String> getRequestHeaderNames(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeaderNames();
    }

    @Override
    public Collection<String> getResponseHeaderNames(HttpServletResponse httpServletResponse) {
        return httpServletResponse.getHeaderNames();
    }

    @Override
    public byte[] getRequestBytes(HttpServletRequest httpServletRequest) {
        return ((CachedBodyRequestWrapperV3) httpServletRequest).getContentAsByteArray();
    }

    @Override
    public byte[] getResponseBytes(HttpServletResponse httpServletResponse) {
        return ((CachedBodyResponseWrapperV3) httpServletResponse).getContentAsByteArray();
    }
}

package io.arex.inst.httpservlet.adapter.impl;

import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.listener.ServletAsyncListenerV5;
import io.arex.inst.httpservlet.wrapper.CachedBodyRequestWrapperV5;
import io.arex.inst.httpservlet.wrapper.CachedBodyResponseWrapperV5;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

/**
 * ServletAdapterImplV5
 */
public class ServletAdapterImplV5 implements ServletAdapter<HttpServletRequest, HttpServletResponse> {
    private static final ServletAdapterImplV5 INSTANCE = new ServletAdapterImplV5();

    public static ServletAdapterImplV5 getInstance() {
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
        return new CachedBodyRequestWrapperV5(httpServletRequest);
    }

    @Override
    public HttpServletResponse wrapResponse(HttpServletResponse httpServletResponse) {
        return new CachedBodyResponseWrapperV5(httpServletResponse);
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
        return httpServletRequest instanceof CachedBodyRequestWrapperV5
            && httpServletResponse instanceof CachedBodyResponseWrapperV5;
    }

    @Override
    public void copyBodyToResponse(HttpServletResponse httpServletResponse) throws IOException {
        ((CachedBodyResponseWrapperV5) httpServletResponse).copyBodyToResponse();
    }

    @Override
    public void addListener(ServletAdapter<HttpServletRequest, HttpServletResponse> adapter,
        HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        httpServletRequest.getAsyncContext()
            .addListener(new ServletAsyncListenerV5(adapter), httpServletRequest, httpServletResponse);
    }

    @Override
    public String getContentType(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getContentType();
    }

    @Override
    public String getServletPath(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getServletPath();
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
    public String getQueryString(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getQueryString();
    }

    @Override
    public byte[] getRequestBytes(HttpServletRequest httpServletRequest) {
        return ((CachedBodyRequestWrapperV5) httpServletRequest).getContentAsByteArray();
    }

    @Override
    public byte[] getResponseBytes(HttpServletResponse httpServletResponse) {
        return ((CachedBodyResponseWrapperV5) httpServletResponse).getContentAsByteArray();
    }
}

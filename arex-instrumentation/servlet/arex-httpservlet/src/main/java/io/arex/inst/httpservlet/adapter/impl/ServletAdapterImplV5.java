package io.arex.inst.httpservlet.adapter.impl;

import io.arex.agent.bootstrap.util.IOUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpservlet.SpringUtil;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.listener.ServletAsyncListenerV5;
import io.arex.inst.httpservlet.wrapper.CachedBodyRequestWrapperV5;
import io.arex.inst.httpservlet.wrapper.CachedBodyResponseWrapperV5;
import io.arex.inst.runtime.model.ArexConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        if (httpServletRequest instanceof CachedBodyRequestWrapperV5) {
            return httpServletRequest;
        }
        return new CachedBodyRequestWrapperV5(httpServletRequest);
    }

    @Override
    public HttpServletResponse wrapResponse(HttpServletResponse httpServletResponse) {
        if (httpServletResponse instanceof CachedBodyResponseWrapperV5) {
            return httpServletResponse;
        }
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
        if (httpServletResponse instanceof CachedBodyResponseWrapperV5) {
            ((CachedBodyResponseWrapperV5) httpServletResponse).copyBodyToResponse();
        }
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
    public String getFullUrl(HttpServletRequest httpServletRequest) {
        String queryString = httpServletRequest.getQueryString();

        if (StringUtil.isEmpty(queryString)) {
            return httpServletRequest.getRequestURL().toString();
        } else {
            return new StringBuilder(httpServletRequest.getRequestURL()).append('?').append(queryString).toString();
        }
    }

    @Override
    public String getRequestPath(HttpServletRequest httpServletRequest) {
        String queryString = httpServletRequest.getQueryString();

        if (StringUtil.isEmpty(queryString)) {
            return httpServletRequest.getRequestURI();
        } else {
            return new StringBuilder(httpServletRequest.getRequestURI()).append('?').append(queryString).toString();
        }
    }

    @Override
    public String getRequestURI(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getRequestURI();
    }

    @Override
    public String getPattern(HttpServletRequest httpServletRequest) {
        // in org.springframework.web.servlet.DispatcherServlet#doService set pattern attribute
        Object pattern = httpServletRequest.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern");
        if (pattern != null) {
            return String.valueOf(pattern);
        }
        /*
         * if not get pattern attribute from request, try to get pattern from request mapping
         * maybe called in filter before DispatcherServlet#doService (filter -> service)
         */
        String patternStr = getPatternFromRequestMapping(httpServletRequest);
        if (StringUtil.isNotEmpty(patternStr)) {
            return patternStr;
        }
        final String requestURI = httpServletRequest.getRequestURI();
        if (StringUtil.isNotEmpty(httpServletRequest.getContextPath()) && requestURI.contains(
                httpServletRequest.getContextPath())) {
            return requestURI.replace(httpServletRequest.getContextPath(), "");
        }
        return requestURI;
    }

    public String getPatternFromRequestMapping(HttpServletRequest httpServletRequest) {
        try {
            return SpringUtil.getPatternFromRequestMapping(httpServletRequest, httpServletRequest.getServletContext());
        } catch (Throwable ignore) {
            // ignore exception
        }
        return null;
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
        CachedBodyRequestWrapperV5 requestWrapper = (CachedBodyRequestWrapperV5) httpServletRequest;
        byte[] content =  requestWrapper.getContentAsByteArray();
        if (content.length > 0) {
            return content;
        }
        // read request body to cache
        if (httpServletRequest.getContentLength() > 0) {
            try {
                return IOUtils.copyToByteArray(requestWrapper.getInputStream());
            } catch (Exception ignore) {
                // ignore exception
            }
        }
        return content;
    }

    @Override
    public byte[] getResponseBytes(HttpServletResponse httpServletResponse) {
        return ((CachedBodyResponseWrapperV5) httpServletResponse).getContentAsByteArray();
    }

    @Override
    public HttpServletRequest asHttpServletRequest(Object servletRequest) {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            try {
                if (httpServletRequest.getCharacterEncoding() == null) {
                    httpServletRequest.setCharacterEncoding(StandardCharsets.UTF_8.name());
                }
            } catch (Exception e) {
                // ignore
            }
            return httpServletRequest;
        }
        return null;
    }

    @Override
    public HttpServletResponse asHttpServletResponse(Object servletResponse) {
        if (servletResponse instanceof HttpServletResponse) {
            return (HttpServletResponse) servletResponse;
        }
        return null;
    }

    @Override
    public boolean markProcessed(HttpServletRequest httpServletRequest, String mark) {
        if (httpServletRequest.getAttribute(mark) != null) {
            return true;
        }
        httpServletRequest.setAttribute(mark, Boolean.TRUE);
        return false;
    }

    @Override
    public String getQueryString(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getQueryString();
    }

    @Override
    public String getServletVersion() {
        return ArexConstants.SERVLET_V5;
    }
}

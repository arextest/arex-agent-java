package io.arex.inst.httpclient.apache.common;

import io.arex.agent.bootstrap.util.IOUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.httpclient.common.HttpResponseWrapper.StringTuple;
import io.arex.inst.runtime.log.LogManager;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApacheHttpClientAdapter implements HttpClientAdapter<HttpRequest, HttpResponse> {
    private final HttpUriRequest httpRequest;

    public ApacheHttpClientAdapter(HttpRequest httpRequest) {
        this.httpRequest = (HttpUriRequest) httpRequest;
        wrapHttpEntity(httpRequest);
    }

    @Override
    public String getMethod() {
        return this.httpRequest.getMethod();
    }

    @Override
    public byte[] getRequestBytes() {
        HttpEntityEnclosingRequest enclosingRequest = enclosingRequest(httpRequest);
        if (enclosingRequest == null) {
            return ZERO_BYTE;
        }
        HttpEntity entity = enclosingRequest.getEntity();
        if (entity == null) {
            return ZERO_BYTE;
        }
        if (entity instanceof CachedHttpEntityWrapper) {
            return ((CachedHttpEntityWrapper) entity).getCachedBody();
        }
        try {
            return IOUtils.copyToByteArray(entity.getContent());
        } catch (Exception e) {
            LogManager.warn("copyToByteArray", "getRequestBytes error, uri: " + getUri(), e);
            return ZERO_BYTE;
        }
    }

    @Override
    public String getRequestContentType() {
        return this.getRequestHeader(CONTENT_TYPE_NAME);
    }

    @Override
    public String getRequestHeader(String name) {
        Header header = this.httpRequest.getFirstHeader(name);
        return header == null ? null : header.getValue();
    }

    @Override
    public URI getUri() {
        return this.httpRequest.getURI();
    }


    @Override
    public HttpResponseWrapper wrap(HttpResponse response) {
        HttpEntity httpEntity = response.getEntity();
        if (!check(httpEntity)) {
            return null;
        }

        byte[] responseBody;
        try {
            responseBody = IOUtils.copyToByteArray(httpEntity.getContent());
        } catch (Exception e) {
            LogManager.warn("copyToByteArray", "getResponseBody error, uri: " + getUri(), e);
            return null;
        }

        if (httpEntity instanceof BasicHttpEntity) {
            ((BasicHttpEntity) httpEntity).setContent(new ByteArrayInputStream(responseBody));
            response.setEntity(httpEntity);
        } else if (httpEntity instanceof HttpEntityWrapper) {
            // Output response normally now, later need to check revert DecompressingEntity
            BasicHttpEntity entity = ApacheHttpClientHelper.createHttpEntity(response);
            entity.setContent(new ByteArrayInputStream(responseBody));
            response.setEntity(entity);
        }

        Locale locale = response.getLocale();
        List<HttpResponseWrapper.StringTuple> headers = new ArrayList<>();
        for (Header header : response.getAllHeaders()) {
            if (StringUtil.isEmpty(header.getName())) {
                continue;
            }
            headers.add(new HttpResponseWrapper.StringTuple(header.getName(), header.getValue()));
        }

        return new HttpResponseWrapper(response.getStatusLine().toString(), responseBody,
            new HttpResponseWrapper.StringTuple(locale.getLanguage(), locale.getCountry()),
            headers);
    }

    @Override
    public HttpResponse unwrap(HttpResponseWrapper wrapped) {
        StatusLine statusLine = ApacheHttpClientHelper.parseStatusLine(wrapped.getStatusLine());
        HttpResponse response = new CloseableHttpResponseProxy(statusLine);
        response.setLocale(new Locale(wrapped.getLocale().name(), wrapped.getLocale().value()));
        appendHeaders(response, wrapped.getHeaders());
        // Output response normally now, later need to check revert DecompressingEntity
        BasicHttpEntity entity = ApacheHttpClientHelper.createHttpEntity(response);
        entity.setContent(new ByteArrayInputStream(wrapped.getContent()));
        entity.setContentLength(wrapped.getContent().length);
        response.setEntity(entity);

        return response;
    }

    private static void appendHeaders(HttpResponse response, List<StringTuple> headers) {
        for (int i = 0; i < headers.size(); i++) {
            StringTuple header = headers.get(i);
            response.addHeader(header.name(), header.value());
        }
    }

    private static boolean check(HttpEntity entity) {
        return entity instanceof BasicHttpEntity || entity instanceof HttpEntityWrapper;
    }

    public boolean skipRemoteStorageRequest() {
        return ignoreUserAgent(this.getRequestHeader(USER_AGENT_NAME));
    }

    private static boolean ignoreUserAgent(String userAgent) {
        return userAgent != null && userAgent.contains("arex");
    }

    private void wrapHttpEntity(HttpRequest httpRequest) {
        HttpEntityEnclosingRequest enclosingRequest = enclosingRequest(httpRequest);
        if (enclosingRequest == null) {
            return;
        }
        HttpEntity entity = enclosingRequest.getEntity();
        if (entity == null || entity.isRepeatable()) {
            return;
        }
        try {
            enclosingRequest.setEntity(new CachedHttpEntityWrapper(entity));
        } catch (Exception ignore) {
            // ignore exception
        }
    }

    private HttpEntityEnclosingRequest enclosingRequest(HttpRequest httpRequest) {
        if (httpRequest instanceof HttpEntityEnclosingRequest) {
            return (HttpEntityEnclosingRequest) httpRequest;
        }
        return null;
    }
}

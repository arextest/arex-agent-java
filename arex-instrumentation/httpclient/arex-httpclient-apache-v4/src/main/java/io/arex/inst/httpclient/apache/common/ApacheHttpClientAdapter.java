package io.arex.inst.httpclient.apache.common;

import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.httpclient.common.HttpResponseWrapper.StringTuple;
import io.arex.inst.runtime.log.LogManager;
import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.http.util.EntityUtils;

public class ApacheHttpClientAdapter implements HttpClientAdapter<HttpRequest, HttpResponse> {
    private final HttpUriRequest httpRequest;

    public ApacheHttpClientAdapter(HttpRequest httpRequest) {
        this.httpRequest = (HttpUriRequest) httpRequest;
    }

    @Override
    public String getMethod() {
        return this.httpRequest.getMethod();
    }

    @Override
    public byte[] getRequestBytes() {
        if (!(httpRequest instanceof HttpEntityEnclosingRequest)) {
            return ZERO_BYTE;
        }
        HttpEntityEnclosingRequest enclosingRequest = (HttpEntityEnclosingRequest) httpRequest;
        if (enclosingRequest.getEntity() == null) {
            return ZERO_BYTE;
        }

        bufferRequestEntity(enclosingRequest);

        return getEntityBytes(enclosingRequest.getEntity());
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
        Header[] responseHeaders = response.getAllHeaders();
        if (ArrayUtils.isEmpty(responseHeaders)) {
            return null;
        }

        List<StringTuple> headers = new ArrayList<>(responseHeaders.length);
        for (Header header : responseHeaders) {
            if (StringUtil.isEmpty(header.getName())) {
                continue;
            }
            headers.add(new StringTuple(header.getName(), header.getValue()));
        }

        Locale locale = response.getLocale();

        if (!check(response.getEntity())) {
            return new HttpResponseWrapper(response.getStatusLine().toString(), null,
                new StringTuple(locale.getLanguage(), locale.getCountry()), headers);
        }

        // Compatible with org.apache.http.impl.client.InternalHttpClient.doExecute
        bufferResponseEntity(response);

        byte[] responseBody = getEntityBytes(response.getEntity());
        return new HttpResponseWrapper(response.getStatusLine().toString(), responseBody,
            new StringTuple(locale.getLanguage(), locale.getCountry()), headers);
    }

    @Override
    public HttpResponse unwrap(HttpResponseWrapper wrapped) {
        StatusLine statusLine = ApacheHttpClientHelper.parseStatusLine(wrapped.getStatusLine());
        HttpResponse response = new CloseableHttpResponseProxy(statusLine);
        response.setLocale(new Locale(wrapped.getLocale().name(), wrapped.getLocale().value()));
        appendHeaders(response, wrapped.getHeaders());
        // Output response normally now, later need to check revert DecompressingEntity
        if (wrapped.getContent() != null) {
            BasicHttpEntity entity = ApacheHttpClientHelper.createHttpEntity(response);
            entity.setContent(new ByteArrayInputStream(wrapped.getContent()));
            entity.setContentLength(wrapped.getContent().length);
            response.setEntity(entity);
        }

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

    public static void bufferRequestEntity(HttpEntityEnclosingRequest enclosingRequest) {
        if (enclosingRequest.getEntity() == null || enclosingRequest.getEntity() instanceof BufferedHttpEntity) {
            return;
        }
        try {
            enclosingRequest.setEntity(new ArexBufferedHttpEntity(enclosingRequest.getEntity()));
        } catch (Exception ignore) {
            // ignore exception
        }
    }

    public static void bufferResponseEntity(HttpResponse response) {
        if (response.getEntity() == null || response.getEntity() instanceof BufferedHttpEntity) {
            return;
        }
        try {
            EntityUtils.updateEntity(response, new ArexBufferedHttpEntity(response.getEntity()));
        } catch (Exception e) {
            // ignore exception
        }
    }

    private byte[] getEntityBytes(HttpEntity entity) {
        if (!(entity instanceof BufferedHttpEntity)) {
            return ZERO_BYTE;
        }
        try {
            return EntityUtils.toByteArray(entity);
        } catch (IOException e) {
            LogManager.warn("AHC.getEntityBytes", "getEntityBytes error, uri: " + getUri(), e);
            return ZERO_BYTE;
        }
    }
}

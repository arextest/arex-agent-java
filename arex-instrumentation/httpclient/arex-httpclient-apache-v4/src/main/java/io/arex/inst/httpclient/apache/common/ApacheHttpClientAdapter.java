package io.arex.inst.httpclient.apache.common;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.IOUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.httpclient.common.HttpResponseWrapper.StringTuple;
import io.arex.inst.runtime.log.LogManager;
import java.io.ByteArrayOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
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
        // getContent will throw UnsupportedOperationException
        if (entity instanceof GzipCompressingEntity) {
            return writeTo((GzipCompressingEntity) entity);
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

        List<HttpResponseWrapper.StringTuple> headers = new ArrayList<>(response.getAllHeaders().length);
        for (Header header : response.getAllHeaders()) {
            if (StringUtil.isEmpty(header.getName())) {
                continue;
            }
            headers.add(new HttpResponseWrapper.StringTuple(header.getName(), header.getValue()));
        }

        HttpEntity httpEntity = response.getEntity();
        Locale locale = response.getLocale();

        if (!check(httpEntity)) {
            return buildEmptyBodyResponseWrapper(response.getStatusLine().toString(), locale, headers);
        }

        byte[] responseBody;
        try {
            responseBody = IOUtils.copyToByteArray(httpEntity.getContent());
            // For release connection, see PoolingHttpClientConnectionManager#requestConnection,releaseConnection
            EntityUtils.consumeQuietly(httpEntity);
        } catch (Exception e) {
            LogManager.warn("AHC.wrap", "AHC copyToByteArray error, uri: " + getUri(), e);
            return buildEmptyBodyResponseWrapper(response.getStatusLine().toString(), locale, headers);
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

        return new HttpResponseWrapper(response.getStatusLine().toString(), responseBody,
            new HttpResponseWrapper.StringTuple(locale.getLanguage(), locale.getCountry()), headers);
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

    private HttpResponseWrapper buildEmptyBodyResponseWrapper(String statusLine, Locale locale,
        List<StringTuple> headers) {
        if (CollectionUtil.isEmpty(headers)) {
            LogManager.warn("AHC.wrap", "AHC response wrap failed, uri: " + getUri());
            return null;
        }
        return new HttpResponseWrapper(statusLine, null,
            new HttpResponseWrapper.StringTuple(locale.getLanguage(), locale.getCountry()), headers);
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

    public static void wrapHttpEntity(HttpRequest httpRequest) {
        HttpEntityEnclosingRequest enclosingRequest = enclosingRequest(httpRequest);
        if (enclosingRequest == null) {
            return;
        }
        HttpEntity entity = enclosingRequest.getEntity();
        if (entity == null || entity.isRepeatable() || entity instanceof CachedHttpEntityWrapper) {
            return;
        }
        try {
            enclosingRequest.setEntity(new CachedHttpEntityWrapper(entity));
        } catch (Exception ignore) {
            // ignore exception
        }
    }

    private static HttpEntityEnclosingRequest enclosingRequest(HttpRequest httpRequest) {
        if (httpRequest instanceof HttpEntityEnclosingRequest) {
            return (HttpEntityEnclosingRequest) httpRequest;
        }
        return null;
    }

    private byte[] writeTo(GzipCompressingEntity entity) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            entity.writeTo(out);
            return out.toByteArray();
        } catch (Exception e) {
            LogManager.warn("writeTo", "getRequestBytes error, uri: " + getUri(), e);
            return ZERO_BYTE;
        }
    }
}

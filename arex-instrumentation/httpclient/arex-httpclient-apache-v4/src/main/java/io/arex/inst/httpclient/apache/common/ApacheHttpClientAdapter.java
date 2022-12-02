package io.arex.inst.httpclient.apache.common;

import io.arex.foundation.model.MockResult;
import io.arex.foundation.util.StringUtil;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.httpclient.common.HttpResponseWrapper.StringTuple;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApacheHttpClientAdapter implements HttpClientAdapter<HttpRequest, MockResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientAdapter.class);
    private final HttpRequestBase httpRequest;

    public ApacheHttpClientAdapter(HttpRequest httpRequest) {
        this.httpRequest = (HttpRequestBase) httpRequest;
    }

    @Override
    public String getMethod() {
        return this.httpRequest.getMethod();
    }

    @Override
    public byte[] getRequestBytes() {
        if (!(this.httpRequest instanceof HttpEntityEnclosingRequestBase)) {
            return null;
        }
        HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase;
        httpEntityEnclosingRequestBase = (HttpEntityEnclosingRequestBase) this.httpRequest;
        HttpEntity entity = httpEntityEnclosingRequestBase.getEntity();
        byte[] content;
        try {
            content = EntityUtils.toByteArray(entity);
        } catch (IOException e) {
            LOGGER.warn("extract request content error:{}", e.getMessage(), e);
            return null;
        }
        httpEntityEnclosingRequestBase.setEntity(new ByteArrayEntity(content));
        return content;
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
    public HttpResponseWrapper wrap(MockResult mockResult) {
        HttpResponse response = (HttpResponse) mockResult.getMockResult();
        HttpEntity httpEntity = response.getEntity();
        if (!check(httpEntity)) {
            return null;
        }

        byte[] content;
        try (InputStream stream = httpEntity.getContent()) {
            content = ApacheHttpClientHelper.readInputStream(stream);
        } catch (Exception ex) {
            LOGGER.warn("read content error:{}", ex.getMessage(), ex);
            return null;
        }

        if (httpEntity instanceof BasicHttpEntity) {
            ((BasicHttpEntity) httpEntity).setContent(new ByteArrayInputStream(content));
            response.setEntity(httpEntity);
        } else if (httpEntity instanceof HttpEntityWrapper) {
            BasicHttpEntity entity = ApacheHttpClientHelper.createHttpEntity(response);
            entity.setContent(new ByteArrayInputStream(content));
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

        return new HttpResponseWrapper(response.getStatusLine().toString(), content,
                new HttpResponseWrapper.StringTuple(locale.getLanguage(), locale.getCountry()),
                headers, null);
    }

    @Override
    public MockResult unwrap(HttpResponseWrapper wrapped) {
        HttpResponse response = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(
                ApacheHttpClientHelper.parseStatusLine(wrapped.getStatusLine()), null);
        response.setLocale(new Locale(wrapped.getLocale().name(), wrapped.getLocale().value()));
        appendHeaders(response, wrapped.getHeaders());
        BasicHttpEntity entity = ApacheHttpClientHelper.createHttpEntity(response);
        entity.setContent(new ByteArrayInputStream(wrapped.getContent()));
        entity.setContentLength(wrapped.getContent().length);
        response.setEntity(entity);

        return MockResult.of(wrapped.isIgnoreMockResult(), response);
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

}
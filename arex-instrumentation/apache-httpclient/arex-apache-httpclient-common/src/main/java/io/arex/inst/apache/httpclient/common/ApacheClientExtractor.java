package io.arex.inst.apache.httpclient.common;

import io.arex.foundation.model.HttpClientMocker;
import io.arex.foundation.util.LogUtil;
import io.arex.foundation.util.StringUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

import java.util.Base64;

public class ApacheClientExtractor {
    private final HttpRequest httpRequest;
    private final String target;
    private String request;
    private String contentType;

    public ApacheClientExtractor(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
        this.target = ApacheHttpClientHelper.calculateTarget(httpRequest);

        produceRequest(httpRequest);
    }

    private void produceRequest(HttpRequest request) {
        if (request instanceof HttpPost) {
            try {
                HttpPost post = (HttpPost) request;
                Header header = post.getFirstHeader("Content-Type");
                if (header != null) {
                    contentType = header.getValue();
                }

                HttpEntity entity = post.getEntity();
                byte[] content = EntityUtils.toByteArray(entity);
                this.request = Base64.getEncoder().encodeToString(content);
                LogUtil.warn("AREX content-type:" + this.contentType + ", Content:" + new String(content));
                post.setEntity(new ByteArrayEntity(content));
            } catch (Exception ex) {
                LogUtil.warn("extract request content failed.", ex);
            }
        }
    }

    public void record(HttpResponse response) {
        if (!isMockEnabled()) {
            return;
        }

        HttpResponseWrapper wrapped = HttpResponseWrapper.of(response);
        if (wrapped == null) {
            return;
        }

        (new HttpClientMocker(this.target, contentType, request, wrapped)).record();
    }

    public void record(Exception exception) {
        if (!isMockEnabled()) {
            return;
        }

        HttpResponseWrapper wrapped = HttpResponseWrapper.of(new ExceptionWrapper(exception));
        if (wrapped == null) {
            return;
        }

        (new HttpClientMocker(this.target, contentType, request, wrapped)).record();
    }

    public HttpResponse replay() throws ClientProtocolException {
        HttpResponseWrapper wrapped = mock();
        if (wrapped == null) {
            LogUtil.warn("mock data failed.");
            throw new ClientProtocolException(new ArexDataException("mock data failed."));
        }

        ExceptionWrapper exception = wrapped.getException();
        if (exception != null) {
            throw new ClientProtocolException(exception.getOriginalException());
        }
        return HttpResponseWrapper.to(wrapped);
    }

    public HttpResponseWrapper mock() {
        try {
            HttpClientMocker mocker = new HttpClientMocker(this.target, this.contentType, this.request);
            return (HttpResponseWrapper) mocker.replay();
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean isMockEnabled() {
        if (ignoreUserAgent()) {
            return false;
        }
        return true;
    }

    private boolean ignoreUserAgent(){
        Header userAgent = this.httpRequest.getFirstHeader("User-Agent");
        if (userAgent == null || StringUtil.isEmpty(userAgent.getValue())) {
            return false;
        }

        return userAgent.getValue().contains("arex");
    }
}

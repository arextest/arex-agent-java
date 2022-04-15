package io.arex.inst.apache.httpclient.common;

import io.arex.foundation.util.LogUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.DefaultHttpResponseFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class HttpResponseWrapper {

    @JsonProperty("statusLine")
    private String statusLine;
    @JsonProperty("content")
    private byte[] content;
    @JsonProperty("locale")
    private StringTuple locale;
    @JsonProperty("headers")
    private List<StringTuple> headers;
    @JsonProperty("exceptionWrapper")
    private ExceptionWrapper exceptionWrapper;

    public ExceptionWrapper getException() {
        return exceptionWrapper;
    }

    public void setException(ExceptionWrapper exception) {
        this.exceptionWrapper = exception;
    }

    public HttpResponseWrapper() {
    }

    public HttpResponseWrapper(String statusLine, byte[] content, StringTuple locale, List<StringTuple> headers,
                               ExceptionWrapper exception) {
        this.statusLine = statusLine;
        this.content = content;
        this.locale = locale;
        this.headers = headers;
        this.exceptionWrapper = exception;
    }

    public static class StringTuple {
        @JsonProperty("f")
        String f;
        @JsonProperty("s")
        String s;

        public StringTuple() {
            this(null, null);
        }

        public StringTuple(String first, String second) {
            this.f = first;
            this.s = second;
        }
    }

    // todo: The following code is later considered to be moved into ApacheClientExtractor
    public static HttpResponseWrapper of(HttpResponse response) {
        HttpEntity httpEntity = response.getEntity();
        if (!check(httpEntity)) {
            return null;
        }

        byte[] content = null;
        try (InputStream stream = httpEntity.getContent()) {
            content = ApacheHttpClientHelper.readInputStream(stream);
        } catch (Exception ex) {
            LogUtil.warn("read content failed.", ex);
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
            headers.add(new HttpResponseWrapper.StringTuple(header.getName(), header.getValue()));
        }

        return new HttpResponseWrapper(response.getStatusLine().toString(), content,
                new HttpResponseWrapper.StringTuple(locale.getLanguage(), locale.getCountry()),
                headers, null);
    }

    public static HttpResponseWrapper of(ExceptionWrapper exception) {
        HttpResponseWrapper wrapper = new HttpResponseWrapper();
        wrapper.exceptionWrapper = exception;
        return wrapper;
    }

    public static HttpResponse to(HttpResponseWrapper wrapper) {
        HttpResponse response = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(
                ApacheHttpClientHelper.parseStatusLine(wrapper.statusLine), null);
        response.setLocale(new Locale(wrapper.locale.f, wrapper.locale.s));
        appendHeaders(response, wrapper.headers);

        BasicHttpEntity entity = ApacheHttpClientHelper.createHttpEntity(response);
        entity.setContent(new ByteArrayInputStream(wrapper.content));
        entity.setContentLength(wrapper.content.length);
        response.setEntity(entity);

        return response;
    }

    private static void appendHeaders(HttpResponse response, List<StringTuple> headers) {
        for (int i = 0; i < headers.size(); i++) {
            StringTuple header = headers.get(i);
            response.addHeader(header.f, header.s);
        }
    }

    private static boolean check(HttpEntity entity) {
        return entity instanceof BasicHttpEntity || entity instanceof HttpEntityWrapper;
    }
}


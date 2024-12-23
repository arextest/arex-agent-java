package io.arex.inst.httpclient.ning;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.cookie.CookieDecoder;
import com.ning.http.client.uri.Uri;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import org.jboss.netty.handler.codec.http.HttpHeaders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

public class ResponseWrapper implements Response {
    private final String statusLine;
    private final byte[] content;
    private final FluentCaseInsensitiveStringsMap headers;
    private final int statusCode;

    public ResponseWrapper(HttpResponseWrapper httpResponseWrapper) {
        this.statusLine = httpResponseWrapper.getStatusLine();
        this.content = httpResponseWrapper.getContent();
        List<HttpResponseWrapper.StringTuple> wrapperHeaders = httpResponseWrapper.getHeaders();
        this.headers = new FluentCaseInsensitiveStringsMap();
        for (HttpResponseWrapper.StringTuple header : wrapperHeaders) {
            headers.put(header.getF(), StringUtil.splitToList(header.getS(), ','));
        }
        this.statusCode = httpResponseWrapper.getStatusCode();
    }
    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getStatusText() {
        return this.statusLine;
    }

    @Override
    public byte[] getResponseBodyAsBytes() throws IOException {
        return this.content;
    }

    @Override
    public ByteBuffer getResponseBodyAsByteBuffer() throws IOException {
        return ByteBuffer.wrap(this.content);
    }

    @Override
    public InputStream getResponseBodyAsStream() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public String getResponseBodyExcerpt(int maxLength, String charset) throws IOException {
        String response = getResponseBody(charset);
        return response.length() <= maxLength ? response : response.substring(0, maxLength);
    }

    @Override
    public String getResponseBody(String charset) throws IOException {
        if (charset == null) {
            return getResponseBody();
        }
        return new String(this.content, charset);
    }

    @Override
    public String getResponseBodyExcerpt(int maxLength) throws IOException {
        return getResponseBodyExcerpt(maxLength, null);
    }

    @Override
    public String getResponseBody() throws IOException {
        return new String(this.content);
    }

    @Override
    public Uri getUri() {
        return null;
    }

    @Override
    public String getContentType() {
        return headers != null ? getHeader("Content-Type") : null;
    }

    @Override
    public String getHeader(String name) {
        return headers != null ? getHeaders().getFirstValue(name) : null;
    }

    @Override
    public List<String> getHeaders(String name) {
        return this.headers.get(name);
    }

    @Override
    public FluentCaseInsensitiveStringsMap getHeaders() {
        return this.headers;
    }

    @Override
    public boolean isRedirected() {
        switch (this.statusCode) {
            case 301:
            case 302:
            case 303:
            case 307:
            case 308:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<Cookie> getCookies() {
        List<Cookie> cookies = new ArrayList<>();
        for (Map.Entry<String, List<String>> header : this.headers.entrySet()) {
            if (header.getKey().equalsIgnoreCase(HttpHeaders.Names.SET_COOKIE)) {
                List<String> v = header.getValue();
                for (String value : v) {
                    cookies.add(CookieDecoder.decode(value));
                }
            }
        }
        return cookies;
    }

    @Override
    public boolean hasResponseStatus() {
        return this.statusCode != 0;
    }

    @Override
    public boolean hasResponseHeaders() {
        return MapUtils.isNotEmpty(this.headers);
    }

    @Override
    public boolean hasResponseBody() {
        return this.content != null && this.content.length > 0;
    }
}

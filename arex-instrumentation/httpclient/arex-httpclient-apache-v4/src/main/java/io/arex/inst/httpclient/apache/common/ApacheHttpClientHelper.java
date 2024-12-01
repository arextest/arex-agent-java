package io.arex.inst.httpclient.apache.common;

import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.IgnoreUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;

import java.net.URI;
import java.util.Set;

public class ApacheHttpClientHelper {

    public static BasicHttpEntity createHttpEntity(HttpResponse response) {
        final BasicHttpEntity entity = new BasicHttpEntity();
        final Header contentTypeHeader = response.getFirstHeader(HTTP.CONTENT_TYPE);
        if (contentTypeHeader != null) {
            entity.setContentType(contentTypeHeader);
        }
        final Header contentEncodingHeader = response.getFirstHeader(HTTP.CONTENT_ENCODING);
        if (contentEncodingHeader != null) {
            entity.setContentEncoding(contentEncodingHeader);
        }
        return entity;
    }

    public static CharArrayBuffer convertToBuffer(String value) {
        byte[] bytes = value.getBytes();
        final CharArrayBuffer buffer = new CharArrayBuffer(bytes.length);
        buffer.append(bytes, 0, bytes.length);
        return buffer;
    }

    public static StatusLine parseStatusLine(String statusLine) {
        final CharArrayBuffer buffer = convertToBuffer(statusLine);
        final ParserCursor cursor = new ParserCursor(0, buffer.length());
        return BasicLineParser.INSTANCE.parseStatusLine(buffer, cursor);
    }

    public static boolean ignoreRequest(HttpRequest httpRequest) {
        if (!(httpRequest instanceof HttpUriRequest)) {
            return true;
        }
        try {
            // multi app replay request should real call
            URI uri = ((HttpUriRequest) httpRequest).getURI();
            Object multiAppReplayUrlObj = ContextManager.getAttachment(ArexConstants.MULTI_APP_URL);
            if (multiAppReplayUrlObj != null) {
                for (String multiAppUrl : ((Set<String>) multiAppReplayUrlObj)) {
                    if (multiAppUrl != null && multiAppUrl.contains(uri.getHost())) {
                        return true;
                    }
                }
            }

            return IgnoreUtils.excludeOperation(uri.getPath());
        } catch (Throwable e) {
            return true;
        }
    }
}
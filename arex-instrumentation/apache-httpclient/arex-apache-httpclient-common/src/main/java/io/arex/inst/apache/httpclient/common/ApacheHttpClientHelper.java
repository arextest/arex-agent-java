package io.arex.inst.apache.httpclient.common;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

public class ApacheHttpClientHelper {
    public static String calculateTarget(HttpRequest request) {
        try {
            URI uri = new URI(request.getRequestLine().getUri());
            return request.getRequestLine().getMethod() + " " + uri.getPath();
        } catch (Exception ex) {
            return null;
        }
    }

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

    public static byte[] readInputStream(InputStream in) throws IOException {
        byte[] buffer = new byte[0];

        int read;
        int stepSize = 1024;
        for(int i = 0; i < Integer.MAX_VALUE; i += read) {
            if (i >= buffer.length) {
                if (buffer.length < i + stepSize) {
                    buffer = Arrays.copyOf(buffer, i + stepSize);
                }
            } else {
                stepSize = buffer.length - i;
            }

            read = in.read(buffer, i, stepSize);
            if (read < 0) {
                if (buffer.length != i) {
                    buffer = Arrays.copyOf(buffer, i);
                }
                break;
            }
        }

        return buffer;
    }
}

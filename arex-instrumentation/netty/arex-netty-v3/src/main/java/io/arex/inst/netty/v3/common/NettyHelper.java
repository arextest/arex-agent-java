package io.arex.inst.netty.v3.common;

import io.arex.agent.bootstrap.util.ReflectUtil;
import io.arex.inst.runtime.util.fastreflect.MethodHolder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.util.CharsetUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NettyHelper {
    /**
     * determine the Netty version used by the user
     */
    private static final Method GET_HEADER_METHOD = ReflectUtil.getMethod(HttpMessage.class, "getHeader", String.class);
    private static MethodHolder<String> getHeaderHolder = null;
    private static MethodHolder<HttpHeaders> headersHolder = null;
    private static MethodHolder<Void> setHeaderHolder = null;
    private static MethodHolder<List<Map.Entry<String, String>>> getHeadersHolder = null;

    public static Map<String, String> parseHeaders(List<Map.Entry<String, String>> headerList) {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, String> entry : headerList) {
            headers.put(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    public static String parseBody(ChannelBuffer buffer) {
        if (buffer != null && buffer.readableBytes() > 0) {
            byte[] msgByte = new byte[buffer.readableBytes()];
            buffer.readBytes(msgByte);
            buffer.resetReaderIndex();
            return new String(msgByte, CharsetUtil.UTF_8);
        }
        return null;
    }

    /**
     * < 3.10.0: message.getHeader()
     * >= 3.10.0: message.headers().get()
     */
    public static String getHeader(HttpMessage message, String name) {
        // direct call(< 3.10.0)
        if (GET_HEADER_METHOD != null) {
            return message.getHeader(name);
        }
        // reflect call(>= 3.10.0)
        HttpHeaders httpHeaders = getHttpHeaders(message);
        if (getHeaderHolder != null) {
            return getHeaderHolder.invoke(httpHeaders, name);
        }
        Method getMethod = ReflectUtil.getMethod(HttpHeaders.class, "get", String.class);
        getHeaderHolder = MethodHolder.build(getMethod);
        return getHeaderHolder.invoke(httpHeaders, name);
    }

    /**
     * < 3.10.0: message.setHeader()
     * >= 3.10.0: message.headers().set()
     */
    public static void setHeader(HttpMessage message, String name, Object value) {
        // direct call(< 3.10.0)
        if (GET_HEADER_METHOD != null) {
            message.setHeader(name, value);
            return;
        }

        // reflect call(>= 3.10.0)
        HttpHeaders httpHeaders = getHttpHeaders(message);
        if (setHeaderHolder != null) {
            setHeaderHolder.invoke(httpHeaders, name, value);
            return;
        }
        Method setMethod = ReflectUtil.getMethod(HttpHeaders.class, "set", String.class, Object.class);
        setHeaderHolder = MethodHolder.build(setMethod);
        setHeaderHolder.invoke(httpHeaders, name, value);
    }

    /**
     * < 3.10.0: message.getHeaders()
     * >= 3.10.0: message.headers().entries()
     */
    public static List<Map.Entry<String, String>> getHeaders(HttpMessage message) {
        // direct call(< 3.10.0)
        if (GET_HEADER_METHOD != null) {
            return message.getHeaders();
        }

        // >= 3.10.0
        HttpHeaders httpHeaders = getHttpHeaders(message);
        if (getHeadersHolder != null) {
            return getHeadersHolder.invoke(httpHeaders);
        }
        Method entriesMethod = ReflectUtil.getMethod(HttpHeaders.class, "entries");
        getHeadersHolder = MethodHolder.build(entriesMethod);
        return getHeadersHolder.invoke(httpHeaders);
    }

    private static HttpHeaders getHttpHeaders(HttpMessage message) {
        if (headersHolder != null) {
            return headersHolder.invoke(message);
        }
        Method headersMethod = ReflectUtil.getMethod(HttpMessage.class, "headers");
        headersHolder = MethodHolder.build(headersMethod);
        return headersHolder.invoke(message);
    }
}

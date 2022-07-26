package io.arex.inst.netty.v4.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class NettyHelper {

    public static Map<String, String> parseHeaders(HttpHeaders originHeaders) {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, String> entry : originHeaders.entries()) {
            headers.put(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    public static String parseBody(ByteBuf byteData) {
        if (byteData instanceof EmptyByteBuf) {
            return null;
        }
        byte[] msgByte = new byte[byteData.readableBytes()];
        byteData.readBytes(msgByte);
        byteData.resetReaderIndex();
        return Base64.getEncoder().encodeToString(msgByte);
    }

}

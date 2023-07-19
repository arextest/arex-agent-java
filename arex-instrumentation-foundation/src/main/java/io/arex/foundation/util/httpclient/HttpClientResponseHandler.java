package io.arex.foundation.util.httpclient;

import io.arex.foundation.util.CompressUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

public interface HttpClientResponseHandler {
    String handle(HttpEntity httpEntity) throws IOException;

    class ZstdJsonHandler implements HttpClientResponseHandler {
        public static final ZstdJsonHandler INSTANCE = new ZstdJsonHandler();
        @Override
        public String handle(HttpEntity httpEntity) throws IOException {
            return CompressUtil.zstdDecompress(httpEntity.getContent(), StandardCharsets.UTF_8);
        }
    }

    class JsonHandler implements HttpClientResponseHandler {
        public static final JsonHandler INSTANCE = new JsonHandler();
        @Override
        public String handle(HttpEntity httpEntity) throws IOException {
            return EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        }
    }
}

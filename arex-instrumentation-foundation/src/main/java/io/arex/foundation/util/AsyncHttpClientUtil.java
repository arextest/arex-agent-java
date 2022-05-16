package io.arex.foundation.util;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.util.async.AutoCleanedPoolingNHttpClientConnectionManager;
import io.arex.foundation.util.async.ThreadFactoryImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * AsyncHttpClientUtil
 *
 *
 * @date 2021/11/09
 */
public class AsyncHttpClientUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncHttpClientUtil.class);
    private static final String USER_AGENT = String.format("arex-async-http-client-%s", ConfigManager.INSTANCE.getAgentVersion());

    private static CloseableHttpAsyncClient asyncClient;

    static {
        try {
            asyncClient = createAsyncClient();
            asyncClient.start();
        } catch (Throwable t) {
            LOGGER.warn("[[title=arex.AsyncHttpClientUtil.ctor]]", t);
        }
    }

    public static String executeSync(String urlAddress, String postData) {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");

        HttpEntity httpEntity = new ByteArrayEntity(postData.getBytes(StandardCharsets.UTF_8));

        return executeAsync(urlAddress, httpEntity, requestHeaders).join();
    }

    public static String executeSync(String urlAddress, String postData, String category) {
        return executeAsync(urlAddress, postData, category).join();
    }

    public static CompletableFuture<String> executeAsync(String urlAddress, String postData, String category) {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, ClientConfig.STORAGE_CONTENT_TYPE);
        requestHeaders.put("arex-mocker-category", category);

        HttpEntity httpEntity = new ByteArrayEntity(CompressUtil.zstdCompress(postData, StandardCharsets.UTF_8));
        return executeAsync(urlAddress, httpEntity, requestHeaders);
    }

    public static CompletableFuture<String> executeAsync(String urlAddress, HttpEntity httpEntity, Map<String, String> requestHeaders) {
        HttpPost httpPost = getHttpPost(urlAddress, httpEntity, requestHeaders);
        return executeAsync(httpPost).thenApply(response -> response.get("responseBody"));
    }

    public static CompletableFuture<Map<String, String>> executeAsyncIncludeHeader(String urlAddress, HttpEntity httpEntity, Map<String, String> requestHeaders) {
        HttpPost httpPost = getHttpPost(urlAddress, httpEntity, requestHeaders);
        return executeAsync(httpPost);
    }

    private static HttpPost getHttpPost(String urlAddress, HttpEntity httpEntity, Map<String, String> requestHeaders) {
        HttpPost httpPost = prepareHttpRequest(urlAddress, ClientConfig.DEFAULT_CONNECT_TIMEOUT, ClientConfig.DEFAULT_SOCKET_TIMEOUT);
        httpPost.setEntity(httpEntity);

        if (requestHeaders != null && requestHeaders.size() > 0) {
            requestHeaders.forEach(httpPost::addHeader);
        }
        return httpPost;
    }

    public static CompletableFuture<Map<String, String>> executeAsync(HttpPost httpPost) {
        CompletableFuture<Map<String, String>> resultFuture = new CompletableFuture<>();

        Function<byte[], String> bytesParser;

        if (ClientConfig.STORAGE_CONTENT_TYPE.equals(httpPost.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue())) {
            bytesParser = bytes -> CompressUtil.zstdDecompress(bytes, StandardCharsets.UTF_8);
        } else {
            bytesParser = bytes -> new String(bytes, StandardCharsets.UTF_8);
        }

        asyncClient.execute(httpPost, new ResponseCallback(resultFuture, bytesParser));

        return resultFuture;
    }

    private static CloseableHttpAsyncClient createAsyncClient() {
        RequestConfig defaultRequestConfig =
                createRequestConfig(ClientConfig.DEFAULT_CONNECT_TIMEOUT, ClientConfig.DEFAULT_SOCKET_TIMEOUT);

        AutoCleanedPoolingNHttpClientConnectionManager connectionManager =
                AutoCleanedPoolingNHttpClientConnectionManager.createDefault();

        asyncClient = HttpAsyncClients.custom()
                .setThreadFactory(new ThreadFactoryImpl("arex-async-http-client"))
                .setDefaultRequestConfig(defaultRequestConfig)
                .setConnectionManager(connectionManager).build();
        return asyncClient;
    }

    private static HttpPost prepareHttpRequest(String urlAddress, int connectTimeout, int socketTimeout) {
        HttpPost httpPost = new HttpPost(urlAddress);

        RequestConfig requestConfig = createRequestConfig(connectTimeout, socketTimeout);

        httpPost.setConfig(requestConfig);
        httpPost.addHeader(HttpHeaders.ACCEPT, "*");
        httpPost.addHeader(HttpHeaders.USER_AGENT, USER_AGENT);

        return httpPost;
    }

    private static RequestConfig createRequestConfig(int connectTimeout, int socketTimeout) {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(ClientConfig.DEFAULT_CONNECTION_REQUEST_TIMEOUT)
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout).build();
    }

    private static void close(HttpResponse response) {
        if (response == null) {
            return;
        }

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            EntityUtils.consumeQuietly(entity);
        }
    }

    static class ClientConfig {
        private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 5000;
        private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
        private static final int DEFAULT_SOCKET_TIMEOUT = 5000;

        private static final String STORAGE_CONTENT_TYPE = "application/zstd-json;charset=UTF-8";
    }

    private static class ResponseCallback implements FutureCallback<HttpResponse> {
        private final CompletableFuture<Map<String, String>> responseFuture;
        private final Map<String, String> contextMap;
        private final Function<byte[],String> byteParser;
        public ResponseCallback(CompletableFuture<Map<String, String>> responseFuture, Function<byte[], String> bytesParser) {
            this.responseFuture = responseFuture;
            this.byteParser = bytesParser;
            this.contextMap = MDC.getCopyOfContextMap();
        }

        @Override
        public void completed(HttpResponse response) {
            LogUtil.setContextMap(contextMap);
            String responseContent = null;
            Map<String, String> responseMap = new HashMap<>();
            if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                LOGGER.warn("[[title=arex.completed]]response status info: {}", response.getStatusLine().toString());
            } else {
                try {
                    byte[] responseBytes = EntityUtils.toByteArray(response.getEntity());
                    if (responseBytes != null) {
                        responseContent = byteParser.apply(responseBytes);
                        if (StringUtils.isEmpty(responseContent)) {
                            LOGGER.warn("[[title=arex.completed]]response bytes: {}", Base64.getEncoder().encodeToString(responseBytes));
                        }
                    }
                    Header[] headers = response.getAllHeaders();
                    if (headers != null && headers.length > 0) {
                        for (Header header : headers) {
                            responseMap.put(header.getName(), header.getValue());
                        }
                    }
                } catch (Throwable e) {
                    responseFuture.completeExceptionally(e);
                    LOGGER.warn("[[title=arex.completed]]", e);
                }
            }

            responseMap.put("responseBody", responseContent);
            responseFuture.complete(responseMap);
            close(response);
        }

        @Override
        public void failed(Exception e) {
            LogUtil.setContextMap(contextMap);
            responseFuture.completeExceptionally(e);
            LOGGER.warn("[[title=arex.failed]]", e);
        }

        @Override
        public void cancelled() {
            LogUtil.setContextMap(contextMap);
            responseFuture.completeExceptionally(new InterruptedException("Request has been cancelled."));
            LOGGER.warn("[[title=arex.cancelled]]Request has been cancelled.");
        }
    }
}

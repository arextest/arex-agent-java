package io.arex.foundation.util.httpclient;

import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.model.HttpClientResponse;
import io.arex.foundation.util.CompressUtil;
import io.arex.foundation.util.httpclient.async.AutoCleanedPoolingNHttpClientConnectionManager;
import io.arex.foundation.util.httpclient.async.ThreadFactoryImpl;
import io.arex.inst.runtime.log.LogManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AsyncHttpClientUtil
 *
 * @date 2021/11/09
 */
public class AsyncHttpClientUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncHttpClientUtil.class);
    private static final String USER_AGENT = String.format("arex-async-http-client-%s",
        ConfigManager.INSTANCE.getAgentVersion());
    /**
     * the compressed size of the sent httpEntity is limited to less than 5MB
     */
    private static final long RECORD_BODY_MAX_LIMIT_5MB = 5 * 1024L * 1024L;
    private static CloseableHttpAsyncClient asyncClient;
    private static final CompletableFuture<HttpClientResponse> EMPTY_RESPONSE = CompletableFuture.completedFuture(
        HttpClientResponse.emptyResponse());

    private AsyncHttpClientUtil() {
    }

    static {
        try {
            asyncClient = createAsyncClient();
            asyncClient.start();
        } catch (Exception t) {
            LOGGER.warn("[[title=arex.AsyncHttpClientUtil.ctor]]", t);
        }
    }

    public static CompletableFuture<HttpClientResponse> postAsyncWithJson(String uri, String postData,
        Map<String, String> requestHeaders) {
        HttpEntity httpEntity = new ByteArrayEntity(postData.getBytes(StandardCharsets.UTF_8));

        if (requestHeaders == null) {
            requestHeaders = MapUtils.newHashMapWithExpectedSize(1);
        }
        requestHeaders.putIfAbsent(HttpHeaders.CONTENT_TYPE, ClientConfig.APPLICATION_JSON);

        return executeAsync(uri, httpEntity, requestHeaders, HttpClientResponseHandler.JsonHandler.INSTANCE);
    }

    public static CompletableFuture<HttpClientResponse> postAsyncWithZstdJson(String uri, String postData,
        Map<String, String> requestHeaders) {
        HttpEntity httpEntity = new ByteArrayEntity(CompressUtil.zstdCompress(postData, StandardCharsets.UTF_8));

        if (requestHeaders == null) {
            requestHeaders = MapUtils.newHashMapWithExpectedSize(1);
        }
        requestHeaders.putIfAbsent(HttpHeaders.CONTENT_TYPE, ClientConfig.APPLICATION_ZSTD_JSON);

        return executeAsync(uri, httpEntity, requestHeaders, HttpClientResponseHandler.ZstdJsonHandler.INSTANCE);
    }

    public static CompletableFuture<HttpClientResponse> executeAsync(String uri, HttpEntity httpEntity,
        Map<String, String> requestHeaders, HttpClientResponseHandler responseHandler) {
        if (httpEntity.getContentLength() > RECORD_BODY_MAX_LIMIT_5MB || httpEntity.getContentLength() < 0) {
            LogManager.warn("executeAsync", "do not record, the size is larger than 5MB.");
            return EMPTY_RESPONSE;
        }

        HttpUriRequest httpPost = createHttpPost(uri, httpEntity, requestHeaders);
        CompletableFuture<HttpClientResponse> resultFuture = new CompletableFuture<>();
        asyncClient.execute(httpPost, new HttpClientResponseCallback(resultFuture, responseHandler));

        return resultFuture;
    }

    private static HttpUriRequest createHttpPost(String uri, HttpEntity httpEntity, Map<String, String> requestHeaders) {
        HttpPost httpPost = prepareHttpRequest(uri, ClientConfig.DEFAULT_CONNECT_TIMEOUT,
            ClientConfig.DEFAULT_SOCKET_TIMEOUT);
        httpPost.setEntity(httpEntity);

        if (requestHeaders != null && requestHeaders.size() > 0) {
            requestHeaders.forEach(httpPost::addHeader);
        }
        return httpPost;
    }

    private static HttpPost prepareHttpRequest(String uri, int connectTimeout, int socketTimeout) {
        HttpPost httpPost = new HttpPost(uri);

        RequestConfig requestConfig = createRequestConfig(connectTimeout, socketTimeout);

        httpPost.setConfig(requestConfig);
        httpPost.addHeader(HttpHeaders.ACCEPT, "*");
        httpPost.addHeader(HttpHeaders.USER_AGENT, USER_AGENT);

        return httpPost;
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

    private static RequestConfig createRequestConfig(int connectTimeout, int socketTimeout) {
        return RequestConfig.custom()
            .setConnectionRequestTimeout(ClientConfig.DEFAULT_CONNECTION_REQUEST_TIMEOUT)
            .setConnectTimeout(connectTimeout)
            .setSocketTimeout(socketTimeout).build();
    }

    static class ClientConfig {

        private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 5000;
        private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
        private static final int DEFAULT_SOCKET_TIMEOUT = 5000;
        private static final String APPLICATION_ZSTD_JSON = "application/zstd-json;charset=UTF-8";
        private static final String APPLICATION_JSON = "application/json;charset=UTF-8";
    }
}

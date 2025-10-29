package io.arex.foundation.services;

import com.google.auto.service.AutoService;
import com.google.common.collect.Maps;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.model.HttpClientResponse;
import io.arex.foundation.model.MockDataHead;
import io.arex.foundation.model.NextBuilderMockDataQueryRequest;
import io.arex.foundation.util.httpclient.AsyncHttpClientUtil;
import io.arex.inst.runtime.config.NextBuilderConfig;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.service.ExtensionLogService;
import io.arex.inst.runtime.service.NextBuilderDataCollector;
import io.arex.inst.runtime.service.NextBuilderExtensionParameterService;
import java.util.Map;
import java.util.UUID;

/**
 * NextBuilderService
 *
 * @author ywqiu
 * @date 2025/4/16 9:50
 */
@AutoService(NextBuilderDataCollector.class)
public class NextBuilderService implements NextBuilderDataCollector {

    public static final NextBuilderService INSTANCE = new NextBuilderService();
    private static final String LOGGER_TITLE = "AREX-NextBuilder";

    @Override
    public String query(
        String serviceUrl,
        String originRequestBody,
        String txId,
        String requestMethod) {
        try {
            long startTime = System.currentTimeMillis();
            NextBuilderMockDataQueryRequest request = buildRequest(serviceUrl,
                originRequestBody,
                txId,
                requestMethod);
            ExtensionLogService.getInstance().info(LOGGER_TITLE, "Query Mock Data, txid: " + txId);
            ExtensionLogService.getInstance().info(LOGGER_TITLE, "Query Mock Data, serviceUrl: " + serviceUrl);
            String requestBody = Serializer.serialize(request);
            HttpClientResponse clientResponse = AsyncHttpClientUtil.postAsyncWithJson(
                NextBuilderConfig.get().getMockDataQueryUri(),
                requestBody,
                null).join();
            Map<String, String> tagMaps = Maps.newHashMap();
            tagMaps.put("serviceUrl", serviceUrl);
            tagMaps.put("txId", txId);
            tagMaps.put("requestMethod", requestMethod);
            ExtensionLogService.getInstance().soaLog(LOGGER_TITLE, request, clientResponse, startTime, tagMaps);
            if (clientResponse == null || StringUtil.isEmpty(clientResponse.getBody())) {
                ExtensionLogService.getInstance().warn(LOGGER_TITLE, "Query Mock Data, response is null", tagMaps);
                return null;
            }
            return clientResponse.getBody();
        } catch (Throwable e) {
            ExtensionLogService.getInstance().warn(LOGGER_TITLE, e);
        }
        return null;
    }

    private static NextBuilderMockDataQueryRequest buildRequest(
        String serviceUrl,
        String originRequestBody,
        String txId,
        String requestMethod) {
        NextBuilderMockDataQueryRequest request = new NextBuilderMockDataQueryRequest();
        request.setRequestHead(new MockDataHead());
        request.getRequestHead().setAppId(ConfigManager.INSTANCE.getServiceName());
        request.getRequestHead().setSource("arex-java");
        request.getRequestHead().setTransactionID(UUID.randomUUID().toString());
        request.setServiceUrl(serviceUrl);
        request.setTxId(txId);
        request.setRequestType(requestMethod);
        request.setRequestContent(originRequestBody);
        if (NextBuilderExtensionParameterService.getInstance() != null) {

            request.setEnv(NextBuilderExtensionParameterService.getInstance().getEnvironment());
            request.getRequestHead().setClientIP(NextBuilderExtensionParameterService.getInstance().getClientIp());
            request.setTraceId(NextBuilderExtensionParameterService.getInstance().getMessageId());
            request.setParentTraceId(NextBuilderExtensionParameterService.getInstance().getParentMessageId());
        }
        return request;
    }

}

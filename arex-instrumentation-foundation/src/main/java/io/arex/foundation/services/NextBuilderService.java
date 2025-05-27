package io.arex.foundation.services;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.model.HttpClientResponse;
import io.arex.foundation.model.MockDataHead;
import io.arex.foundation.model.NextBuilderMockDataQueryRequest;
import io.arex.foundation.util.httpclient.AsyncHttpClientUtil;
import io.arex.inst.runtime.config.NextBuilderConfig;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.service.NextBuilderDataCollector;
import io.arex.inst.runtime.service.NextBuilderExtensionParameterService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NextBuilderService
 *
 * @author ywqiu
 * @date 2025/4/16 9:50
 */
@AutoService(NextBuilderDataCollector.class)
public class NextBuilderService implements NextBuilderDataCollector {

    public static final NextBuilderService INSTANCE = new NextBuilderService();

    private static final Logger LOGGER = LoggerFactory.getLogger(NextBuilderService.class);

    @Override
    public String query(
        String serviceUrl,
        String originRequestBody,
        String txId,
        String requestMethod) {
        try {
            NextBuilderMockDataQueryRequest request = buildRequest(serviceUrl,
                originRequestBody,
                txId,
                requestMethod);

            String requestBody = Serializer.serialize(request);
            LOGGER.info("[AREX-NextBuilder] Query Mock Data Request:{}", requestBody);
            HttpClientResponse clientResponse = AsyncHttpClientUtil.postAsyncWithJson(
                NextBuilderConfig.get().getMockDataQueryUri(),
                requestBody,
                null).join();

            if (clientResponse == null || StringUtil.isEmpty(clientResponse.getBody())) {
                LOGGER.warn("[AREX-NextBuilder] Query Mock Data, response is null");
                return null;
            }
            LOGGER.info("[AREX-NextBuilder] Query Mock Data Response:{}", clientResponse.getBody());
            return clientResponse.getBody();
        } catch (Throwable e) {
            LOGGER.warn("[AREX-NextBuilder] Query Mock Data error, exception message: {}", e.getMessage(), e);
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

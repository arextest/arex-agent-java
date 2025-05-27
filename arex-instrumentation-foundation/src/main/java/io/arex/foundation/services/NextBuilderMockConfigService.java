package io.arex.foundation.services;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.model.HttpClientResponse;
import io.arex.foundation.model.NextBuilderMockConfigRequest;
import io.arex.foundation.model.NextBuilderMockConfigResponse;
import io.arex.foundation.util.httpclient.AsyncHttpClientUtil;
import io.arex.inst.runtime.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NextBuilderMockConfigService
 *
 * @author ywqiu
 * @date 2025/4/23 16:33
 */
public class NextBuilderMockConfigService {

    private static final String defaultServiceUrl = "http://ts-agg-service.fat-1.qa.nt.ctripcorp.com/workflowdesign/getArexNodeMockUrl";
    private static final Logger LOGGER = LoggerFactory.getLogger(NextBuilderService.class);

    public static NextBuilderMockConfigResponse callMockConfig(String appId) {
        try {
            NextBuilderMockConfigRequest request = new NextBuilderMockConfigRequest();
            request.setAppId(appId);
            HttpClientResponse clientResponse = AsyncHttpClientUtil.postAsyncWithJson(
                defaultServiceUrl,
                Serializer.serialize(request),
                null).join();

            if (clientResponse == null || StringUtil.isEmpty(clientResponse.getBody())) {
                LOGGER.warn("[AREX] Load queryMockData, response is null");
            }
            return Serializer.deserialize(clientResponse.getBody(), NextBuilderMockConfigResponse.class);
        } catch (Throwable e) {
            LOGGER.warn("[AREX] Load queryMockData error, exception message: {}", e.getMessage(), e);
        }
        return null;
    }
}

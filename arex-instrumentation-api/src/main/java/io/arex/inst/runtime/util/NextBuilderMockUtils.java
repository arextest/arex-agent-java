package io.arex.inst.runtime.util;


import io.arex.agent.bootstrap.model.NextBuilderMock;
import io.arex.agent.bootstrap.model.NextBuilderMockDataQueryResponse;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.service.NextBuilderDataService;

/**
 * NextBuilderMockUtils
 *
 * @author ywqiu
 * @date 2025/4/24 9:58
 */
public final class NextBuilderMockUtils {

    private static final String EMPTY_JSON = "{}";
    public static String transactionId = "";

    private NextBuilderMockUtils() {
    }

    public static NextBuilderMock createApacheHttpClientMock(String serviceUrl) {
        NextBuilderMock nextBuilderMock = new NextBuilderMock();
        nextBuilderMock.setServiceUrl(serviceUrl);
        return nextBuilderMock;
    }

    public static NextBuilderMockDataQueryResponse queryMock(NextBuilderMock nextBuilderMock) {
        nextBuilderMock.setTransactionId(transactionId);
        NextBuilderMockDataQueryResponse response = query(nextBuilderMock);
        if (response != null && StringUtil.isNotEmpty(response.getResponseContent())) {
            response.setResponseContent(
                ZstdService.getInstance().deserialize(response.getResponseContent()));
        }
        return response;
    }

    public static NextBuilderMockDataQueryResponse query(NextBuilderMock nextBuilderMock) {

        String data = NextBuilderDataService.INSTANCE.query(
            nextBuilderMock.getServiceUrl(),
            nextBuilderMock.getOriginRequestBody(),
            nextBuilderMock.getTransactionId(),
            nextBuilderMock.getRequestMethod()
        );

        if (StringUtil.isEmpty(data) || EMPTY_JSON.equals(data)) {
            LogManager.warn("load next build mock data", StringUtil.format("response body is null. request: %s", data));
            return null;
        }
        return Serializer.deserialize(data, NextBuilderMockDataQueryResponse.class);
    }
}

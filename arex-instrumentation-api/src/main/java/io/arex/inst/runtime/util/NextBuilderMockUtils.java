package io.arex.inst.runtime.util;


import io.arex.agent.bootstrap.model.NextBuilderMock;
import io.arex.agent.bootstrap.model.NextBuilderMockContext;
import io.arex.agent.bootstrap.model.NextBuilderMockDataQueryResponse;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.service.ExtensionLogService;
import io.arex.inst.runtime.service.NextBuilderDataService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NextBuilderMockUtils
 *
 * @author ywqiu
 * @date 2025/4/24 9:58
 */
public final class NextBuilderMockUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NextBuilderMockUtils.class);
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
        if (response != null
            && response.getResult() != null
            && StringUtil.isNotEmpty(response.getResult().getData())) {
            response.getResult().setData(
                ZstdService.getInstance().deserialize(response.getResult().getData()));
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
            ExtensionLogService.getInstance().warn("AREX-NextBuilder", "Load queryMockData error, response data is empty");
            return null;
        }
        return Serializer.deserialize(data, NextBuilderMockDataQueryResponse.class);
    }

    public static NextBuilderMockContext getNextBuilderMockContext(String resourceUrl,
        String requestMethod,
        String body) {

        NextBuilderMock nextBuilderMock = createApacheHttpClientMock(resourceUrl);
        nextBuilderMock.setOriginRequestBody(ZstdService.getInstance().serialize(body));
        nextBuilderMock.setRequestMethod(requestMethod);

        NextBuilderMockDataQueryResponse response = queryMock(nextBuilderMock);

        if (response == null
            || response.getResult() == null
            || StringUtil.isEmpty(response.getResult().getData())) {
            ExtensionLogService.getInstance().warn("AREX-NextBuilder",
                "Load queryMockData error mock Response data is null");
            return null;
        }

        NextBuilderMockContext mockContext = new NextBuilderMockContext();
        mockContext.setBody(body);
        mockContext.setUrl(resourceUrl);
        mockContext.setRequestMethod(requestMethod);
        mockContext.setMockResponseBody(response.getResult().getData());
        mockContext.setAcceptEncoding(response.getResult().getAcceptEncoding());
        return mockContext;
    }

    public static byte[] compressString(String input) {
        if (input == null || input.length() == 0) {
            return null;
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            try (GZIPOutputStream gzipOS = new GZIPOutputStream(bos)) {
                gzipOS.write(input.getBytes(StandardCharsets.UTF_8));
            }

            return bos.toByteArray();

        } catch (IOException ex) {
            return null;
        }
    }

}

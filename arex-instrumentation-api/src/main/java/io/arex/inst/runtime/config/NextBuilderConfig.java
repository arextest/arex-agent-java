package io.arex.inst.runtime.config;


import io.arex.inst.runtime.service.NextBuilderExtensionParameterService;
import java.util.List;
import java.util.Objects;

/**
 * NextBuilderConfig
 *
 * @author ywqiu
 * @date 2025/4/24 19:14
 */
public class NextBuilderConfig {

    private static NextBuilderConfig INSTANCE = null;

    public static NextBuilderConfig get() {
        return INSTANCE;
    }

    private final String serviceName;
    private final boolean openMock;

    private final List<String> mockServiceUrls;

    private final String mockDataQueryUri;
    private final List<String> requestList;

    public NextBuilderConfig(String serviceName,
        boolean openMock,
        List<String> mockServiceUrls,
        String mockDataQueryUri,
        List<String> requestList) {
        this.serviceName = serviceName;
        this.openMock = openMock;
        this.mockServiceUrls = mockServiceUrls;
        this.mockDataQueryUri = mockDataQueryUri;
        this.requestList = requestList;
    }

    public static void update(String serviceName,
        boolean openMock,
        List<String> mockServiceUrls,
        String mockDataQueryUri,
        List<String> requestList) {
        INSTANCE = new NextBuilderConfig(serviceName, openMock,
            mockServiceUrls, mockDataQueryUri, requestList);
    }

    public String getServiceName() {
        return serviceName;
    }

    public boolean isOpenMock() {
        return openMock
            && (NextBuilderExtensionParameterService.getInstance() != null
            && !"Prod".equalsIgnoreCase(NextBuilderExtensionParameterService.getInstance().getEnvironment()));
    }

    public List<String> getMockServiceUrls() {
        return mockServiceUrls;
    }

    public String getMockDataQueryUri() {
        return mockDataQueryUri;
    }

    public List<String> getRequestList() {
        return requestList;
    }
}

package io.arex.foundation.config;


import io.arex.inst.runtime.config.NextBuilderConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * NextBuilderConfigManager
 *
 * @author ywqiu
 * @date 2025/4/24 9:36
 */
public class NextBuilderConfigManager {

    public static final NextBuilderConfigManager INSTANCE = new NextBuilderConfigManager();
    private boolean openMock;
    private List<String> mockServiceUrls;
    private List<String> mainServiceUrls;
    private String mockDataQueryUri;

    private NextBuilderConfigManager() {
        this.openMock = false;
        this.mockServiceUrls = new ArrayList<>();
        this.mainServiceUrls = new ArrayList<>();
        this.mockDataQueryUri = null;
    }

    public void updateConfig(
        String serviceName,
        boolean openMock,
        List<String> mockServiceUrls,
        List<String> mainServiceUrls,
        String mockDataQueryUri,
        boolean originalResponse) {
        this.openMock = openMock;
        this.mockServiceUrls = mockServiceUrls;
        this.mainServiceUrls = mainServiceUrls;
        this.mockDataQueryUri = mockDataQueryUri;
        NextBuilderConfig.get().update(
            serviceName,
            openMock,
            mainServiceUrls,
            mockDataQueryUri,
            mockServiceUrls,
            originalResponse
        );

    }

    public void setOpenMock(boolean openMock) {
        this.openMock = openMock;
    }

    public void setMockServiceUrls(List<String> mockServiceUrls) {
        this.mockServiceUrls = mockServiceUrls;
    }

    public void setMainServiceUrls(List<String> mainServiceUrls) {
        this.mainServiceUrls = mainServiceUrls;
    }

    public void setMockDataQueryUri(String mockDataQueryUri) {
        this.mockDataQueryUri = mockDataQueryUri;
    }

    public boolean isOpenMock() {
        return openMock;
    }

    public List<String> getMockServiceUrls() {
        return mockServiceUrls;
    }

    public List<String> getMainServiceUrls() {
        return mainServiceUrls;
    }

    public String getMockDataQueryUri() {
        return mockDataQueryUri;
    }
}

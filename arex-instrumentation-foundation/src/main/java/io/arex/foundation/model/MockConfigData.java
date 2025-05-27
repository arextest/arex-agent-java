package io.arex.foundation.model;

import java.util.List;

/**
 * MockConfigData
 *
 * @author ywqiu
 * @date 2025/4/23 16:36
 */
public class MockConfigData {

    private List<String> requestList;
    private Boolean openMock;
    private String mockDataQueryUri;
    private List<String> mainServiceUrls;

    public List<String> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<String> requestList) {
        this.requestList = requestList;
    }

    public void setOpenMock(Boolean openMock) {
        this.openMock = openMock;
    }

    public void setMockDataQueryUri(String mockDataQueryUri) {
        this.mockDataQueryUri = mockDataQueryUri;
    }

    public String getMockDataQueryUri() {
        return mockDataQueryUri;
    }

    public Boolean getOpenMock() {
        return openMock;
    }

    public void setMainServiceUrls(List<String> mainServiceUrls) {
        this.mainServiceUrls = mainServiceUrls;
    }

    public List<String> getMainServiceUrls() {
        return mainServiceUrls;
    }
}

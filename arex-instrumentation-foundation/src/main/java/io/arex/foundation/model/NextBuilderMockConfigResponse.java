package io.arex.foundation.model;

/**
 * NextBuilderMockConfigResponse
 *
 * @author ywqiu
 * @date 2025/4/23 16:35
 */

public class NextBuilderMockConfigResponse {

    private int statusCode;
    private MockConfigData data;

    public int getStatusCode() {
        return statusCode;
    }

    public MockConfigData getData() {
        return data;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setData(MockConfigData data) {
        this.data = data;
    }
}

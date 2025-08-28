package io.arex.agent.bootstrap.model;

/**
 * NextBuilderMockDataQueryResponse
 *
 * @author ywqiu
 * @date 2025/4/16 10:04
 */
public class NextBuilderMockDataQueryResponse {

    private MockDataResponseHead responseHead;
    private ErrorResult errorResult;

    private NextBuilderMockResult result;

    public void setResponseHead(
        MockDataResponseHead responseHead) {
        this.responseHead = responseHead;
    }

    public void setErrorResult(ErrorResult errorResult) {
        this.errorResult = errorResult;
    }

    public MockDataResponseHead getResponseHead() {
        return responseHead;
    }

    public ErrorResult getErrorResult() {
        return errorResult;
    }

    public NextBuilderMockResult getResult() {
        return result;
    }

    public void setResult(NextBuilderMockResult result) {
        this.result = result;
    }
}

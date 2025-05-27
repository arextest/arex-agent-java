package io.arex.agent.bootstrap.model;

/**
 * ErrorResult
 *
 * @author ywqiu
 * @date 2025/4/16 10:07
 */
public class ErrorResult {

    private String errorCode;
    private String errorMessage;

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

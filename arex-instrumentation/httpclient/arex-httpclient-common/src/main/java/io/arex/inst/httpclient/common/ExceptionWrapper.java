package io.arex.inst.httpclient.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExceptionWrapper {
    @JsonIgnore
    private transient Exception originalException;

    @JsonProperty("errorMessage")
    private String originalMessage;
    @JsonProperty("cancelled")
    private boolean cancelled;

    public Exception getOriginalException() {
        if (originalException == null) {
            originalException = new Exception(originalMessage);
        }
        return originalException;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public ExceptionWrapper(Exception original) {
        if (original != null) {
            this.originalException = original;
            this.originalMessage = original.getMessage();
            this.cancelled = false;
        } else {
            this.cancelled = true;
        }
    }
}

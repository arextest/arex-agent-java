package io.arex.inst.httpclient.common;

public class ExceptionWrapper {
    private transient Exception originalException;
    private String originalMessage;
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

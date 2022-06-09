package io.arex.inst.httpclient.common;

public class ArexDataException extends RuntimeException {
    public ArexDataException(String message) {
        this(message, null);
    }

    public ArexDataException(Throwable cause) {
        this.initCause(cause);
    }

    public ArexDataException(String message, Exception cause) {
        super(message, cause);
    }
}

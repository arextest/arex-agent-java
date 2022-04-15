package io.arex.inst.apache.httpclient.common;

public class ArexDataException extends Exception {
    public ArexDataException(String message) {
        this(message, null);
    }

    public ArexDataException(String message, Exception cause) {
        super(message, cause);
    }
}

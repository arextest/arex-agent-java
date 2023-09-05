package io.arex.foundation.model;

public enum DecelerateReasonEnum {
    NORMAL(0, "normal"),
    QUEUE_OVERFLOW(1, "queue overflow"),
    SERVICE_EXCEPTION(2, "service exception");

    private final int code;
    private final String value;
    DecelerateReasonEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getCodeStr() {
        return String.valueOf(code);
    }

    public String getValue() {
        return value;
    }
}

package io.arex.inst.runtime.model;

public enum InitializeEnum {
    START(0),
    RUNNING(1),
    COMPLETE(2);

    private int code;

    InitializeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

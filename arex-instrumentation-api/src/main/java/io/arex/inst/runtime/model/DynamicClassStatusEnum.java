package io.arex.inst.runtime.model;

public enum DynamicClassStatusEnum {
    /**
     * Unchanged, do nothing
     */
    UNCHANGED(0),

    /**
     * Needs to transform
     */
    RETRANSFORM(1),
    /**
     * Reset, if all dynamic class status of a Class is RESET status, the Class need to reset to original bytecode
     */
    RESET(2);

    private final int code;

    DynamicClassStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

package io.arex.foundation.model;

public enum MockDataType {
    /**
     * RECORD
     */
    RECORD((byte) 0),
    /**
     * REPLAY
     */
    REPLAY((byte) 1);

    private final byte value;

    MockDataType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}

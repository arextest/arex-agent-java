package io.arex.foundation.model;

public enum AgentStatusEnum {
    /**
     * NONE
     */
    NONE(0, "none"),
    /**
     * Arex first load the config
     */
    START(1, "start"),
    /**
     * AREX just to load the config
     */
    UN_START(2, "unstart"),
    /**
     * AREX is up and recording
     */
    WORKING(3, "working"),
    /**
     * AREX is up, but not recording maybe rate=0 or allowDayOfWeeks is not match
     */
    SLEEPING(4, "sleeping"),
    /**
     * AREX is shutdown, need to restart
     */
    SHUTDOWN(5, "shutdown");

    private final int code;
    private final String value;
    AgentStatusEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}

package io.arex.agent.bootstrap.model;

/**
 * Data service policy selection
 */
public enum MockStrategyEnum {

    /**
     * Try Find Last Value
     */
    FIND_LAST("0"),

    /**
     * Over Storage Size break
     */
    OVER_BREAK("1"),

    /**
     * Strict matching, Null if not matched
     */
    STRICT_MATCH("2");

    private String code;

    MockStrategyEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

package io.arex.agent.bootstrap.model;

/**
 * Data service policy selection
 */
public enum MockStrategyEnum {

    /**
     * if the number of visits is greater than the number stored in the records,
     * try to find the last value as mock response
     */
    FIND_LAST("0"),

    /**
     * if the number of visits is greater than the number stored in the records,don't use last value as mock
     * response,should be return null
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

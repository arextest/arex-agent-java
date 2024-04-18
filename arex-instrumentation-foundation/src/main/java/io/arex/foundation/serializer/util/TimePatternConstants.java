package io.arex.foundation.serializer.util;


import io.arex.agent.bootstrap.util.JdkUtils;

public class TimePatternConstants {
    private TimePatternConstants() {
    }

    public static final String SIMPLE_DATE_FORMAT_MILLIS_REQUEST = "yyyy-MM-dd HH:mm:";
    public static final String SIMPLE_DATE_FORMAT_MILLIS_WITH_ZONE_REQUEST = "yyyy-MM-dd'T'HH:mm:";
    public static final String SHORT_TIME_FORMAT_MILLISECOND_REQUEST = "HH:mm:";
    public static final String TIME_ZONE_REQUEST = "ZZZ";
    public static final String ZERO_SECOND_TIME_REQUEST = "00.000";
    public static String localTimeZeroSecondTimeRequest = ZERO_SECOND_TIME_REQUEST;

    /**
     * yyyy-MM-dd HH:mm:ss.SSS/yyyy-MM-dd'T'HH:mm:ss.SSSZZZ
     */
    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SIMPLE_DATE_FORMAT_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String SIMPLE_DATE_FORMAT_NANOSECOND = "yyyy-MM-dd HH:mm:ss.SSSSSSSSS";
    /**
     *  2020-06-09T09:00:00.000+08:00
     */
    public static final String SIMPLE_DATE_FORMAT_WITH_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ";
    public static final String SIMPLE_DATE_FORMAT_WITH_TIMEZONE_DATETIME = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
    /**
     * yyyy-MM-dd
     */
    public static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
    /**
     * HH:mm:ss.SSS
     */
    public static final String SHORT_TIME_FORMAT = "HH:mm:ss";
    public static final String SHORT_TIME_FORMAT_MILLISECOND = "HH:mm:ss.SSS";
    public static final String SHORT_TIME_FORMAT_NANOSECOND = "HH:mm:ss.SSSSSSSSS";

    public static String localDateTimeFormat = SIMPLE_DATE_FORMAT_MILLIS;

    public static String localTimeFormat = SHORT_TIME_FORMAT_MILLISECOND;

    static {
        if (JdkUtils.isJdk11OrHigher()) {
            localDateTimeFormat = SIMPLE_DATE_FORMAT_NANOSECOND;
            localTimeFormat = SHORT_TIME_FORMAT_NANOSECOND;
            localTimeZeroSecondTimeRequest = "00.000000000";
        }
    }
}

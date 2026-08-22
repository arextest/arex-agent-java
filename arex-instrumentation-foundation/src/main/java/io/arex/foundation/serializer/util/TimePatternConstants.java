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
     * 2020-06-09T09:00:00.000+08:00[Asia/Shanghai], keep the zone id to avoid losing it after deserialize
     *
     * <p>The offset uses ZZZZZ and the year uses uuuu, unlike the patterns above:
     * ZZZ drops the seconds of the offset, and on deserialize the offset overrides the zone when the
     * instant is computed, so any zone that still had a sub-minute offset comes back shifted
     * (Africa/Monrovia was -00:44:30 until 1972, Instant.EPOCH drifts 30s there) and the shift
     * accumulates over record-replay-record. yyyy is year-of-era, it turns a year before 1 AD into a
     * positive one and can not write LocalDateTime.MIN back.
     */
    public static final String SIMPLE_DATE_FORMAT_MILLIS_WITH_ZONE_ID = "uuuu-MM-dd'T'HH:mm:ss.SSSZZZZZ'['VV']'";
    public static final String SIMPLE_DATE_FORMAT_NANOSECOND_WITH_ZONE_ID = "uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSSZZZZZ'['VV']'";
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

    public static String zonedDateTimeFormat = SIMPLE_DATE_FORMAT_MILLIS_WITH_ZONE_ID;

    static {
        if (JdkUtils.isJdk11OrHigher()) {
            localDateTimeFormat = SIMPLE_DATE_FORMAT_NANOSECOND;
            localTimeFormat = SHORT_TIME_FORMAT_NANOSECOND;
            localTimeZeroSecondTimeRequest = "00.000000000";
            zonedDateTimeFormat = SIMPLE_DATE_FORMAT_NANOSECOND_WITH_ZONE_ID;
        }
    }
}

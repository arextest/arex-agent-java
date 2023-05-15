package io.arex.inst.serializer;

public class DatePatternConstants {
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

    public static final int JDK_11 = 11;

    static {
        if (isJdk11()) {
            localDateTimeFormat = SIMPLE_DATE_FORMAT_NANOSECOND;
            localTimeFormat = SHORT_TIME_FORMAT_NANOSECOND;
        }
    }

    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    public static boolean isJdk11() {
        return getJavaVersion() >= JDK_11;
    }
}

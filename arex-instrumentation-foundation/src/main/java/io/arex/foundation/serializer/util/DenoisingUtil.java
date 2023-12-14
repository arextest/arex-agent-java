package io.arex.foundation.serializer.util;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.thirdparty.util.time.DateFormatUtils;
import org.joda.time.DateTime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DenoisingUtil {
    private DenoisingUtil() {
    }

    private static final String UUID_DENOISED = "00000000-0000-0000-0000-000000000000";
    private static final String IP_DENOISED = "1.1.1.1";
    private static final String POINT = ".";

    public static boolean isUUID(String source) {
        return StringUtil.isNotEmpty(source)
                && source.length() == 36
                && source.indexOf('-') == 8
                && source.indexOf('-', 9) == 13
                && source.indexOf('-', 14) == 18
                && source.indexOf('-', 19) == 23;
    }

    public static boolean isIP(String source) {
        if (StringUtil.isEmpty(source)) {
            return false;
        }
        int length = source.length();
        if (length < 7 || length > 15 || StringUtil.countMatches(source, POINT) != 3) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char c = source.charAt(i);
            if (c == '.') {
                continue;
            }
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static String zeroIP() {
        return IP_DENOISED;
    }

    public static String zeroUUID() {
        return UUID_DENOISED;
    }

    public static String zeroSecondTime(Calendar calendar) {
        String timeZone = DateFormatUtils.format(calendar, TimePatternConstants.TIME_ZONE_REQUEST, calendar.getTimeZone());
        String time = DateFormatUtils.format(calendar, TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS_WITH_ZONE_REQUEST, calendar.getTimeZone());
        return time + TimePatternConstants.ZERO_SECOND_TIME_REQUEST + timeZone;
    }

    public static String zeroSecondTime(Date date) {
        return DateFormatUtils.format(date, TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS_REQUEST) + TimePatternConstants.ZERO_SECOND_TIME_REQUEST;
    }

    public static String zeroSecondTime(DateTime dateTime) {
        String timeZone = dateTime.toString(TimePatternConstants.TIME_ZONE_REQUEST);
        String time = dateTime.toString(TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS_WITH_ZONE_REQUEST);
        return time + TimePatternConstants.ZERO_SECOND_TIME_REQUEST + timeZone;
    }

    public static String zeroSecondTime(org.joda.time.LocalDateTime localDateTime) {
        return localDateTime.toString(TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS_REQUEST) +
                TimePatternConstants.ZERO_SECOND_TIME_REQUEST;
    }

    public static String zeroSecondTime(org.joda.time.LocalTime localTime) {
        return localTime.toString(TimePatternConstants.SHORT_TIME_FORMAT_MILLISECOND_REQUEST) +
                TimePatternConstants.ZERO_SECOND_TIME_REQUEST;
    }

    public static String zeroSecondTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS_REQUEST)) +
                TimePatternConstants.localTimeZeroSecondTimeRequest;
    }

    public static String zeroSecondTime(LocalTime localTime) {
        return localTime.format(DateTimeFormatter.ofPattern(TimePatternConstants.SHORT_TIME_FORMAT_MILLISECOND_REQUEST)) +
                TimePatternConstants.localTimeZeroSecondTimeRequest;
    }

    public static String zeroSecondTime(OffsetDateTime offsetDateTime) {
        String timeZone = DateFormatUtils.format(offsetDateTime, TimePatternConstants.TIME_ZONE_REQUEST);
        String time = DateFormatUtils.format(offsetDateTime, TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS_WITH_ZONE_REQUEST);
        return time + TimePatternConstants.ZERO_SECOND_TIME_REQUEST + timeZone;
    }

    public static String zeroSecondTime(Instant instant) {
        return DateTimeFormatter.ofPattern(TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS_REQUEST).
                withZone(ZoneId.systemDefault()).format(instant) +
                TimePatternConstants.localTimeZeroSecondTimeRequest;
    }
}

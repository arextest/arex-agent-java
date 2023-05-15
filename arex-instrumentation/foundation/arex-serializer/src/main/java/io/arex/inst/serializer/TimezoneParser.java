package io.arex.inst.serializer;

import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.arex.agent.bootstrap.util.StringUtil;

public class TimezoneParser {
    public static final TimezoneParser INSTANCE = new TimezoneParser();
    private static final String DEFAULT_TIME_ZONE_FORMAT = "(([+\\-])\\d{2}:\\d{2})|Z";
    private static final Pattern DEFAULT_TIME_ZONE_PATTERN = Pattern.compile(DEFAULT_TIME_ZONE_FORMAT);
    private final ConcurrentHashMap<String, TimeZone> timeZonesMap = new ConcurrentHashMap<>();

    public TimeZone parse(String date) {
        if (StringUtil.isBlank(date)) {
            return TimeZone.getDefault();
        }

        // 2020-06-09T09:00:00.000+08:00 length=29
        if (date.length() <= DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE.length()) {
            return TimeZone.getDefault();
        }

        // 2020-06-09T09:00:00.000+08:00 substring from index 23, result is +08:00
        date = date.substring(DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS.length());

        Matcher matcher = DEFAULT_TIME_ZONE_PATTERN.matcher(date);
        if (!matcher.matches()) {
            return TimeZone.getDefault();
        }

        return getTimeZone(date);
    }

    private TimeZone getTimeZone(final String zoneId) {
        return timeZonesMap.computeIfAbsent(zoneId, (id) -> TimeZone.getTimeZone("GMT" + zoneId));
    }
}

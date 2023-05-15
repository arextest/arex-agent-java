package io.arex.inst.serializer;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import io.arex.agent.bootstrap.util.StringUtil;

public class DateFormatParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateFormatParser.class);
    private static final Map<String, DateTimeFormatter> DEFAULT_FORMATTER_MAP = new ConcurrentHashMap<>();
    private static final Map<Integer, String> DATE_PATTERN_MAP = new HashMap<>();

    public static final DateFormatParser INSTANCE = new DateFormatParser();

    private DateFormatParser() {
//        initDatePatternMap();
    }

//    public void initDatePatternMap() {
//        DATE_PATTERN_MAP
//                .put(DatePatternConstants.SIMPLE_DATE_FORMAT.length(), DatePatternConstants.SIMPLE_DATE_FORMAT);
//        DATE_PATTERN_MAP
//                .put(DatePatternConstants.SHORT_TIME_FORMAT.length(), DatePatternConstants.SHORT_TIME_FORMAT);
//        DATE_PATTERN_MAP.put(DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS.length(), DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS);
//        DATE_PATTERN_MAP.put(DatePatternConstants.SHORT_TIME_FORMAT_MILLISECOND.length(), DatePatternConstants.SHORT_TIME_FORMAT_MILLISECOND);
//        DATE_PATTERN_MAP.put(DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE.length() + 1, DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE);
//    }


    public Calendar parseCalendar(final String source) {
        TimeZone timeZone = TimezoneParser.INSTANCE.parse(source);
        Calendar calendar = Calendar.getInstance(timeZone);
        return Optional.ofNullable(parseDate(source, timeZone)).map(date -> {
            calendar.setTime(date);
            return calendar;
        }).orElse(calendar);
    }

    public GregorianCalendar parseGregorianCalendar(final String source) {
        TimeZone timeZone = TimezoneParser.INSTANCE.parse(source);
        GregorianCalendar gregorianCalendar = new GregorianCalendar(timeZone);
        return Optional.ofNullable(parseDate(source, timeZone)).map(date -> {
            gregorianCalendar.setTime(date);
            return gregorianCalendar;
        }).orElse(gregorianCalendar);
    }

    /**
     * Parse date without timezone
     *
     * @param source datetime string
     * @return Date
     */
    public Date parseDate(final String source) {
        return parseDate(source, null);
    }

    /**
     * Parse date with timezone
     *
     * @param source datetime string
     * @return Date
     */
    public Date parseDate(String source, TimeZone timeZone) {
        if (StringUtil.isEmpty(source)) {
            return null;
        }

        if (timeZone == null) {
            timeZone = TimezoneParser.INSTANCE.parse(source);
        }

        try {
            if (source.length() >= DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE.length()) {
                source = source.substring(0, DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS.length()).replace('T', ' ');
            }

            String pattern =
                    DATE_PATTERN_MAP.getOrDefault(source.length(), DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS);
            return FastDateFormat.getInstance(pattern, timeZone).parse(source);
        } catch (ParseException e) {
            LOGGER.error("parseDate", e);
            return null;
        }
    }

//    /**
//     * Get datetime formatter
//     *
//     * @param source datetime string
//     * @param defaultPattern default datetime pattern
//     * @return DateTimeFormatter instance
//     */
//    public DateTimeFormatter getFormatter(final String source, final String defaultPattern) {
////        String pattern = DATE_PATTERN_MAP.getOrDefault(source.length(), defaultPattern);
//        return DEFAULT_FORMATTER_MAP.computeIfAbsent(source, DateTimeFormatter::ofPattern);
//    }

    /**
     * Get datetime formatter
     *
     * @param pattern datetime pattern
     * @return DateTimeFormatter instance
     */
    public DateTimeFormatter getFormatter(final String pattern) {
        return DEFAULT_FORMATTER_MAP.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
    }
}

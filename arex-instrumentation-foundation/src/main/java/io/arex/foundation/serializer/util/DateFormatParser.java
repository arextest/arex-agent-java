package io.arex.foundation.serializer.util;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.thirdparty.util.time.FastDateFormat;
import io.arex.inst.runtime.log.LogManager;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
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

public class DateFormatParser {
    private static final Map<String, DateTimeFormatter> DEFAULT_FORMATTER_MAP = new ConcurrentHashMap<>();
    private static final Map<Integer, String> DATE_PATTERN_MAP = new HashMap<>();

    public static final DateFormatParser INSTANCE = new DateFormatParser();

    private DateFormatParser() {
        initDatePatternMap();
    }

    public void initDatePatternMap() {
        DATE_PATTERN_MAP
                .put(TimePatternConstants.SIMPLE_DATE_FORMAT.length(), TimePatternConstants.SIMPLE_DATE_FORMAT);
        DATE_PATTERN_MAP
                .put(TimePatternConstants.SHORT_TIME_FORMAT.length(), TimePatternConstants.SHORT_TIME_FORMAT);
        DATE_PATTERN_MAP.put(TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS.length(),
                TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS);
        DATE_PATTERN_MAP.put(TimePatternConstants.SHORT_TIME_FORMAT_MILLISECOND.length(),
                TimePatternConstants.SHORT_TIME_FORMAT_MILLISECOND);
        DATE_PATTERN_MAP.put(TimePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE.length() + 1,
                TimePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE);
    }


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

    public DateTime parseDateTime(String value) {
        TimeZone timeZone = TimezoneParser.INSTANCE.parse(value);
        DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(timeZone);
        Date date = DateFormatParser.INSTANCE.parseDate(value, timeZone);
        if (date == null) {
            return null;
        }
        return new DateTime(date.getTime()).withZone(dateTimeZone);
    }

    public XMLGregorianCalendar parseXMLGregorianCalendar(String value) {
        GregorianCalendar calendar = parseGregorianCalendar(value);
        XMLGregorianCalendar xmlGregorianCalendar = null;
        try {
            xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        } catch (Exception ex) {
            LogManager.warn("XMLGregorianCalendar.parseXMLGregorianCalendar", ex);
        }
        return xmlGregorianCalendar;
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
            if (source.length() >= TimePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE.length()) {
                source = source.substring(0, TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS.length()).replace('T', ' ');
            }

            String pattern =
                    DATE_PATTERN_MAP.getOrDefault(source.length(), TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS);
            return FastDateFormat.getInstance(pattern, timeZone).parse(source);
        } catch (ParseException e) {
            LogManager.warn("parseDate", e);
            return null;
        }
    }

    /**
     * Get datetime formatter
     *
     * @param source datetime string
     * @param defaultPattern default datetime pattern
     * @return DateTimeFormatter instance
     */
    public DateTimeFormatter getFormatter(final String source, final String defaultPattern) {
        String pattern = DATE_PATTERN_MAP.getOrDefault(source.length(), defaultPattern);
        return DEFAULT_FORMATTER_MAP.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
    }

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

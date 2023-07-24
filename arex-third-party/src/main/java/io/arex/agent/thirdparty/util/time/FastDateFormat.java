package io.arex.agent.thirdparty.util.time;

import java.text.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FastDateFormat extends Format implements DateParser, DatePrinter {
    private static final FormatCache<FastDateFormat> cache= new FormatCache<FastDateFormat>() {
        @Override
        protected FastDateFormat createInstance(final String pattern, final TimeZone timeZone, final Locale locale) {
            return new FastDateFormat(pattern, timeZone, locale);
        }
    };

    private final FastDatePrinter printer;
    private final FastDateParser parser;

    public static FastDateFormat getInstance(final String pattern, final TimeZone timeZone) {
        return cache.getInstance(pattern, timeZone, null);
    }

    public static FastDateFormat getInstance(final String pattern, final TimeZone timeZone, final Locale locale) {
        return cache.getInstance(pattern, timeZone, locale);
    }

    // Constructor
    //-----------------------------------------------------------------------
    /**
     * <p>Constructs a new FastDateFormat.</p>
     *
     * @param pattern  {@link SimpleDateFormat} compatible pattern
     * @param timeZone  non-null time zone to use
     * @param locale  non-null locale to use
     * @throws NullPointerException if pattern, timeZone, or locale is null.
     */
    protected FastDateFormat(final String pattern, final TimeZone timeZone, final Locale locale) {
        this(pattern, timeZone, locale, null);
    }

    // Constructor
    //-----------------------------------------------------------------------
    /**
     * <p>Constructs a new FastDateFormat.</p>
     *
     * @param pattern  {@link SimpleDateFormat} compatible pattern
     * @param timeZone  non-null time zone to use
     * @param locale  non-null locale to use
     * @param centuryStart The start of the 100 year period to use as the "default century" for 2 digit year parsing.  If centuryStart is null, defaults to now - 80 years
     * @throws NullPointerException if pattern, timeZone, or locale is null.
     */
    protected FastDateFormat(final String pattern, final TimeZone timeZone, final Locale locale, final Date centuryStart) {
        printer= new FastDatePrinter(pattern, timeZone, locale);
        parser= new FastDateParser(pattern, timeZone, locale, centuryStart);
    }

    // Format methods
    //-----------------------------------------------------------------------
    /**
     * <p>Formats a {@code Date}, {@code Calendar} or
     * {@code Long} (milliseconds) object.</p>
     * This method is an implementation of {@link Format#format(Object, StringBuffer, FieldPosition)}
     *
     * @param obj  the object to format
     * @param toAppendTo  the buffer to append to
     * @param pos  the position - ignored
     * @return the buffer passed in
     */
    @Override
    public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
        return toAppendTo.append(printer.format(obj));
    }

    /**
     * <p>Formats a millisecond {@code long} value.</p>
     *
     * @param millis  the millisecond value to format
     * @return the formatted string
     * @since 2.1
     */
    @Override
    public String format(final long millis) {
        return printer.format(millis);
    }

    /**
     * <p>Formats a {@code Date} object using a {@code GregorianCalendar}.</p>
     *
     * @param date  the date to format
     * @return the formatted string
     */
    @Override
    public String format(final Date date) {
        return printer.format(date);
    }

    /**
     * <p>Formats a {@code Calendar} object.</p>
     *
     * @param calendar  the calendar to format
     * @return the formatted string
     */
    @Override
    public String format(final Calendar calendar) {
        return printer.format(calendar);
    }

    @Deprecated
    @Override
    public StringBuffer format(final long millis, final StringBuffer buf) {
        return printer.format(millis, buf);
    }

    /**
     * <p>Formats a {@code Date} object into the
     * supplied {@code StringBuffer} using a {@code GregorianCalendar}.</p>
     *
     * @param date  the date to format
     * @param buf  the buffer to format into
     * @return the specified string buffer
     * @deprecated Use {{@link #format(Date, Appendable)}.
     */
    @Deprecated
    @Override
    public StringBuffer format(final Date date, final StringBuffer buf) {
        return printer.format(date, buf);
    }

    /**
     * <p>Formats a {@code Calendar} object into the
     * supplied {@code StringBuffer}.</p>
     *
     * @param calendar  the calendar to format
     * @param buf  the buffer to format into
     * @return the specified string buffer
     * @deprecated Use {{@link #format(Calendar, Appendable)}.
     */
    @Deprecated
    @Override
    public StringBuffer format(final Calendar calendar, final StringBuffer buf) {
        return printer.format(calendar, buf);
    }

    /**
     * <p>Formats a millisecond {@code long} value into the
     * supplied {@code StringBuffer}.</p>
     *
     * @param millis  the millisecond value to format
     * @param buf  the buffer to format into
     * @return the specified string buffer
     * @since 3.5
     */
    @Override
    public <B extends Appendable> B format(final long millis, final B buf) {
        return printer.format(millis, buf);
    }

    /**
     * <p>Formats a {@code Date} object into the
     * supplied {@code StringBuffer} using a {@code GregorianCalendar}.</p>
     *
     * @param date  the date to format
     * @param buf  the buffer to format into
     * @return the specified string buffer
     * @since 3.5
     */
    @Override
    public <B extends Appendable> B format(final Date date, final B buf) {
        return printer.format(date, buf);
    }

    /**
     * <p>Formats a {@code Calendar} object into the
     * supplied {@code StringBuffer}.</p>
     *
     * @param calendar  the calendar to format
     * @param buf  the buffer to format into
     * @return the specified string buffer
     * @since 3.5
     */
    @Override
    public <B extends Appendable> B format(final Calendar calendar, final B buf) {
        return printer.format(calendar, buf);
    }

    // Parsing
    //-----------------------------------------------------------------------


    /* (non-Javadoc)
     * @see DateParser#parse(java.lang.String)
     */
    @Override
    public Date parse(final String source) throws ParseException {
        return parser.parse(source);
    }

    /* (non-Javadoc)
     * @see DateParser#parse(java.lang.String, java.text.ParsePosition)
     */
    @Override
    public Date parse(final String source, final ParsePosition pos) {
        return parser.parse(source, pos);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.commons.lang3.time.DateParser#parse(java.lang.String, java.text.ParsePosition, java.util.Calendar)
     */
    @Override
    public boolean parse(final String source, final ParsePosition pos, final Calendar calendar) {
        return parser.parse(source, pos, calendar);
    }

    /* (non-Javadoc)
     * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
     */
    @Override
    public Object parseObject(final String source, final ParsePosition pos) {
        return parser.parseObject(source, pos);
    }

    // Accessors
    //-----------------------------------------------------------------------
    /**
     * <p>Gets the pattern used by this formatter.</p>
     *
     * @return the pattern, {@link SimpleDateFormat} compatible
     */
    @Override
    public String getPattern() {
        return printer.getPattern();
    }

    /**
     * <p>Gets the time zone used by this formatter.</p>
     *
     * <p>This zone is always used for {@code Date} formatting. </p>
     *
     * @return the time zone
     */
    @Override
    public TimeZone getTimeZone() {
        return printer.getTimeZone();
    }

    /**
     * <p>Gets the locale used by this formatter.</p>
     *
     * @return the locale
     */
    @Override
    public Locale getLocale() {
        return printer.getLocale();
    }

    /**
     * <p>Gets an estimate for the maximum string length that the
     * formatter will produce.</p>
     *
     * <p>The actual formatted length will almost always be less than or
     * equal to this amount.</p>
     *
     * @return the maximum formatted length
     */
    public int getMaxLengthEstimate() {
        return printer.getMaxLengthEstimate();
    }

    // Basics
    //-----------------------------------------------------------------------
    /**
     * <p>Compares two objects for equality.</p>
     *
     * @param obj  the object to compare to
     * @return {@code true} if equal
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof FastDateFormat)) {
            return false;
        }
        final FastDateFormat other = (FastDateFormat) obj;
        // no need to check parser, as it has same invariants as printer
        return printer.equals(other.printer);
    }

    /**
     * <p>Returns a hash code compatible with equals.</p>
     *
     * @return a hash code compatible with equals
     */
    @Override
    public int hashCode() {
        return printer.hashCode();
    }

    /**
     * <p>Gets a debugging string version of this formatter.</p>
     *
     * @return a debugging string
     */
    @Override
    public String toString() {
        return "FastDateFormat[" + printer.getPattern() + "," + printer.getLocale() + "," + printer.getTimeZone().getID() + "]";
    }
}

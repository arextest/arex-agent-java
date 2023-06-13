package io.arex.agent.thirdparty.util.time;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateFormatUtils {
    public static String format(final Date date, final String pattern) {
        return format(date, pattern, null, null);
    }

    public static String format(final Calendar calendar, final String pattern, final TimeZone timeZone) {
        return format(calendar, pattern, timeZone, null);
    }

    public static String format(final Date date, final String pattern, final TimeZone timeZone, final Locale locale) {
        final FastDateFormat df = FastDateFormat.getInstance(pattern, timeZone, locale);
        return df.format(date);
    }

    public static String format(final Calendar calendar, final String pattern, final TimeZone timeZone, final Locale locale) {
        final FastDateFormat df = FastDateFormat.getInstance(pattern, timeZone, locale);
        return df.format(calendar);
    }
}

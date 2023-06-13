package io.arex.agent.thirdparty.util.time;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastTimeZone {
    private static final Pattern GMT_PATTERN = Pattern.compile("^(?:(?i)GMT)?([+-])?(\\d\\d?)?(:?(\\d\\d?))?$");

    private static final TimeZone GREENWICH = new GmtTimeZone(false, 0, 0);

    /**
     * Gets the GMT TimeZone.
     * @return A TimeZone with a raw offset of zero.
     */
    public static TimeZone getGmtTimeZone() {
        return GREENWICH;
    }

    /**
     * Gets a TimeZone with GMT offsets.  A GMT offset must be either 'Z', or 'UTC', or match
     * <em>(GMT)? hh?(:?mm?)?</em>, where h and m are digits representing hours and minutes.
     *
     * @param pattern The GMT offset
     * @return A TimeZone with offset from GMT or null, if pattern does not match.
     */
    public static TimeZone getGmtTimeZone(final String pattern) {
        if ("Z".equals(pattern) || "UTC".equals(pattern)) {
            return GREENWICH;
        }

        final Matcher m = GMT_PATTERN.matcher(pattern);
        if (m.matches()) {
            final int hours = parseInt(m.group(2));
            final int minutes = parseInt(m.group(4));
            if (hours == 0 && minutes == 0) {
                return GREENWICH;
            }
            return new GmtTimeZone(parseSign(m.group(1)), hours, minutes);
        }
        return null;
    }

    /**
     * Gets a TimeZone, looking first for GMT custom ids, then falling back to Olson ids.
     * A GMT custom id can be 'Z', or 'UTC', or has an optional prefix of GMT,
     * followed by sign, hours digit(s), optional colon(':'), and optional minutes digits.
     * i.e. <em>[GMT] (+|-) Hours [[:] Minutes]</em>
     *
     * @param id A GMT custom id (or Olson id
     * @return A timezone
     */
    public static TimeZone getTimeZone(final String id) {
        final TimeZone tz = getGmtTimeZone(id);
        if (tz != null) {
            return tz;
        }
        return TimeZone.getTimeZone(id);
    }

    private static int parseInt(final String group) {
        return group != null ? Integer.parseInt(group) : 0;
    }

    private static boolean parseSign(final String group) {
        return group != null && group.charAt(0) == '-';
    }

    // do not instantiate
    private FastTimeZone() {
    }
}

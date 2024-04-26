package io.arex.agent.bootstrap.util;

public class NumberUtil {
    private NumberUtil() {
    }

    public static int toInt(String str) {
        return toInt(str, 0);
    }

    public static int toInt(String str, int defaultValue) {
        if(str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static long parseLong(Object value) {
        if (value == null) {
            return 0L;
        }

        String valueStr = String.valueOf(value);
        if (StringUtil.isEmpty(valueStr)) {
            return 0L;
        }

        try {
            return Long.parseLong(valueStr);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}

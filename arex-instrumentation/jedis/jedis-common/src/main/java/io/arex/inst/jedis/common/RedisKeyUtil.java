package io.arex.inst.jedis.common;

import java.util.Base64;

public class RedisKeyUtil {

    public static String generate(String key1, String key2) {
        return key1.concat(key2);
    }

    public static String generate(String key1, String[] keys2) {
        StringBuilder sb = new StringBuilder(getLen(key1.length() + 1, keys2));
        sb.append(key1).append(':');
        generateMultiple(sb, keys2);
        return sb.toString();
    }

    public static String generate(String... keys) {
        switch (keys.length) {
            case 0: return "";
            case 1: return keys[0];
            case 2: return keys[0].concat(keys[1]);
            default: return generateMultiple(keys);
        }
    }

    public static String generate(byte[]... source) {
        int len = 0;
        String[] keys = new String[source.length];
        for (int i = 0; i < source.length; i++) {
            keys[i] = Base64.getEncoder().encodeToString(source[i]);
            len += keys[i].length() + 1;
        }

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < keys.length; i++) {
            sb.append(keys[i]).append(',');
        }
        return sb.toString();
    }

    private static String generateMultiple(String... keys) {
        StringBuilder sb = new StringBuilder(getLen(0, keys));
        generateMultiple(sb, keys);
        return sb.toString();
    }

    private static void generateMultiple(StringBuilder builder, String... keys) {
        for (int i = 0; i < keys.length; i++) {
            builder.append(keys[i]).append(',');
        }
    }

    private static int getLen(int first, String... keys) {
        for (int i = 0; i < keys.length; i++) {
            first += keys[i].length() + 1;
        }
        return first;
    }

}

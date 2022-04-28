package io.arex.inst.jedis.common;

import java.util.Base64;

public class RedisKeyUtil {

    public static String generate(String... keys) {
        switch (keys.length) {
            case 0: return "";
            case 1: return keys[0];
            default: return generateMultiple(keys);
        }
    }

    public static String generate(byte[]... source) {
        StringBuilder sb = new StringBuilder(source.length * 5);
        sb.append(Base64.getEncoder().encodeToString(source[0]));

        for (int i = 1; i < source.length; i++) {
            sb.append(';').append(Base64.getEncoder().encodeToString(source[i]));
        }

        return sb.toString();
    }

    private static String generateMultiple(String... keys) {
        StringBuilder builder = new StringBuilder(keys.length * 5);
        builder.append(keys[0]);
        generateMultiple(builder, keys);
        return builder.toString();
    }

    private static void generateMultiple(StringBuilder builder, String... keys) {
        for (int i = 1; i < keys.length; i++) {
            builder.append(';').append(keys[i]);
        }
    }
}

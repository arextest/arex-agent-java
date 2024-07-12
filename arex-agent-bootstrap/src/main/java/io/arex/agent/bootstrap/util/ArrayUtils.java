package io.arex.agent.bootstrap.util;

import java.util.function.Function;

public class ArrayUtils {
    private ArrayUtils() {}

    public static byte[] addAll(final byte[] array1, final byte... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    private static byte[] clone(final byte[] array) {
        if (array == null) {
            return new byte[0];
        }
        return array.clone();
    }

    public static boolean isEmpty(final Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotEmpty(final Object[] array) {
        return !isEmpty(array);
    }

    public static String toString(Object[] array, Function<Object, String> parser) {
        if (isEmpty(array)) {
            return null;
        }
        int iMax = array.length - 1;
        StringBuilder builder = new StringBuilder();
        builder.append("[\"");
        for (int i = 0; ; i++) {
            if (array[i] == null) {
                builder.append("null");
            } else {
                builder.append(parser != null ? parser.apply(array[i]) : array[i]);
            }
            if (i == iMax) {
                return builder.append("\"]").toString();
            }
            builder.append("\", \"");
        }
    }

    public static boolean equals(String[] array1, String[] array2) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null) {
            return false;
        }
        if (array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (!array1[i].equals(array2[i])) {
                return false;
            }
        }
        return true;
    }
}

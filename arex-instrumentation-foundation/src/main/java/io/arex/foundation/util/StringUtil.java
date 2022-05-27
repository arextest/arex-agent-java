package io.arex.foundation.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class StringUtil {
    public static final String EMPTY = "";

    public static boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static String[] split(final String source, final char separator) {
        if (isEmpty(source)) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        final int len = source.length();
        final List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        while (i < len) {
            if (source.charAt(i) == separator) {
                if (match) {
                    list.add(source.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }
        if (match || lastMatch) {
            list.add(source.substring(start, i));
        }
        return list.toArray(new String[0]);
    }

    public static String join(final Iterator<?> iterator, final String separator) {
        if (iterator == null || !iterator.hasNext()) {
            return null;
        }

        final Object first = iterator.next();
        if (!iterator.hasNext()) {
            return Objects.toString(first, "");
        }

        final StringBuilder buf = new StringBuilder(256); // Java default is 16, probably too small
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            final Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    public static String formatJson(String jsonStr) {
        if (isEmpty(jsonStr)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        boolean isInQuotationMarks = false;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '"':
                    if (last != '\\') {
                        isInQuotationMarks = !isInQuotationMarks;
                    }
                    sb.append(current);
                    break;
                case '{':
                case '[':
                    sb.append(current);
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent++;
                        indent(sb, indent);
                    }
                    break;
                case '}':
                case ']':
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent--;
                        indent(sb, indent);
                    }
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\' && !isInQuotationMarks) {
                        sb.append('\n');
                        indent(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }

        return sb.toString();
    }

    private static void indent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("    ");
        }
    }

    public static String breakLine(String content, int len) {
        String tmp = "";
        if (len > 0) {
            if (content.length() > len) {
                int rows = (content.length() + len - 1) / len;
                for (int i = 0; i < rows; i++) {
                    if (i == rows - 1) {
                        tmp += content.substring(i * len);
                    } else {
                        tmp += content.substring(i * len, i * len + len) + "\r\n";
                    }
                }
            } else {
                tmp = content;
            }
        }
        return tmp;
    }

    public static Map<String, String> asMap(String content) {
        if (isEmpty(content)) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (String str : content.split(";")) {
            if (isEmpty(str)) {
                continue;
            }
            String[] arr = str.split("=");
            if (arr.length != 2) {
                continue;
            }

            final String k = arr[0];
            final String v = arr[1];
            if (isNotEmpty(k) && isNotEmpty(v)) {
                map.put(k, v);
            }
        }
        return map;
    }

    public static int encodeAndHash(String str){
        if (StringUtils.isBlank(str)) {
            return 0;
        }
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8)).hashCode();
    }
}

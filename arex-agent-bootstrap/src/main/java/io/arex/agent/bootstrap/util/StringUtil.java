package io.arex.agent.bootstrap.util;

import io.arex.agent.thirdparty.util.CharSequenceUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class StringUtil {
    public static final String EMPTY = "";
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static final int INDEX_NOT_FOUND = -1;

    public static String defaultString(final String str) {
        return str == null ? EMPTY : str;
    }

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
    public static String join(final Iterable<?> iterable, final String separator) {
        if (iterable == null) {
            return null;
        }

        Iterator<?> iterator = iterable.iterator();

        if (!iterator.hasNext()) {
            return EMPTY;
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

    private static void indent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("    ");
        }
    }

    public static Map<String, String> asMap(String content) {
        if (isEmpty(content)) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        for (String str : StringUtil.split(content, ';')) {
            String[] arr = StringUtil.split(str, '=');
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

    // net.bytebuddy auto modified by shade: shaded.net.bytebuddy
    public static String removeShadePrefix(String str) {
        return str.length() > 7 ? str.substring(7) : str;
    }

    public static String substring(String str, int start) {
        if (str == null) {
            return null;
        }

        if (start < 0) {
            start += str.length();
        }

        if (start < 0) {
            start = 0;
        }

        return start > str.length() ? EMPTY : str.substring(start);
    }

    public static String[] split(final String source, final char separator) {
        return split(source, separator, false);
    }

    public static String[] split(final String source, final char separator, final boolean preserveAllTokens) {
        if (isEmpty(source)) {
            return EMPTY_STRING_ARRAY;
        }

        final int len = source.length();
        final List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        while (i < len) {
            if (source.charAt(i) == separator) {
                if (match || preserveAllTokens) {
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
        if (match || preserveAllTokens && lastMatch) {
            list.add(source.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] splitByWholeSeparator(String str, String separator) {
        if (str == null) {
            return StringUtil.EMPTY_STRING_ARRAY;
        } else {
            int len = str.length();
            if (len == 0) {
                return StringUtil.EMPTY_STRING_ARRAY;
            } else {
                int separatorLength = separator.length();
                List<String> substrings = new ArrayList<>();
                int beg = 0;
                int end = 0;

                while(end < len) {
                    end = str.indexOf(separator, beg);
                    if (end > -1) {
                        if (end > beg) {
                            substrings.add(str.substring(beg, end));
                            beg = end + separatorLength;
                        } else {
                            beg = end + separatorLength;
                        }
                    } else {
                        substrings.add(str.substring(beg));
                        end = len;
                    }
                }

                return substrings.toArray(StringUtil.EMPTY_STRING_ARRAY);
            }
        }
    }

    public static String[] splitByFirstSeparator(String str, char separator) {
        if (str == null) {
            return StringUtil.EMPTY_STRING_ARRAY;
        }
        int index = str.indexOf(separator);
        if (index == -1) {
            return new String[]{str};
        }
        return new String[]{str.substring(0, index), str.substring(index + 1)};
    }

    public static int encodeAndHash(String str){
        if (isBlank(str)) {
            return 0;
        }
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8)).hashCode();
    }

    public static String replace(final String text, final String searchString, final String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    public static String replace(final String text, final String searchString, final String replacement, int max) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        final int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = Math.max(increase, 0);
        increase *= max < 0 ? 16 : Math.min(max, 64);
        final StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    public static String replaceEach(final String text, final String[] searchList, final String[] replacementList,
                                      final boolean repeat, final int timeToLive) {
        if (timeToLive < 0) {
            final Set<String> searchSet = new HashSet<>(Arrays.asList(searchList));
            final Set<String> replacementSet = new HashSet<>(Arrays.asList(replacementList));
            searchSet.retainAll(replacementSet);
            if (!searchSet.isEmpty()) {
                throw new IllegalStateException("Aborting to protect against StackOverflowError - " +
                        "output of one loop is the input of another");
            }
        }

        if (isEmpty(text) || searchList == null || searchList.length == 0 ||
                replacementList == null || replacementList.length == 0) {
            return text;
        }

        final int searchLength = searchList.length;
        final int replacementLength = replacementList.length;

        // make sure lengths are ok, these need to be equal
        if (searchLength != replacementLength) {
            throw new IllegalArgumentException("Search and Replace array lengths don't match: "
                    + searchLength + " vs " + replacementLength);
        }

        final boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

        int textIndex = -1;
        int replaceIndex = -1;
        int tempIndex = -1;

        for (int i = 0; i < searchLength; i++) {
            if (noMoreMatchesForReplIndex[i] || isEmpty(searchList[i]) || replacementList[i] == null) {
                continue;
            }
            tempIndex = text.indexOf(searchList[i]);

            if (tempIndex == -1) {
                noMoreMatchesForReplIndex[i] = true;
            } else if (textIndex == -1 || tempIndex < textIndex) {
                textIndex = tempIndex;
                replaceIndex = i;
            }
        }

        if (textIndex == -1) {
            return text;
        }

        int start = 0;
        int increase = 0;

        for (int i = 0; i < searchList.length; i++) {
            if (searchList[i] == null || replacementList[i] == null) {
                continue;
            }
            final int greater = replacementList[i].length() - searchList[i].length();
            if (greater > 0) {
                increase += 3 * greater;
            }
        }
        increase = Math.min(increase, text.length() / 5);

        final StringBuilder buf = new StringBuilder(text.length() + increase);
        while (textIndex != -1) {

            for (int i = start; i < textIndex; i++) {
                buf.append(text.charAt(i));
            }
            buf.append(replacementList[replaceIndex]);

            start = textIndex + searchList[replaceIndex].length();

            textIndex = -1;
            replaceIndex = -1;
            for (int i = 0; i < searchLength; i++) {
                if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
                        searchList[i].isEmpty() || replacementList[i] == null) {
                    continue;
                }
                tempIndex = text.indexOf(searchList[i], start);

                // see if we need to keep searching for this
                if (tempIndex == -1) {
                    noMoreMatchesForReplIndex[i] = true;
                } else if (textIndex == -1 || tempIndex < textIndex) {
                    textIndex = tempIndex;
                    replaceIndex = i;
                }
            }
        }

        final int textLength = text.length();
        for (int i = start; i < textLength; i++) {
            buf.append(text.charAt(i));
        }
        final String result = buf.toString();
        if (!repeat) {
            return result;
        }

        return replaceEach(result, searchList, replacementList, repeat, timeToLive - 1);
    }

    public static boolean containsIgnoreCase(final CharSequence str, final CharSequence searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        final int len = searchStr.length();
        final int max = str.length() - len;
        for (int i = 0; i <= max; i++) {
            if (regionMatches(str, true, i, searchStr, 0, len)) {
                return true;
            }
        }
        return false;
    }

    public static boolean startWith(String source, String prefix) {
        return startWithFrom(source, prefix, 0);
    }

    public static boolean startWithFrom(String source, String prefix, int checkStartIndex) {
        int length = prefix.length();
        if (checkStartIndex < 0 || checkStartIndex + length > source.length()) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            if (prefix.charAt(i) != source.charAt(checkStartIndex++)) {
                return false;
            }
        }
        return true;
    }

    static boolean regionMatches(CharSequence cs, boolean ignoreCase, int thisStart, CharSequence substring, int start, int length) {
        if (cs instanceof String && substring instanceof String) {
            return ((String)cs).regionMatches(ignoreCase, thisStart, (String)substring, start, length);
        } else {
            int index1 = thisStart;
            int index2 = start;
            int tmpLen = length;
            int srcLen = cs.length() - thisStart;
            int otherLen = substring.length() - start;
            if (thisStart >= 0 && start >= 0 && length >= 0) {
                if (srcLen >= length && otherLen >= length) {
                    while(tmpLen-- > 0) {
                        char c1 = cs.charAt(index1++);
                        char c2 = substring.charAt(index2++);
                        if (c1 != c2) {
                            if (!ignoreCase) {
                                return false;
                            }

                            if (Character.toUpperCase(c1) != Character.toUpperCase(c2) && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                                return false;
                            }
                        }
                    }

                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * refer: org.apache.commons.lang3.StringUtils#strip
     */
    public static String strip(final String str) {
        return strip(str, null);
    }

    public static String strip(String str, final String stripChars) {
        if (isEmpty(str)) {
            return str;
        }
        str = stripStart(str, stripChars);
        return stripEnd(str, stripChars);
    }

    public static String stripStart(final String str, final String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while (start != strLen && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (start != strLen && stripChars.indexOf(str.charAt(start)) != INDEX_NOT_FOUND) {
                start++;
            }
        }
        return str.substring(start);
    }

    public static String stripEnd(final String str, final String stripChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

        if (stripChars == null) {
            while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != INDEX_NOT_FOUND) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        return CharSequenceUtils.regionMatches(cs1, false, 0, cs2, 0, cs1.length());
    }

    public static String format(String format, String... args) {
        if (isEmpty(format) || args == null || args.length == 0) {
            return EMPTY;
        }
        try {
            int cnt = 0;
            StringBuilder builder = new StringBuilder();
            int i = 0;
            while (i < format.length()) {
                if (format.charAt(i) == '%' && i + 1 < format.length()) {
                    if (format.charAt(i + 1) == 's') {
                        builder.append(args[cnt++]);
                        i = i + 2;
                        continue;
                    }
                    if (format.charAt(i + 1) == 'n') {
                        builder.append('\n');
                        i = i + 2;
                        continue;
                    }
                }
                builder.append(format.charAt(i));
                i++;
            }
            return builder.toString();
        } catch (Throwable e) {
            return EMPTY;
        }
    }

    /**
     * split str to set
      */
    public static Set<String> splitToSet(String str, char separatorChars) {
        if (isEmpty(str)) {
            return Collections.emptySet();
        }
        String[] strs = split(str, separatorChars);
        return new HashSet<>(Arrays.asList(strs));
    }

    public static boolean isNumeric(final String cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * get the first occurrence of a number in a string
     * example: 1-SNAPSHOT -> 1
     *          CR1 -> 1
     */
    public static Integer getFirstNumeric(String s) {
        for (int i = 0; i < s.length(); i++) {
            char charAt = s.charAt(i);
            if (Character.isDigit(charAt)) {
                return Character.getNumericValue(charAt);
            }
        }
        return 0;
    }

    public static boolean isNullWord(String str) {
        return equals(str, "null") || equals(str, "NULL");
    }
}

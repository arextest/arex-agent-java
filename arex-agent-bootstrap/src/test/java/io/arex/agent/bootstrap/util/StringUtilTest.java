package io.arex.agent.bootstrap.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class StringUtilTest {
    @Test
    void defaultString() {
        String actualResult = StringUtil.defaultString(null);
        assertEquals("", actualResult);

        actualResult = StringUtil.defaultString("abc");
        assertEquals("abc", actualResult);
    }

    @Test
    void defaultIfEmpty() {
        String actualResult = StringUtil.defaultIfEmpty(null, "abc");
        assertEquals("abc", actualResult);

        actualResult = StringUtil.defaultIfEmpty("", "abc");
        assertEquals("abc", actualResult);

        actualResult = StringUtil.defaultIfEmpty("abc", "def");
        assertEquals("abc", actualResult);
    }

    @Test
    void isEmpty() {
        boolean actualResult = StringUtil.isEmpty(null);
        assertTrue(actualResult);

        actualResult = StringUtil.isEmpty("");
        assertTrue(actualResult);
    }

    @Test
    void isNotEmpty() {
        boolean actualResult = StringUtil.isNotEmpty(null);
        assertFalse(actualResult);

        actualResult = StringUtil.isNotEmpty("");
        assertFalse(actualResult);
    }

    @Test
    void isBlank() {
        boolean actualResult = StringUtil.isBlank(null);
        assertTrue(actualResult);

        actualResult = StringUtil.isBlank("a c");
        assertFalse(actualResult);

        actualResult = StringUtil.isBlank(" ");
        assertTrue(actualResult);
    }

    @Test
    void replace() {
        String source = "abcabcabc";
        String actualResult = StringUtil.replace(source, "a", "A");
        assertEquals("AbcAbcAbc", actualResult);

        actualResult = StringUtil.replace(source, "a", "A", 2);
        assertEquals("AbcAbcabc", actualResult);
    }

    @Test
    void join() {
        String actualResult = StringUtil.join(null, ",");
        assertNull(actualResult);

        actualResult = StringUtil.join(Collections.emptyList(), ",");
        assertEquals(StringUtil.EMPTY, actualResult);

        actualResult = StringUtil.join(Collections.singleton("a"), ",");
        assertEquals("a", actualResult);

        String[] array = new String[] {"a", "b", "c"};
        actualResult = StringUtil.join(Arrays.asList(array), ",");
        assertEquals("a,b,c", actualResult);
    }

    @Test
    void join_array() {
        String actualResult = StringUtil.join(",", null);
        assertNull(actualResult);

        actualResult = StringUtil.join(",");
        assertEquals("", actualResult);

        actualResult = StringUtil.join(",", "a");
        assertEquals("a", actualResult);

        actualResult = StringUtil.join(",", "a", "b", null, "d");
        assertEquals("a,b,,d", actualResult);
    }

    @Test
    void asMap() {
        Map<String, String> actualResult = StringUtil.asMap(null);
        assertTrue(actualResult.isEmpty());

        actualResult = StringUtil.asMap("a=b=c;c=d");
        assertEquals(1, actualResult.size());
    }

    @Test
    void removeShadePrefix() {
        String actualResult = StringUtil.removeShadePrefix("shaded.net.bytebuddy");
        assertEquals("net.bytebuddy", actualResult);
    }

    @Test
    void substring() {
        String actualResult = StringUtil.substring(null, 1);
        assertNull(actualResult);

        actualResult = StringUtil.substring("abc", -2);
        assertEquals("bc", actualResult);

        actualResult = StringUtil.substring("abc", -5);
        assertEquals("abc", actualResult);
    }

    @Test
    void substring_start_end() {
        assertNull(StringUtil.substring(null, 0, 0));
        assertEquals("", StringUtil.substring("", 0 ,  0));
        assertEquals("ab", StringUtil.substring("abc", 0, 2));
        assertEquals("", StringUtil.substring("abc", 2, 0));
        assertEquals("c", StringUtil.substring("abc", 2, 4));
        assertEquals("", StringUtil.substring("abc", 4, 6));
        assertEquals("", StringUtil.substring("abc", 2, 2));
        assertEquals("b", StringUtil.substring("abc", -2, -1));
        assertEquals("ab", StringUtil.substring("abc", -4, 2));
    }

    @Test
    void split() {
        String[] actualResult = StringUtil.split(null, ',');
        assertArrayEquals(new String[0], actualResult);

        String val = "x,,y,z,";
        actualResult = StringUtil.split(val, ',');
        assertArrayEquals(new String[] {"x", "y", "z"}, actualResult);

        actualResult = StringUtil.split(val, ',', true);
        assertArrayEquals(new String[] {"x", "", "y", "z", ""}, actualResult);
    }
    @Test
    void splitByWholeSeparator() {
        String[] actualResult = StringUtil.splitByWholeSeparator(null, ",");
        assertArrayEquals(new String[0], actualResult);

        actualResult = StringUtil.splitByWholeSeparator("", ",");
        assertArrayEquals(new String[0], actualResult);

        String val = "x,,y,z,";
        actualResult = StringUtil.splitByWholeSeparator(val, ",,");
        assertArrayEquals(new String[] {"x", "y,z,"}, actualResult);
    }

    @Test
    void encodeAndHash() {
        int actualResult = StringUtil.encodeAndHash(null);
        assertEquals(0, actualResult);

        actualResult = StringUtil.encodeAndHash("abc");
        assertEquals(2737406, actualResult);
    }

    @Test
    void containsIgnoreCase() {
        boolean actualResult = StringUtil.containsIgnoreCase(null, null);
        assertFalse(actualResult);

        actualResult = StringUtil.containsIgnoreCase("abc", "A");
        assertTrue(actualResult);

        actualResult = StringUtil.containsIgnoreCase("abc", "d");
        assertFalse(actualResult);
    }

    @Test
    void startWithFrom() {
        boolean actualResult = StringUtil.startWithFrom("abc", "a", -1);
        assertFalse(actualResult);

        actualResult = StringUtil.startWithFrom("abc", "a", 1);
        assertFalse(actualResult);

        actualResult = StringUtil.startWithFrom("abc", "b", 1);
        assertTrue(actualResult);
    }

    @ParameterizedTest
    @CsvSource({
            "false, 1, a, 1, false",
            "true,  1, a, 1, false",
            "true,  1, d, 1, false",
            "true,  3, d, 1, false",
            "true,  1, d, -1, false"
    })
    void regionMatches(boolean ignoreCase, int thisStart, String substring, int length, boolean expect) {
        StringBuilder source = new StringBuilder("abc");
        boolean result = StringUtil.regionMatches(source, ignoreCase, thisStart, substring, 0, length);
        assertEquals(expect, result);
    }

    @Test
    void splitByFirstSeparator() {
        String[] actualResult = StringUtil.splitByFirstSeparator(null, ',');
        assertArrayEquals(new String[0], actualResult);

        String noSeparator = "x";
        actualResult = StringUtil.splitByFirstSeparator(noSeparator, ',');
        assertArrayEquals(new String[] {"x"}, actualResult);

        String val = "x,,y,z,";
        actualResult = StringUtil.splitByFirstSeparator(val, ',');
        assertArrayEquals(new String[] {"x", ",y,z,"}, actualResult);
    }

    @Test
    void strip() {
        assertEquals("", StringUtil.strip(""));
        assertEquals("mock", StringUtil.strip(" mock "));
    }

    @ParameterizedTest
    @CsvSource(value ={
            "null, null, null",
            "' mock', null, mock",
            "'mock', '', mock",
            "mock, mo, ck",

    }, nullValues={"null"})
    void stripStart(String source, String strip, String expect) {
        assertEquals(expect, StringUtil.stripStart(source, strip));
    }

    @ParameterizedTest
    @CsvSource(value ={
            "null, null, null",
            "'mock ', null, mock",
            "'mock', '', mock",
            "mock, ck, mo",

    }, nullValues={"null"})
    void stripEnd(String source, String strip, String expect) {
        assertEquals(expect, StringUtil.stripEnd(source, strip));
    }

    @ParameterizedTest
    @CsvSource(value ={
            "null, null, true",
            "mock, null, false",
            "mock, moc, false",
            "mock, mock, true"
    }, nullValues={"null"})
    void equals(String source, String target, boolean expect) {
        assertEquals(expect, StringUtil.equals(source, target));
    }

    @Test
    void testFormat() {
        String format = "%s %nrequest: %s, %nresponse: %s";
        String actualResult = StringUtil.format(format, "acasdasdada", "badadadadas", "ccccccc");
        String expectResult = "acasdasdada \nrequest: badadadadas, \nresponse: ccccccc";
        assertEquals(expectResult, actualResult);

        // test args not equal number of %s
        actualResult = StringUtil.format(format, "badadadadas", "ccccccc");
        Assertions.assertEquals(StringUtil.EMPTY, actualResult);

        // null format
        actualResult = StringUtil.format(null, "badadadadas", "ccccccc");
        Assertions.assertEquals(StringUtil.EMPTY, actualResult);

        // null args
        actualResult = StringUtil.format(format, null);
        Assertions.assertEquals(StringUtil.EMPTY, actualResult);

        String format2 = "% %nrequest: %s, %nresponse: %s";
        actualResult = StringUtil.format(format2, "badadadadas", "ccccccc");
        Assertions.assertEquals("% \nrequest: badadadadas, \nresponse: ccccccc", actualResult);

        format2 = "%s request";
        actualResult = StringUtil.format(format2, "badadadadas");
        Assertions.assertEquals("badadadadas request", actualResult);

        format2 = "% %trequest: %s, %dresponse: %s";
        actualResult = StringUtil.format(format2, "badadadadas", "ccccccc");
        Assertions.assertEquals("% %trequest: badadadadas, %dresponse: ccccccc", actualResult);
    }

    @Test
    void testSplitToSet() {
        // null string
        Set<String> nullSet = StringUtil.splitToSet(null, ',');
        assertEquals(0, nullSet.size());

        // empty string
        Set<String> emptySet = StringUtil.splitToSet("", ',');
        assertEquals(0, emptySet.size());

        String s = "aaa,bb,c";
        Set<String> set = StringUtil.splitToSet(s, ',');
        assertEquals(3, set.size());
        assertTrue(set.contains("aaa"));
        assertTrue(set.contains("bb"));
        assertTrue(set.contains("c"));
    }

    @Test
    void testSplitToList() {
        // null string
        List<String> nullSet = StringUtil.splitToList(null, ',');
        assertEquals(0, nullSet.size());

        // empty string
        List<String> emptySet = StringUtil.splitToList("", ',');
        assertEquals(0, emptySet.size());

        String s = "aaa,bb,c";
        List<String> set = StringUtil.splitToList(s, ',');
        assertEquals(3, set.size());
        assertTrue(set.contains("aaa"));
        assertTrue(set.contains("bb"));
        assertTrue(set.contains("c"));
    }

    @Test
    void testIsNumeric() {
        String s = "123";
        assertTrue(StringUtil.isNumeric(s));
        String s2 = "123.1";
        assertFalse(StringUtil.isNumeric(s2));
        String s3 = "";
        assertFalse(StringUtil.isNumeric(s3));
        String s4 = null;
        assertFalse(StringUtil.isNumeric(s4));
    }

    @Test
    void testGetFirstNumeric() {
        String s = "1-SNAPSHOT";
        assertEquals(1, StringUtil.getFirstNumeric(s));
        String s2 = "RC2";
        assertEquals(2, StringUtil.getFirstNumeric(s2));
        String s3 = "SNAPSHOT";
        assertEquals(0, StringUtil.getFirstNumeric(s3));
    }

    @Test
    void isNullWord() {
        assertTrue(StringUtil.isNullWord("null"));
        assertTrue(StringUtil.isNullWord("NULL"));
        assertFalse(StringUtil.isNullWord("mock"));
    }

    @Test
    void startWith() {
        assertTrue(StringUtil.startWith("mock", "m"));
        assertFalse(StringUtil.startWith("mock", "o"));
        assertFalse(StringUtil.startWith(null, "M"));
        assertFalse(StringUtil.startWith("mock", null));
        assertTrue(StringUtil.startWith(null, null));
        assertFalse(StringUtil.startWith("", "M"));
    }

    @Test
    void countMatches() {
        assertEquals(0, StringUtil.countMatches(null, null));
        assertEquals(0, StringUtil.countMatches(null, ""));
        assertEquals(0, StringUtil.countMatches("", null));
        assertEquals(0, StringUtil.countMatches("", ""));
        assertEquals(0, StringUtil.countMatches("mock", "M"));
        assertEquals(1, StringUtil.countMatches("mock", "m"));
    }

    @Test
    void substringBefore() {
        assertEquals(null, StringUtil.substringBefore(null, null));
        assertEquals("", StringUtil.substringBefore("", null));
        assertEquals("", StringUtil.substringBefore("", ""));
        assertEquals("", StringUtil.substringBefore("mock", ""));
        assertEquals("mock", StringUtil.substringBefore("mock", null));
        assertEquals("mock", StringUtil.substringBefore("mock", "cd"));
        assertEquals("mo", StringUtil.substringBefore("mock", "c"));
    }

    @Test
    void subStringAfter() {
        assertEquals(null, StringUtil.substringAfter(null, null));
        assertEquals("", StringUtil.substringAfter("", null));
        assertEquals("", StringUtil.substringAfter("", ""));
        assertEquals("mock", StringUtil.substringAfter("mock", ""));
        assertEquals("", StringUtil.substringAfter("mock", null));
        assertEquals("ck", StringUtil.substringAfter("mock", "o"));
        assertEquals("k", StringUtil.substringAfter("mock", "c"));
        assertEquals("mock", StringUtil.substringAfter("mock", "cd"));
    }

    @Test
    void splitByLastSeparator() {
        String[] actualResult = StringUtil.splitByLastSeparator(null, ',');
        assertArrayEquals(new String[0], actualResult);

        String noSeparator = "x";
        actualResult = StringUtil.splitByLastSeparator(noSeparator, ',');
        assertArrayEquals(new String[] {"x"}, actualResult);

        String val = "x,,y,z";
        actualResult = StringUtil.splitByLastSeparator(val, ',');
        assertArrayEquals(new String[] {"x,,y", "z"}, actualResult);
    }

    @Test
    void mapToString() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "b");
        map.put("c", "d");
        assertEquals("a=b;c=d;", StringUtil.mapToString(map));
    }
}

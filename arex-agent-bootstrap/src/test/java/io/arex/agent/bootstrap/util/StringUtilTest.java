package io.arex.agent.bootstrap.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StringUtilTest {
    @Test
    void defaultString() {
        String actualResult = StringUtil.defaultString(null);
        assertEquals("", actualResult);

        actualResult = StringUtil.defaultString("abc");
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
        assertNull(actualResult);

        actualResult = StringUtil.join(Collections.singleton("a"), ",");
        assertEquals("a", actualResult);

        String[] array = new String[] {"a", "b", "c"};
        actualResult = StringUtil.join(Arrays.asList(array), ",");
        assertEquals("a,b,c", actualResult);
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

    @Test
    void regionMatches() {
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
}
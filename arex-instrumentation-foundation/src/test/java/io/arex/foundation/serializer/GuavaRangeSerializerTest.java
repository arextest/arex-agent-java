package io.arex.foundation.serializer;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Range;
import io.arex.foundation.serializer.gson.GsonSerializer;
import io.arex.foundation.serializer.jackson.JacksonSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GuavaRangeSerializerTest {
    static Range<Integer> range1, range2, range3, range4;
    @BeforeAll
    static void setUp() {
        range1 = Range.closed(1, 10);
        range2 = Range.lessThan(2);
        range3 = Range.greaterThan(3);
        range4 = Range.openClosed(4, 40);
    }

    @AfterAll
    static void tearDown() {
        range1 = null;
        range2 = null;
        range3 = null;
        range4 = null;
    }

    @Test
    void testRangeSerializeWithGson() {
        final String range1Json = GsonSerializer.INSTANCE.serialize(range1);
        final Range deserializeRange1 = GsonSerializer.INSTANCE.deserialize(range1Json, Range.class);
        assertEquals(range1.lowerEndpoint(), deserializeRange1.lowerEndpoint());
        assertEquals(range1.upperEndpoint(), deserializeRange1.upperEndpoint());

        final String range2Json = GsonSerializer.INSTANCE.serialize(range2);
        final Range deserializeRange2 = GsonSerializer.INSTANCE.deserialize(range2Json, Range.class);
        assertTrue(range2.contains(1));
        assertTrue(deserializeRange2.contains(1));
        assertEquals(range2.upperEndpoint(), deserializeRange2.upperEndpoint());
        assertFalse(range2.hasLowerBound());
        assertFalse(deserializeRange2.hasLowerBound());

        final String range3Json = GsonSerializer.INSTANCE.serialize(range3);
        final Range deserializeRange3 = GsonSerializer.INSTANCE.deserialize(range3Json, Range.class);
        assertEquals(range3.lowerEndpoint(), deserializeRange3.lowerEndpoint());
        assertFalse(range3.hasUpperBound());
        assertFalse(deserializeRange3.hasUpperBound());

        final String range4Json = GsonSerializer.INSTANCE.serialize(range4);
        final Range deserializeRange4 = GsonSerializer.INSTANCE.deserialize(range4Json, Range.class);
        assertEquals(range4.lowerEndpoint(), deserializeRange4.lowerEndpoint());
        assertEquals(range4.upperEndpoint(), deserializeRange4.upperEndpoint());
        assertEquals(range4.lowerBoundType(), deserializeRange4.lowerBoundType());
        assertEquals(range4.upperBoundType(), deserializeRange4.upperBoundType());
    }

    @Test
    void testRangeSerializeWithJackson() throws Throwable {
        final String range1Json = JacksonSerializer.INSTANCE.serialize(range1);
        final Range deserializeRange1 = JacksonSerializer.INSTANCE.deserialize(range1Json, Range.class);
        assertEquals(range1.lowerEndpoint(), deserializeRange1.lowerEndpoint());
        assertEquals(range1.upperEndpoint(), deserializeRange1.upperEndpoint());

        final String range2Json = JacksonSerializer.INSTANCE.serialize(range2);
        final Range deserializeRange2 = JacksonSerializer.INSTANCE.deserialize(range2Json, Range.class);
        assertTrue(range2.contains(1));
        assertTrue(deserializeRange2.contains(1));
        assertFalse(range2.hasLowerBound());
        assertFalse(deserializeRange2.hasLowerBound());
        assertEquals(range2.upperEndpoint(), deserializeRange2.upperEndpoint());

        final String range3Json = JacksonSerializer.INSTANCE.serialize(range3);
        final Range deserializeRange3 = JacksonSerializer.INSTANCE.deserialize(range3Json, Range.class);
        assertEquals(range3.lowerEndpoint(), deserializeRange3.lowerEndpoint());
        assertFalse(range3.hasUpperBound());
        assertFalse(deserializeRange3.hasUpperBound());

        final String range4Json = JacksonSerializer.INSTANCE.serialize(range4);
        final Range deserializeRange4 = JacksonSerializer.INSTANCE.deserialize(range4Json, Range.class);
        assertEquals(range4.lowerEndpoint(), deserializeRange4.lowerEndpoint());
        assertEquals(range4.upperEndpoint(), deserializeRange4.upperEndpoint());
        assertEquals(range4.lowerBoundType(), deserializeRange4.lowerBoundType());
        assertEquals(range4.upperBoundType(), deserializeRange4.upperBoundType());
    }
}

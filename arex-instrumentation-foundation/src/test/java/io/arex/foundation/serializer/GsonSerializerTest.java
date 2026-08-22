package io.arex.foundation.serializer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.internal.LinkedTreeMap;
import io.arex.agent.bootstrap.internal.Pair;
import io.arex.foundation.serializer.gson.GsonSerializer;
import io.arex.foundation.serializer.jackson.JacksonSerializer;
import io.arex.inst.runtime.util.TypeUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.lang.reflect.Type;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GsonSerializerTest {

    @Test
    public void testSqlDate() throws InterruptedException {
        java.sql.Date expectedSqlDate = new java.sql.Date(System.currentTimeMillis());
        String expectedJson = GsonSerializer.INSTANCE.serialize(expectedSqlDate);
        System.out.println(expectedJson);
        java.util.Date actualSqlDate = GsonSerializer.INSTANCE.deserialize(expectedJson, java.sql.Date.class);
        String actualJson = GsonSerializer.INSTANCE.serialize(actualSqlDate);
        assertEquals(expectedSqlDate, actualSqlDate);
        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testSqlTime() throws InterruptedException {
        Time expectedTime = new Time(System.currentTimeMillis());
        String expectedJson = GsonSerializer.INSTANCE.serialize(expectedTime);
        System.out.println(expectedJson);
        java.util.Date actualTime = GsonSerializer.INSTANCE.deserialize(expectedJson, Time.class);
        String actualJson = GsonSerializer.INSTANCE.serialize(actualTime);
        assertEquals(expectedTime, actualTime);
        assertEquals(expectedJson, actualJson);
    }

    /**
     * https://github.com/arextest/arex-agent-java/issues/588
     */
    @ParameterizedTest
    @MethodSource("zonedDateTimeCase")
    public void testZonedDateTime(ZonedDateTime expected) {
        String json = GsonSerializer.INSTANCE.serialize(expected);
        ZonedDateTime actualResult = GsonSerializer.INSTANCE.deserialize(json, ZonedDateTime.class);
        assertEquals(expected, actualResult);
        // the zone id should not be replaced by the zone offset
        assertEquals(expected.getZone(), actualResult.getZone());
        // the offset keeps the local time repeated in a dst overlap distinguishable
        assertEquals(expected.getOffset(), actualResult.getOffset());
        assertEquals(json, GsonSerializer.INSTANCE.serialize(actualResult));
    }

    static Stream<Arguments> zonedDateTimeCase() {
        // 2025-11-02T01:30 of America/New_York happens twice, once with -04:00 and once with -05:00
        ZonedDateTime dstOverlap = ZonedDateTime.of(2025, 11, 2, 1, 30, 0, 0, ZoneId.of("America/New_York"));
        return Stream.of(
                Arguments.arguments(ZonedDateTime.of(2020, 6, 9, 9, 0, 0, 123456789, ZoneId.of("Asia/Shanghai"))),
                Arguments.arguments(ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))),
                Arguments.arguments(ZonedDateTime.now(ZoneOffset.UTC)),
                Arguments.arguments(ZonedDateTime.now(ZoneId.of("GMT-01:00"))),
                Arguments.arguments(dstOverlap.withEarlierOffsetAtOverlap()),
                Arguments.arguments(dstOverlap.withLaterOffsetAtOverlap()),
                // zones that still had an offset with seconds, dropping them shifts the instant
                Arguments.arguments(ZonedDateTime.of(1900, 1, 1, 12, 0, 0, 0, ZoneId.of("Asia/Shanghai"))),
                Arguments.arguments(Instant.EPOCH.atZone(ZoneId.of("Africa/Monrovia"))),
                Arguments.arguments(ZonedDateTime.of(2026, 1, 1, 9, 0, 0, 0, ZoneOffset.ofTotalSeconds(8 * 3600 + 30))),
                // year of era would turn this into the year 100 AD
                Arguments.arguments(ZonedDateTime.of(-100, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)));
    }

    /**
     * both serializers must emit the same bytes, a case can be recorded with one and replayed with
     * the other
     */
    @Test
    public void testZonedDateTimeSameFormatAsJackson() throws Throwable {
        ZonedDateTime expected = ZonedDateTime.of(2020, 6, 9, 9, 0, 0, 123456789, ZoneId.of("Asia/Shanghai"));
        assertEquals(JacksonSerializer.INSTANCE.serialize(expected), GsonSerializer.INSTANCE.serialize(expected));
    }

    @Test
    public void testTimeSerializeAndDeserialize() throws Exception {
        TimeTestInfo expectedTimeTest = new TimeTestInfo(LocalDateTime.now());
        String expectedJson = GsonSerializer.INSTANCE.serialize(expectedTimeTest);
        System.out.println(expectedJson);

        String expectedBytesToJson = GsonSerializer.INSTANCE.serialize(expectedTimeTest);
        System.out.println(expectedBytesToJson);

        assert expectedJson.equals(expectedBytesToJson);

        TimeUnit.SECONDS.sleep(1);

        TimeTestInfo deserializedTimeTest = GsonSerializer.INSTANCE.deserialize(expectedJson, TimeTestInfo.class);
        assert deserializedTimeTest != null;
        assert expectedTimeTest.getCalendar().equals(deserializedTimeTest.getCalendar());
        assert expectedTimeTest.getGregorianCalendar().equals(deserializedTimeTest.getGregorianCalendar());
        assert expectedTimeTest.getGregorianCalendar().equals(deserializedTimeTest.getGregorianCalendar());
        assert expectedTimeTest.getXmlGregorianCalendar().equals(deserializedTimeTest.getXmlGregorianCalendar());

        assert expectedTimeTest.getLocalDate().equals(deserializedTimeTest.getLocalDate());
        assert expectedTimeTest.getLocalTime().equals(deserializedTimeTest.getLocalTime());
        assert expectedTimeTest.getLocalDateTime().equals(deserializedTimeTest.getLocalDateTime());

        assert expectedTimeTest.getTimestamp().equals(deserializedTimeTest.getTimestamp());
        assert expectedTimeTest.getDate().equals(deserializedTimeTest.getDate());

        assert expectedTimeTest.getInstant().equals(deserializedTimeTest.getInstant());
        assert expectedTimeTest.getZonedDateTime().equals(deserializedTimeTest.getZonedDateTime());

        assert expectedTimeTest.getJodaLocalDate().equals(deserializedTimeTest.getJodaLocalDate());
        assert expectedTimeTest.getJodaLocalTime().equals(deserializedTimeTest.getJodaLocalTime());
        assert expectedTimeTest.getJodaLocalDateTime().equals(deserializedTimeTest.getJodaLocalDateTime());
        assert expectedTimeTest.getDateTime().equals(deserializedTimeTest.getDateTime());
        assert expectedTimeTest.getTimeZone1().equals(deserializedTimeTest.getTimeZone1());

        String deserializedJson = GsonSerializer.INSTANCE.serialize(deserializedTimeTest);
        System.out.println(deserializedJson);

        assert expectedJson.equals(deserializedJson);
    }

    @Test
    void serialize() {
        // null object
        assertNull(GsonSerializer.INSTANCE.serialize(null));

        // error serialize object
        assertThrows(Throwable.class, () -> GsonSerializer.INSTANCE.serialize(Thread.currentThread()));
    }

    @Test
    void deserializeClass() {
        // null object
        assertNull(GsonSerializer.INSTANCE.deserialize(null, String.class));

        // null class
        assertNull(GsonSerializer.INSTANCE.deserialize("", null));

        // deserialize object
        String json  = GsonSerializer.INSTANCE.serialize(LocalDateTime.now());
        assertNotNull(GsonSerializer.INSTANCE.deserialize(json, LocalDateTime.class));
    }

    @Test
    void deserializeType() {
        // null object
        assertNull(GsonSerializer.INSTANCE.deserialize(null, TypeUtil.forName(TypeUtil.getName(LocalDateTime.now()))));

        // null type
        assertNull(GsonSerializer.INSTANCE.deserialize("", TypeUtil.forName(null)));

        // deserialize object
        String json  = GsonSerializer.INSTANCE.serialize(LocalDateTime.now());
        assertNotNull(GsonSerializer.INSTANCE.deserialize(json, TypeUtil.forName(TypeUtil.getName(LocalDateTime.now()))));
    }

    @Test
    void testAddCustomSerializer() {
        Map<String, Object> map = new LinkedTreeMap<>();
        GsonSerializer.INSTANCE.addTypeSerializer(LinkedTreeMap.class, null);
        // empty map
        String json = GsonSerializer.INSTANCE.serialize(map);
        assertEquals("{}", json);
        final LinkedTreeMap deserialize = GsonSerializer.INSTANCE.deserialize(json, LinkedTreeMap.class);
        assertEquals(map, deserialize);


        map.put("key", "AREX-101-202");
        map.put("long", 2L);
        json = GsonSerializer.INSTANCE.serialize(map);
        assertEquals("{\"key\":\"AREX-101-202\",\"long\":{\"value\":2,\"type\":\"java.lang.Long\"}}", json);
        final LinkedTreeMap deserialize1 = GsonSerializer.INSTANCE.deserialize(json, LinkedTreeMap.class);
        assertEquals(map, deserialize1);

        // value is null
        map.put("null", null);
        json = GsonSerializer.INSTANCE.serialize(map);
        assertEquals("{\"key\":\"AREX-101-202\",\"long\":{\"value\":2,\"type\":\"java.lang.Long\"}}", json);
    }

    @Test
    void testFastUtil() throws Throwable {
        final IntOpenHashSet hashSet = new IntOpenHashSet();
        final String json = GsonSerializer.INSTANCE.serialize(hashSet);
        final IntSet deserialize = GsonSerializer.INSTANCE.deserialize(json, IntSet.class);
        assert deserialize != null;
        assertEquals(hashSet, deserialize);
    }

    @Test
    void testClass() {
        String className = GsonSerializer.INSTANCE.serialize(String.class);
        assertEquals("\"java.lang.String\"", className);
        Object deserialize1 = GsonSerializer.INSTANCE.deserialize(className, Class.class);
        assertEquals(String.class, deserialize1);
    }

    @Test
    void testNullField() {
        final Pair<LocalDateTime, Long> pairFirstNull = Pair.of(null, System.currentTimeMillis());
        final String genericFirstNull = TypeUtil.getName(pairFirstNull);
        assertEquals("io.arex.agent.bootstrap.internal.Pair-java.lang.String,java.lang.Long", genericFirstNull);
        String json = GsonSerializer.INSTANCE.serialize(pairFirstNull);
        System.out.println(json);
        Type type1 = TypeUtil.forName(genericFirstNull);
        Pair firstNulldeserialize = GsonSerializer.INSTANCE.deserialize(json, type1);
        assertNull(firstNulldeserialize.getFirst());

        final Pair pairNull = Pair.of(System.currentTimeMillis(), null);
        final String genericNull = TypeUtil.getName(pairNull);
        assertEquals("io.arex.agent.bootstrap.internal.Pair-java.lang.Long,java.lang.String", genericNull);
        json = GsonSerializer.INSTANCE.serialize(pairNull);
        System.out.println(json);
        Type type2 = TypeUtil.forName(genericNull);
        firstNulldeserialize = GsonSerializer.INSTANCE.deserialize(json, type2);
        assertNull(firstNulldeserialize.getSecond());
    }
}

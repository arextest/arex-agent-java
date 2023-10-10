package io.arex.foundation.serializer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import com.google.gson.internal.LinkedTreeMap;
import io.arex.inst.runtime.util.TypeUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

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

        assert expectedTimeTest.getJodaLocalDate().equals(deserializedTimeTest.getJodaLocalDate());
        assert expectedTimeTest.getJodaLocalTime().equals(deserializedTimeTest.getJodaLocalTime());
        assert expectedTimeTest.getJodaLocalDateTime().equals(deserializedTimeTest.getJodaLocalDateTime());
        assert expectedTimeTest.getDateTime().equals(deserializedTimeTest.getDateTime());

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


        map.put("key", "value");
        map.put("long", 2L);
        json = GsonSerializer.INSTANCE.serialize(map);
        assertEquals("{\"key\":\"value\",\"long-java.lang.Long\":2}", json);
        final LinkedTreeMap deserialize1 = GsonSerializer.INSTANCE.deserialize(json, LinkedTreeMap.class);
        assertEquals(map, deserialize1);

        // value is null
        map.put("null", null);
        json = GsonSerializer.INSTANCE.serialize(map);
        assertEquals("{\"key\":\"value\",\"long-java.lang.Long\":2}", json);
    }

    @Test
    void testFastUtil() throws Throwable {
        final IntOpenHashSet hashSet = new IntOpenHashSet();
        final String json = GsonSerializer.INSTANCE.serialize(hashSet);
        final IntSet deserialize = GsonSerializer.INSTANCE.deserialize(json, IntSet.class);
        assert deserialize != null;
        assertEquals(hashSet, deserialize);
    }
}
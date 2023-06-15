package io.arex.foundation.serializer;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.runtime.util.TypeUtil;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class JacksonSerializerTest {
    @Test
    void testLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        String json = JacksonSerializer.INSTANCE.serialize(now);
        LocalDateTime actualResult = JacksonSerializer.INSTANCE.deserialize(json, LocalDateTime.class);
        assertEquals(now, actualResult);
    }

    @Test
    void testLocalTime() {
        LocalDateTime now = LocalDateTime.now();
        String json = JacksonSerializer.INSTANCE.serialize(now);
        LocalDateTime actualResult = JacksonSerializer.INSTANCE.deserialize(json, LocalDateTime.class);
        assertEquals(now, actualResult);
    }

    @Test
    void testNullList() {
        final List<Object> list = new ArrayList<>();
        list.add(null);
        String json = JacksonSerializer.INSTANCE.serialize(list);
        final String name = TypeUtil.getName(list);
        final List<Object> result = JacksonSerializer.INSTANCE.deserialize(json, TypeUtil.forName(name));
        assert result != null;
        assertEquals(list.size(), result.size());
        assertNull(result.get(0));
    }

    @Test
    public void testSqlDate() throws InterruptedException {
        java.sql.Date expectedSqlDate = new java.sql.Date(System.currentTimeMillis());
        String expectedJson = JacksonSerializer.INSTANCE.serialize(expectedSqlDate);
        Thread.sleep(10);
        System.out.println(expectedJson);
        java.util.Date actualSqlDate = JacksonSerializer.INSTANCE.deserialize(expectedJson, java.sql.Date.class);
        String actualJson = JacksonSerializer.INSTANCE.serialize(actualSqlDate);
        assertEquals(expectedSqlDate, actualSqlDate);
        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testSqlTime() throws InterruptedException {
        Time expectedTime = new Time(System.currentTimeMillis());
        String expectedJson = JacksonSerializer.INSTANCE.serialize(expectedTime);
        Thread.sleep(10);
        System.out.println(expectedJson);
        java.util.Date actualTime = JacksonSerializer.INSTANCE.deserialize(expectedJson, Time.class);
        String actualJson = JacksonSerializer.INSTANCE.serialize(actualTime);
        assertEquals(expectedTime, actualTime);
        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testTimeSerializeAndDeserialize() throws Exception {
        TimeTestInfo expectedTimeTest = new TimeTestInfo(LocalDateTime.now());
        String expectedJson = JacksonSerializer.INSTANCE.serialize(expectedTimeTest);
        System.out.println(expectedJson);

        String expectedBytesToJson = JacksonSerializer.INSTANCE.serialize(expectedTimeTest);
        System.out.println(expectedBytesToJson);

        assert expectedJson.equals(expectedBytesToJson);

        TimeUnit.SECONDS.sleep(1);

        TimeTestInfo deserializedTimeTest = JacksonSerializer.INSTANCE.deserialize(expectedJson, TimeTestInfo.class);
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

        String deserializedJson = JacksonSerializer.INSTANCE.serialize(deserializedTimeTest);
        System.out.println(deserializedJson);

        assert expectedJson.equals(deserializedJson);
    }

    @Test
    void serialize() {
        // null object
        assertNull(JacksonSerializer.INSTANCE.serialize(null));

        // error serialize object
        assertNull(JacksonSerializer.INSTANCE.serialize(JacksonSerializer.class.getDeclaredMethods()));
    }

    @Test
    void deserializeClass() {
        // null object
        assertNull(JacksonSerializer.INSTANCE.deserialize(null, String.class));

        // error deserialize object
        String json  = JacksonSerializer.INSTANCE.serialize(LocalDateTime.now());
        assertNull(JacksonSerializer.INSTANCE.deserialize(json, LocalTime.class));
    }

    @Test
    void deserializeType() {
        // null object
        assertNull(JacksonSerializer.INSTANCE.deserialize(null, TypeUtil.forName(TypeUtil.getName(LocalTime.now()))));

        // error deserialize object
        String json  = JacksonSerializer.INSTANCE.serialize(LocalDateTime.now());
        assertNull(JacksonSerializer.INSTANCE.deserialize(json, TypeUtil.forName(TypeUtil.getName(LocalTime.now()))));
    }

}
package io.arex.foundation.serializer;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Time;
import java.time.LocalDateTime;
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

        String deserializedJson = GsonSerializer.INSTANCE.serialize(deserializedTimeTest);
        System.out.println(deserializedJson);

        assert expectedJson.equals(deserializedJson);
    }
}
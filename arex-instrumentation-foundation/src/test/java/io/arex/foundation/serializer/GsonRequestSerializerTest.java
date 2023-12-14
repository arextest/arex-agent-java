package io.arex.foundation.serializer;

import io.arex.foundation.serializer.gson.GsonSerializer;
import io.arex.foundation.serializer.gson.GsonRequestSerializer;
import io.arex.foundation.serializer.jackson.JacksonRequestSerializer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GsonRequestSerializerTest {
    public static final GsonRequestSerializer REQUEST_SERIALIZER_INSTANCE = new GsonRequestSerializer();

    @Test
    void serialize() throws Throwable {
        TimeTestInfo timeTestInfo = new TimeTestInfo();
        String json1 = REQUEST_SERIALIZER_INSTANCE.serialize(timeTestInfo);
        System.out.println(json1);
        ZeroSecondTimeTestInfo testInfo = GsonSerializer.INSTANCE.deserialize(json1, ZeroSecondTimeTestInfo.class);
        ZeroSecondTimeTestInfo zeroSecondTimeTestInfo = new ZeroSecondTimeTestInfo(timeTestInfo);

        assertEquals(testInfo.getDateTime(), zeroSecondTimeTestInfo.getDateTime());
        assertEquals(testInfo.getJodaLocalDateTime(), zeroSecondTimeTestInfo.getJodaLocalDateTime());
        assertEquals(testInfo.getJodaLocalDate(), zeroSecondTimeTestInfo.getJodaLocalDate());
        assertEquals(testInfo.getJodaLocalTime(), zeroSecondTimeTestInfo.getJodaLocalTime());
        assertEquals(testInfo.getTimestamp(), zeroSecondTimeTestInfo.getTimestamp());
        assertEquals(testInfo.getDate(), zeroSecondTimeTestInfo.getDate());
        assertEquals(testInfo.getCalendar(), zeroSecondTimeTestInfo.getCalendar());
        assertEquals(testInfo.getGregorianCalendar(), zeroSecondTimeTestInfo.getGregorianCalendar());
        assertEquals(testInfo.getXmlGregorianCalendar(), zeroSecondTimeTestInfo.getXmlGregorianCalendar());
        assertEquals(testInfo.getLocalDateTime(), zeroSecondTimeTestInfo.getLocalDateTime());
        assertEquals(testInfo.getLocalDate(), zeroSecondTimeTestInfo.getLocalDate());
        assertEquals(testInfo.getLocalTime(), zeroSecondTimeTestInfo.getLocalTime());
        assertEquals(testInfo.getOffsetDateTime(), zeroSecondTimeTestInfo.getOffsetDateTime());
        assertEquals(testInfo.getTimeZone1(), zeroSecondTimeTestInfo.getTimeZone1());
    }

    @Test
    void testUUID() throws Throwable {
        TestClass testClass = new TestClass();
        String uuid = UUID.randomUUID().toString();
        testClass.setStringField(uuid);

        String requestTestString = REQUEST_SERIALIZER_INSTANCE.serialize(testClass);
        System.out.println(requestTestString);
        assertEquals("{\"stringField\":\"00000000-0000-0000-0000-000000000000\"}", requestTestString);

        String responseTestString = GsonSerializer.INSTANCE.serialize(testClass);
        TestClass deserializeTestClass = GsonSerializer.INSTANCE.deserialize(responseTestString, TestClass.class);
        System.out.println(responseTestString);
        assertEquals(testClass.getStringField(), deserializeTestClass.getStringField());
    }

    @Test
    void testNullString() throws Throwable {
        TestClass testClass = new TestClass();
        String requestTestString = REQUEST_SERIALIZER_INSTANCE.serialize(testClass);
        String responseTestString = GsonSerializer.INSTANCE.serialize(testClass);
        assertEquals(requestTestString, responseTestString);

        testClass.setStringField("");
        requestTestString = REQUEST_SERIALIZER_INSTANCE.serialize(testClass);
        responseTestString = GsonSerializer.INSTANCE.serialize(testClass);
        assertEquals(requestTestString, responseTestString);
    }

    @Test
    void testIp() throws Throwable {
        TestClass testClass = new TestClass();
        String normalIp = "10.32.179.147";
        testClass.setStringField(normalIp);
        String requestTestString = REQUEST_SERIALIZER_INSTANCE.serialize(testClass);
        System.out.println(requestTestString);
        assertEquals("{\"stringField\":\"1.1.1.1\"}", requestTestString);

        String responseTestString = GsonSerializer.INSTANCE.serialize(testClass);
        assertEquals("{\"stringField\":\"10.32.179.147\"}", responseTestString);

        String abnormalIp = "10.123.11";
        testClass.setStringField(abnormalIp);
        requestTestString = REQUEST_SERIALIZER_INSTANCE.serialize(testClass);
        System.out.println(requestTestString);
        assertEquals("{\"stringField\":\"10.123.11\"}", requestTestString);
    }

    static class TestClass {
        private String stringField;

        public String getStringField() {
            return stringField;
        }

        public void setStringField(String stringField) {
            this.stringField = stringField;
        }
    }
}

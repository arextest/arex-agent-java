package io.arex.foundation.serializer;

import io.arex.foundation.serializer.jackson.JacksonRequestSerializer;
import io.arex.foundation.serializer.jackson.JacksonSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.aop.framework.ReflectiveMethodInvocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JacksonRequestSerializerTest {
    public static final JacksonRequestSerializer JACKSON_REQUEST_SERIALIZER = new JacksonRequestSerializer();
    @Test
    void serialize() throws Throwable {
        TimeTestInfo timeTestInfo = new TimeTestInfo();
        String json1 = JACKSON_REQUEST_SERIALIZER.serialize(timeTestInfo);
        System.out.println(json1);

        ZeroSecondTimeTestInfo testInfo = JacksonSerializer.INSTANCE.deserialize(json1, ZeroSecondTimeTestInfo.class);
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
        GsonRequestSerializerTest.TestClass testClass = new GsonRequestSerializerTest.TestClass();
        String uuid = UUID.randomUUID().toString();
        testClass.setStringField(uuid);

        String requestTestString = JACKSON_REQUEST_SERIALIZER.serialize(testClass);
        System.out.println(requestTestString);
        assertEquals("{\"stringField\":\"00000000-0000-0000-0000-000000000000\"}", requestTestString);

        String responseTestString = JacksonSerializer.INSTANCE.serialize(testClass);
        GsonRequestSerializerTest.TestClass deserializeTestClass = JacksonSerializer.INSTANCE.deserialize(responseTestString, GsonRequestSerializerTest.TestClass.class);
        System.out.println(responseTestString);
        assertEquals(testClass.getStringField(), deserializeTestClass.getStringField());
    }

    @Test
    void testNullString() throws Throwable {
        GsonRequestSerializerTest.TestClass testClass = new GsonRequestSerializerTest.TestClass();
        String requestTestString = JACKSON_REQUEST_SERIALIZER.serialize(testClass);
        String responseTestString = JacksonSerializer.INSTANCE.serialize(testClass);
        assertEquals(requestTestString, responseTestString);

        testClass.setStringField("");
        requestTestString = JACKSON_REQUEST_SERIALIZER.serialize(testClass);
        responseTestString = JacksonSerializer.INSTANCE.serialize(testClass);
        assertEquals(requestTestString, responseTestString);
    }

    @Test
    void testIp() throws Throwable {
        GsonRequestSerializerTest.TestClass testClass = new GsonRequestSerializerTest.TestClass();
        String normalIp = "10.32.179.147";
        testClass.setStringField(normalIp);
        String requestTestString = JACKSON_REQUEST_SERIALIZER.serialize(testClass);
        System.out.println(requestTestString);
        assertEquals("{\"stringField\":\"1.1.1.1\"}", requestTestString);

        String responseTestString = JacksonSerializer.INSTANCE.serialize(testClass);
        assertEquals("{\"stringField\":\"10.32.179.147\"}", responseTestString);

        String abnormalIp = "10.123.11";
        testClass.setStringField(abnormalIp);
        requestTestString = JACKSON_REQUEST_SERIALIZER.serialize(testClass);
        System.out.println(requestTestString);
        assertEquals("{\"stringField\":\"10.123.11\"}", requestTestString);

        // not numeric
        String notIp = "退改.取消.授权.预订";
        testClass.setStringField(notIp);
        requestTestString = JACKSON_REQUEST_SERIALIZER.serialize(testClass);
        System.out.println(requestTestString);
        assertEquals("{\"stringField\":\"退改.取消.授权.预订\"}", requestTestString);
    }

    @Test
    void testMethodInvocationProceedingJoinPoint() throws Throwable {
        JacksonRequestSerializer jacksonRequestSerializer = new JacksonRequestSerializer();
        // null object
        assertNull(jacksonRequestSerializer.serialize(null));

        // error serialize object
        JacksonSerializerTest.CaseSensitive caseSensitive = new JacksonSerializerTest.CaseSensitive();
        Method method = caseSensitive.getClass().getMethod("setAmount", Float.class);
        Constructor<ReflectiveMethodInvocation> declaredConstructor =
                ReflectiveMethodInvocation.class.getDeclaredConstructor(Object.class, Object.class, Method.class, Object[].class,
                        Class.class, List.class);
        declaredConstructor.setAccessible(true);
        ReflectiveMethodInvocation reflectiveMethodInvocation = declaredConstructor.newInstance(caseSensitive, null, method, new Object[]{0.1f}, null, null);
        MethodInvocationProceedingJoinPoint joinPoint = new MethodInvocationProceedingJoinPoint(reflectiveMethodInvocation);
        String json = jacksonRequestSerializer.serialize(joinPoint);
        String expectedJson = "\"" + joinPoint.toString() + jacksonRequestSerializer.serialize(joinPoint.getArgs()) + "\"";
        assertEquals(expectedJson, json);
        System.out.println(json);
    }


}

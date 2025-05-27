package io.arex.foundation.serializer;

import io.arex.foundation.serializer.FastUtilAdapterFactoryTest.TestType;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.foundation.serializer.gson.GsonSerializer;
import io.arex.foundation.serializer.jackson.JacksonSerializer;
import io.arex.inst.runtime.util.TypeUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.lang.Nullable;

class JacksonSerializerTest {
    @Test
    void testLocalDateTime() throws Throwable {
        LocalDateTime now = LocalDateTime.now();
        String json = JacksonSerializer.INSTANCE.serialize(now);
        LocalDateTime actualResult = JacksonSerializer.INSTANCE.deserialize(json, LocalDateTime.class);
        assertEquals(now, actualResult);
    }

    @Test
    void testLocalTime() throws Throwable {
        LocalDateTime now = LocalDateTime.now();
        String json = JacksonSerializer.INSTANCE.serialize(now);
        LocalDateTime actualResult = JacksonSerializer.INSTANCE.deserialize(json, LocalDateTime.class);
        assertEquals(now, actualResult);
    }

    @Test
    public void testDecimal() throws Throwable {
        String expectedJson = "{\"percentage\": 0E-8}";
        Map<String,Object> map = JacksonSerializer.INSTANCE.deserialize(expectedJson, Map.class);
        assert map != null;
    }

    @Test
    void testNullList() throws Throwable {
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
    public void testSqlDate() throws Throwable {
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
    public void testSqlTime() throws Throwable {
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
    public void testTimeSerializeAndDeserialize() throws Throwable {
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
    void serialize() throws Throwable {
        // null object
        assertNull(JacksonSerializer.INSTANCE.serialize(null));

        // error serialize object
        assertThrows(Throwable.class, () -> JacksonSerializer.INSTANCE.serialize(JacksonSerializer.class.getDeclaredMethods()));
    }

    @Test
    void deserializeClass() throws Throwable {
        // null object
        assertNull(JacksonSerializer.INSTANCE.deserialize(null, String.class));

        // null class
        assertNull(JacksonSerializer.INSTANCE.deserialize("test", (Class)null));

        // error deserialize object
        String json  = JacksonSerializer.INSTANCE.serialize(LocalDateTime.now());
        assertNotNull(JacksonSerializer.INSTANCE.deserialize(json, LocalDateTime.class));
    }

    @Test
    void deserializeType() throws Throwable {
        // null object
        assertNull(JacksonSerializer.INSTANCE.deserialize(null, TypeUtil.forName(TypeUtil.getName(LocalDateTime.now()))));

        // null type
        assertNull(JacksonSerializer.INSTANCE.deserialize("test", TypeUtil.forName(null)));

        // error deserialize object
        String json  = JacksonSerializer.INSTANCE.serialize(LocalDateTime.now());
        assertNotNull(JacksonSerializer.INSTANCE.deserialize(json, TypeUtil.forName(TypeUtil.getName(LocalDateTime.now()))));
    }

    @Test
    void testFastUtil() throws Throwable {
        final TestType testType = FastUtilAdapterFactoryTest.getTestType();
        testType.setObject2DoubleMap(null);
        testType.setDoubleRangeObject2DoubleMap(null);
        final String jackJson = JacksonSerializer.INSTANCE.serialize(testType);
        final TestType deserializeJackTestType = JacksonSerializer.INSTANCE.deserialize(jackJson, TestType.class);
        assertNotNull(deserializeJackTestType);
    }

    @Test
    void testCaseSensitiveProperties() throws Throwable {
        final CaseSensitive caseSensitive = new CaseSensitive();
        caseSensitive.setAmountPaid("100");
        caseSensitive.setAmountpaid("200");
        caseSensitive.setAmount(100.0f);
        final String jackJson = JacksonSerializer.INSTANCE.serialize(caseSensitive);
        final CaseSensitive deserializeJackTestType = JacksonSerializer.INSTANCE.deserialize(jackJson, CaseSensitive.class);
        assertNotNull(deserializeJackTestType);
        assertEquals("100", deserializeJackTestType.getAmountPaid());
        assertEquals("200", deserializeJackTestType.getAmountpaid());
    }

    @Test
    void testNoDefaultCreator() throws Throwable {
        final NoDefaultCreator noDefaultCreator = new NoDefaultCreator("test", 100, 100.0);
        final String jackJson = JacksonSerializer.INSTANCE.serialize(noDefaultCreator);
        final NoDefaultCreator deserializeJackTestType = JacksonSerializer.INSTANCE.deserialize(jackJson, NoDefaultCreator.class);
        assertNotNull(deserializeJackTestType);
        assertEquals("test", deserializeJackTestType.getName());
        assertEquals(100, deserializeJackTestType.getAge());
        assertEquals(100.0, deserializeJackTestType.getAmount());
    }

    static class NoDefaultCreator {
        private final String name;
        private final int age;

        private final double amount;

        public NoDefaultCreator(String name, int age, double amount) {
            this.name = name;
            this.age = age;
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public double getAmount() {
            return amount;
        }
    }

    public static class CaseSensitive {
        private String amountPaid;
        private String amountpaid;
        private Float amount;

        public Float getAmount() {
            return amount;
        }

        public void setAmount(Float amount) {
            this.amount = amount;
        }

        public String getAmountPaid() {
            return amountPaid;
        }

        public void setAmountPaid(String amountPaid) {
            this.amountPaid = amountPaid;
        }

        public String getAmountpaid() {
            return amountpaid;
        }

        public void setAmountpaid(String amountpaid) {
            this.amountpaid = amountpaid;
        }
    }

}

package io.arex.foundation.serializer;

import io.arex.foundation.serializer.custom.FastUtilAdapterFactoryTest;
import io.arex.foundation.serializer.custom.FastUtilAdapterFactoryTest.TestType;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.runtime.util.TypeUtil;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

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
        final String jackJson = JacksonSerializer.INSTANCE.serialize(caseSensitive);
        final CaseSensitive deserializeJackTestType = JacksonSerializer.INSTANCE.deserialize(jackJson, CaseSensitive.class);
        assertNotNull(deserializeJackTestType);
        assertEquals("100", deserializeJackTestType.getAmountPaid());
        assertEquals("200", deserializeJackTestType.getAmountpaid());
    }

    @Test
    void testBigDecimal() throws Throwable {
        BigDecimal bigDecimal = new BigDecimal("0.916520000");
        String json3 = JacksonSerializer.INSTANCE.serialize(bigDecimal);
        BigDecimal actualResult3 = JacksonSerializer.INSTANCE.deserialize(json3, TypeUtil.forName(TypeUtil.getName(bigDecimal)));
        String json4 = GsonSerializer.INSTANCE.serialize(bigDecimal);
        BigDecimal actualResult4 = GsonSerializer.INSTANCE.deserialize(json4, TypeUtil.forName(TypeUtil.getName(bigDecimal)));

        CaseSensitive caseSensitive = new CaseSensitive();
        caseSensitive.setBigDecimal(bigDecimal);
        String json = JacksonSerializer.INSTANCE.serialize(caseSensitive);
        CaseSensitive actualResult = JacksonSerializer.INSTANCE.deserialize(json, CaseSensitive.class);
        String json2 = GsonSerializer.INSTANCE.serialize(caseSensitive);
        CaseSensitive actualResult2 = GsonSerializer.INSTANCE.deserialize(json2, CaseSensitive.class);
    }

    static class CaseSensitive {
        private String amountPaid;
        private String amountpaid;
        private BigDecimal bigDecimal;

        public BigDecimal getBigDecimal() {
            return bigDecimal;
        }

        public void setBigDecimal(BigDecimal bigDecimal) {
            this.bigDecimal = bigDecimal;
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

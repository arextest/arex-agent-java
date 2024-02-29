package io.arex.foundation.serializer.jackson;

import io.arex.foundation.serializer.TimeTestInfo;
import io.arex.inst.runtime.util.TypeUtil;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class JacksonSerializerWithTypeTest {

    @Test
    void name() {
        assertEquals("jackson-with-type", JacksonSerializerWithType.INSTANCE.name());
    }

    @Test
    void reCreateSerializer() {
        assertEquals(JacksonSerializerWithType.INSTANCE, JacksonSerializerWithType.INSTANCE.reCreateSerializer());
    }

    @Test
    void testMapSerializer() throws Throwable {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key1", "value1");
        map.put("bigDecimal", new BigDecimal(10));
        String json = JacksonSerializerWithType.INSTANCE.serialize(map);
        System.out.println(json);
        assertEquals(json, "[\"java.util.HashMap\",{\"key1\":\"value1\",\"bigDecimal\":[\"java.math.BigDecimal\",10]}]");

        Map<String, Object> actualResult = (Map<String, Object>) JacksonSerializerWithType.INSTANCE.deserialize(json, Object.class);

        assertEquals(actualResult.get("key1"), map.get("key1"));
        assertEquals(actualResult.get("bigDecimal"), map.get("bigDecimal"));
    }

    @Test
    void testTimeSerializerWithType() throws Throwable {
        Map<String, Object> map = new HashMap<String, Object>();
        TimeTestInfo timeTestInfo = new TimeTestInfo();
        map.put("localDateTime", timeTestInfo.getLocalDateTime());
        map.put("localDate", timeTestInfo.getLocalDate());
        map.put("localTime", timeTestInfo.getLocalTime());
        map.put("timestamp", timeTestInfo.getTimestamp());
        map.put("date", timeTestInfo.getDate());
        map.put("calendar", timeTestInfo.getCalendar());
        map.put("gregorianCalendar", timeTestInfo.getGregorianCalendar());
        map.put("xmlGregorianCalendar", timeTestInfo.getXmlGregorianCalendar());
        map.put("instant", timeTestInfo.getInstant());
        map.put("jodaLocalDate", timeTestInfo.getJodaLocalDate());
        map.put("jodaLocalTime", timeTestInfo.getJodaLocalTime());
        map.put("jodaLocalDateTime", timeTestInfo.getJodaLocalDateTime());
        map.put("dateTime", timeTestInfo.getDateTime());
        map.put("offsetDateTime", timeTestInfo.getOffsetDateTime());
        String json = JacksonSerializerWithType.INSTANCE.serialize(map);
        Map<String, Object> actualResult = (Map<String, Object>) JacksonSerializerWithType.INSTANCE.deserialize(json, Object.class);
        assertEquals(actualResult.get("localDateTime"), map.get("localDateTime"));
        assertEquals(actualResult.get("localDate"), map.get("localDate"));
        assertEquals(actualResult.get("localTime"), map.get("localTime"));
        assertEquals(actualResult.get("timestamp"), map.get("timestamp"));
        assertEquals(actualResult.get("date"), map.get("date"));
        assertEquals(actualResult.get("calendar"), map.get("calendar"));
        assertEquals(actualResult.get("gregorianCalendar"), map.get("gregorianCalendar"));
        assertEquals(actualResult.get("xmlGregorianCalendar"), map.get("xmlGregorianCalendar"));
        assertEquals(actualResult.get("instant"), map.get("instant"));
        assertEquals(actualResult.get("jodaLocalDate"), map.get("jodaLocalDate"));
        assertEquals(actualResult.get("jodaLocalTime"), map.get("jodaLocalTime"));
        assertEquals(actualResult.get("jodaLocalDateTime"), map.get("jodaLocalDateTime"));
        assertEquals(actualResult.get("dateTime"), map.get("dateTime"));

        Map<String, Object> actualResult2 = (Map<String, Object>) JacksonSerializerWithType.INSTANCE.deserialize(json, TypeUtil.forName(TypeUtil.getName(Object.class)));
        assertEquals(actualResult, actualResult2);
    }
}

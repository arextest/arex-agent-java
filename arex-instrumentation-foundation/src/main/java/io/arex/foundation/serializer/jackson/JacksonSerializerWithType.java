package io.arex.foundation.serializer.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.auto.service.AutoService;
import com.google.common.collect.Range;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.serializer.jackson.adapter.CalendarAdapter;
import io.arex.foundation.serializer.jackson.adapter.DateAdapter;
import io.arex.foundation.serializer.jackson.adapter.DateTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.GregorianCalendarAdapter;
import io.arex.foundation.serializer.jackson.adapter.GuavaRangeAdapter;
import io.arex.foundation.serializer.jackson.adapter.InstantAdapter;
import io.arex.foundation.serializer.jackson.adapter.JodaLocalDateAdapter;
import io.arex.foundation.serializer.jackson.adapter.JodaLocalDateTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.JodaLocalTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.LocalDateAdapter;
import io.arex.foundation.serializer.jackson.adapter.LocalDateTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.LocalTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.OffsetDateTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.SqlDateAdapter;
import io.arex.foundation.serializer.jackson.adapter.SqlTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.TimestampAdapter;
import io.arex.foundation.serializer.jackson.adapter.XMLGregorianCalendarAdapter;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.serializer.StringSerializable;
import org.joda.time.DateTime;

import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Type;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@AutoService(StringSerializable.class)
public class JacksonSerializerWithType implements StringSerializable {
    private final ObjectMapper mapper = new ArexObjectMapper();
    private static final SimpleModule MODULE = new JacksonSerializer.JacksonSimpleModule();
    public static final JacksonSerializerWithType INSTANCE = new JacksonSerializerWithType();

    @Override
    public String name() {
        return "jackson-with-type";
    }

    public JacksonSerializerWithType() {
        configMapper();
        customTimeFormatSerializer(MODULE);
        customTimeFormatDeserializer(MODULE);
        mapper.registerModule(MODULE);
    }

    private void configMapper() {
        mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        mapper.configure(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature(), true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        // serializer with type info
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
    }

    private void customTimeFormatSerializer(SimpleModule module) {
        DateAdapter.Serializer dateSerializer = new DateAdapter.Serializer();
        CalendarAdapter.Serializer calendarSerializer = new CalendarAdapter.Serializer();
        module.addSerializer(DateTime.class, new DateTimeAdapter.Serializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeAdapter.Serializer());
        module.addSerializer(LocalDate.class, new LocalDateAdapter.Serializer());
        module.addSerializer(LocalTime.class, new LocalTimeAdapter.Serializer());
        module.addSerializer(org.joda.time.LocalDateTime.class, new JodaLocalDateTimeAdapter.Serializer());
        module.addSerializer(org.joda.time.LocalDate.class, new JodaLocalDateAdapter.Serializer());
        module.addSerializer(org.joda.time.LocalTime.class, new JodaLocalTimeAdapter.Serializer());
        module.addSerializer(Calendar.class, calendarSerializer);
        module.addSerializer(GregorianCalendar.class, calendarSerializer);
        module.addSerializer(Timestamp.class, dateSerializer);
        module.addSerializer(XMLGregorianCalendar.class, new XMLGregorianCalendarAdapter.Serializer());
        // java.sql.Date/Time serialize same as java.util.Date
        module.addSerializer(Date.class, dateSerializer);
        module.addSerializer(Instant.class, new InstantAdapter.Serializer());
        module.addSerializer(OffsetDateTime.class, new OffsetDateTimeAdapter.Serializer());
        module.addSerializer(Range.class, new GuavaRangeAdapter.Serializer());
    }

    private void customTimeFormatDeserializer(SimpleModule module) {
        module.addDeserializer(DateTime.class, new DateTimeAdapter.Deserializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeAdapter.Deserializer());
        module.addDeserializer(LocalDate.class, new LocalDateAdapter.Deserializer());
        module.addDeserializer(LocalTime.class, new LocalTimeAdapter.Deserializer());
        module.addDeserializer(org.joda.time.LocalDateTime.class, new JodaLocalDateTimeAdapter.Deserializer());
        module.addDeserializer(org.joda.time.LocalDate.class, new JodaLocalDateAdapter.Deserializer());
        module.addDeserializer(org.joda.time.LocalTime.class, new JodaLocalTimeAdapter.Deserializer());
        module.addDeserializer(Calendar.class, new CalendarAdapter.Deserializer());
        module.addDeserializer(GregorianCalendar.class, new GregorianCalendarAdapter.Deserializer());
        module.addDeserializer(Timestamp.class, new TimestampAdapter.Deserializer());
        module.addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarAdapter.Deserializer());
        module.addDeserializer(Date.class, new DateAdapter.Deserializer());
        module.addDeserializer(Instant.class, new InstantAdapter.Deserializer());
        module.addDeserializer(OffsetDateTime.class, new OffsetDateTimeAdapter.Deserializer());
        module.addDeserializer(Range.class, new GuavaRangeAdapter.Deserializer());
        module.addDeserializer(java.sql.Date.class, new SqlDateAdapter.Deserializer());
        module.addDeserializer(Time.class, new SqlTimeAdapter.Deserializer());
    }

    @Override
    public String serialize(Object object) throws Throwable {
        if (object == null) {
            return null;
        }

        return mapper.writeValueAsString(object);
    }

    @Override
    public <T> T deserialize(String json, Class<T> clazz) throws Throwable {
        if (StringUtil.isEmpty(json) || clazz == null) {
            return null;
        }

        return mapper.readValue(json, clazz);
    }

    @Override
    public <T> T deserialize(String json, Type type) throws Throwable {
        if (StringUtil.isEmpty(json) || type == null) {
            return null;
        }

        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return deserialize(json, javaType);
    }

    public <T> T deserialize(String json, JavaType javaType) {
        try {
            return mapper.readValue(json, javaType);
        } catch (Throwable ex) {
            LogManager.warn("jackson-deserialize-type", ex);
        }
        return null;
    }

    @Override
    public StringSerializable reCreateSerializer() {
        return INSTANCE;
    }
}

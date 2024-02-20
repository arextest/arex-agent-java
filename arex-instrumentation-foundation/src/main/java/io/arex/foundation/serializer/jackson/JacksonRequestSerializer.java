package io.arex.foundation.serializer.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.auto.service.AutoService;
import io.arex.foundation.serializer.jackson.adapter.StringAdapter;
import io.arex.foundation.serializer.jackson.adapter.CalendarAdapter;
import io.arex.foundation.serializer.jackson.adapter.DateAdapter;
import io.arex.foundation.serializer.jackson.adapter.DateTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.InstantAdapter;
import io.arex.foundation.serializer.jackson.adapter.JodaLocalDateAdapter;
import io.arex.foundation.serializer.jackson.adapter.JodaLocalDateTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.JodaLocalTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.LocalDateAdapter;
import io.arex.foundation.serializer.jackson.adapter.LocalDateTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.LocalTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.OffsetDateTimeAdapter;
import io.arex.foundation.serializer.jackson.adapter.XMLGregorianCalendarAdapter;
import io.arex.inst.runtime.serializer.StringSerializable;
import org.joda.time.DateTime;

import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Type;
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
public class JacksonRequestSerializer implements StringSerializable {
    private final ObjectMapper mapper = new ObjectMapper();

    public JacksonRequestSerializer() {
        configMapper();
        SimpleModule module = new JacksonSerializer.JacksonSimpleModule();
        customTimeFormatSerializer(module);
        mapper.registerModule(module);
    }

    private void customTimeFormatSerializer(SimpleModule module) {
        CalendarAdapter.RequestSerializer calendarRequestSerializer = new CalendarAdapter.RequestSerializer();
        DateAdapter.RequestSerializer dateRequestSerializer = new DateAdapter.RequestSerializer();
        module.addSerializer(DateTime.class, new DateTimeAdapter.RequestSerializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeAdapter.RequestSerializer());
        module.addSerializer(LocalDate.class, new LocalDateAdapter.Serializer());
        module.addSerializer(LocalTime.class, new LocalTimeAdapter.RequestSerializer());
        module.addSerializer(org.joda.time.LocalDateTime.class, new JodaLocalDateTimeAdapter.RequestSerializer());
        module.addSerializer(org.joda.time.LocalDate.class, new JodaLocalDateAdapter.Serializer());
        module.addSerializer(org.joda.time.LocalTime.class, new JodaLocalTimeAdapter.RequestSerializer());
        module.addSerializer(Calendar.class, calendarRequestSerializer);
        module.addSerializer(GregorianCalendar.class, calendarRequestSerializer);
        module.addSerializer(Timestamp.class, dateRequestSerializer);
        module.addSerializer(XMLGregorianCalendar.class, new XMLGregorianCalendarAdapter.RequestSerializer());
        // java.sql.Date/Time serialize same as java.util.Date
        module.addSerializer(Date.class, dateRequestSerializer);
        module.addSerializer(Instant.class, new InstantAdapter.RequestSerializer());
        module.addSerializer(OffsetDateTime.class, new OffsetDateTimeAdapter.RequestSerializer());
        module.addSerializer(String.class, new StringAdapter.Serializer());
    }

    private void configMapper() {
        mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }

    @Override
    public String name() {
        return "jackson-request";
    }

    @Override
    public String serialize(Object object) throws Throwable {
        return mapper.writeValueAsString(object);
    }

    @Override
    public <T> T deserialize(String value, Class<T> clazz) throws Throwable {
        // request not need deserialize
        return null;
    }

    @Override
    public <T> T deserialize(String value, Type type) throws Throwable {
        // request not need deserialize
        return null;
    }

    @Override
    public StringSerializable reCreateSerializer() {
        return new JacksonRequestSerializer();
    }
}

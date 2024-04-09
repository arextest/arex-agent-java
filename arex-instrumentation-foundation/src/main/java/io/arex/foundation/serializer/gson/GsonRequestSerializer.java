package io.arex.foundation.serializer.gson;

import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.arex.foundation.serializer.gson.adapter.CustomTypeAdapterFactory;
import io.arex.foundation.serializer.gson.adapter.DateAdapter;
import io.arex.foundation.serializer.gson.adapter.GsonExclusion;
import io.arex.foundation.serializer.gson.adapter.StringAdapter;
import io.arex.foundation.serializer.gson.adapter.CalendarAdapter;
import io.arex.foundation.serializer.gson.adapter.ClassAdapter;
import io.arex.foundation.serializer.gson.adapter.InstantAdapter;
import io.arex.foundation.serializer.gson.adapter.LocalDateAdapter;
import io.arex.foundation.serializer.gson.adapter.LocalDateTimeAdapter;
import io.arex.foundation.serializer.gson.adapter.LocalTimeAdapter;
import io.arex.foundation.serializer.gson.adapter.OffsetDateTimeAdapter;
import io.arex.foundation.serializer.gson.adapter.TimeZoneAdapter;
import io.arex.foundation.serializer.gson.adapter.XMLGregorianCalendarAdapter;
import io.arex.inst.runtime.serializer.StringSerializable;

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
import java.util.TimeZone;

@AutoService(StringSerializable.class)
public class GsonRequestSerializer implements StringSerializable {
    public static final GsonRequestSerializer INSTANCE = new GsonRequestSerializer();
    private final Gson serializer;

    public GsonRequestSerializer() {
        DateAdapter.RequestSerializer dateRequestSerializer = new DateAdapter.RequestSerializer();
        CalendarAdapter.RequestSerializer calendarRequestSerializer = new CalendarAdapter.RequestSerializer();
        this.serializer = new GsonBuilder()
                .registerTypeAdapter(XMLGregorianCalendar.class, new XMLGregorianCalendarAdapter.RequestSerializer())
                .registerTypeAdapter(Time.class, dateRequestSerializer)
                .registerTypeAdapter(Timestamp.class, dateRequestSerializer)
                .registerTypeAdapter(Date.class, dateRequestSerializer)
                .registerTypeAdapter(java.sql.Date.class, dateRequestSerializer)
                .registerTypeAdapter(Calendar.class, calendarRequestSerializer)
                .registerTypeAdapter(GregorianCalendar.class, calendarRequestSerializer)
                .registerTypeAdapter(Instant.class, new InstantAdapter.RequestSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter.RequestSerializer())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter.Serializer())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter.RequestSerializer())
                .registerTypeAdapter(Class.class, new ClassAdapter.Serializer())
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter.RequestSerializer())
                .registerTypeHierarchyAdapter(TimeZone.class, new TimeZoneAdapter.Serializer())
                .registerTypeAdapter(String.class, new StringAdapter.Serializer())
                .registerTypeAdapterFactory(new CustomTypeAdapterFactory.RequestSerializerFactory())
                .enableComplexMapKeySerialization()
                .setExclusionStrategies(new GsonExclusion())
                .disableHtmlEscaping()
                .create();
    }

    @Override
    public String name() {
        return "gson-request";
    }

    @Override
    public String serialize(Object object) throws Throwable {
        if (object == null) {
            return null;
        }
        return serializer.toJson(object);
    }

    @Override
    public <T> T deserialize(String value, Class<T> clazz) throws Throwable {
        // request serializer not need deserialize
        return null;
    }

    @Override
    public <T> T deserialize(String value, Type type) throws Throwable {
        // request serializer not need deserialize
        return null;
    }

    @Override
    public StringSerializable reCreateSerializer() {
        return new GsonRequestSerializer();
    }
}

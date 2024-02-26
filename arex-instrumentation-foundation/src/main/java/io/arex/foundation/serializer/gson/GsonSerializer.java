package io.arex.foundation.serializer.gson;

import com.google.auto.service.AutoService;

import com.google.common.collect.Range;
import io.arex.foundation.serializer.gson.adapter.FastUtilAdapterFactory;
import io.arex.agent.bootstrap.util.StringUtil;
import com.google.gson.Gson;

import io.arex.foundation.serializer.gson.adapter.NumberStrategy;
import io.arex.foundation.serializer.gson.adapter.ProtobufAdapterFactory;
import io.arex.foundation.serializer.gson.adapter.*;
import io.arex.inst.runtime.serializer.StringSerializable;

import java.sql.Time;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.joda.time.DateTime;
import com.google.gson.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@AutoService(StringSerializable.class)
public class GsonSerializer implements StringSerializable {

    public static final GsonSerializer INSTANCE = new GsonSerializer();
    private Gson serializer;
    private GsonBuilder gsonBuilder;

    @Override
    public void addTypeSerializer(Class<?> clazz, Object typeSerializer) {
        if (typeSerializer == null) {
            // map<String, Object> custom serializer
            this.gsonBuilder.registerTypeAdapter(clazz, new MapAdapter.Serializer());
            this.gsonBuilder.registerTypeAdapter(clazz, new MapAdapter.Deserializer());
        } else {
            this.gsonBuilder.registerTypeAdapter(clazz, typeSerializer);
        }
        this.serializer = gsonBuilder.create();
    }

    public GsonSerializer() {
        DateAdapter.Serializer dateSerializer = new DateAdapter.Serializer();
        CalendarAdapter.Serializer calendarSerializer = new CalendarAdapter.Serializer();
        gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateTimeAdapter.Serializer())
                .registerTypeAdapter(DateTime.class, new DateTimeAdapter.Deserializer())
                .registerTypeAdapter(org.joda.time.LocalDateTime.class, new JodaLocalDateTimeAdapter.Serializer())
                .registerTypeAdapter(org.joda.time.LocalDateTime.class, new JodaLocalDateTimeAdapter.Deserializer())
                .registerTypeAdapter(org.joda.time.LocalDate.class, new JodaLocalDateAdapter.Serializer())
                .registerTypeAdapter(org.joda.time.LocalDate.class, new JodaLocalDateAdapter.Deserializer())
                .registerTypeAdapter(org.joda.time.LocalTime.class, new JodaLocalTimeAdapter.Serializer())
                .registerTypeAdapter(org.joda.time.LocalTime.class, new JodaLocalTimeAdapter.Deserializer())
                .registerTypeAdapter(XMLGregorianCalendar.class, new XMLGregorianCalendarAdapter.Serializer())
                .registerTypeAdapter(XMLGregorianCalendar.class, new XMLGregorianCalendarAdapter.Deserializer())
                .registerTypeAdapter(Time.class, dateSerializer)
                .registerTypeAdapter(Time.class, new SqlTimeAdapter.Deserializer())
                .registerTypeAdapter(Timestamp.class, dateSerializer)
                .registerTypeAdapter(Timestamp.class, new TimestampAdapter.Deserializer())
                .registerTypeAdapter(Date.class, dateSerializer)
                .registerTypeAdapter(Date.class, new DateAdapter.Deserializer())
                .registerTypeAdapter(java.sql.Date.class, dateSerializer)
                .registerTypeAdapter(java.sql.Date.class, new SqlDateAdapter.Deserializer())
                .registerTypeAdapter(Calendar.class, calendarSerializer)
                .registerTypeAdapter(Calendar.class, new CalendarAdapter.Deserializer())
                .registerTypeAdapter(GregorianCalendar.class, calendarSerializer)
                .registerTypeAdapter(GregorianCalendar.class, new GregorianCalendarAdapter.Deserializer())
                .registerTypeAdapter(Instant.class, new InstantAdapter.Serializer())
                .registerTypeAdapter(Instant.class, new InstantAdapter.Deserializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter.Serializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter.Deserializer())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter.Serializer())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter.Deserializer())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter.Serializer())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter.Deserializer())
                .registerTypeAdapter(Class.class, new ClassAdapter.Serializer())
                .registerTypeAdapter(Class.class, new ClassAdapter.Deserializer())
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter.Serializer())
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter.Deserializer())
                .registerTypeAdapter(Range.class, new GuavaRangeAdapter.Serializer())
                .registerTypeAdapter(Range.class, new GuavaRangeAdapter.Deserializer())
                .registerTypeHierarchyAdapter(TimeZone.class, new TimeZoneAdapter.Serializer())
                .registerTypeAdapter(TimeZone.class, new TimeZoneAdapter.Deserializer())
                .registerTypeAdapterFactory(new FastUtilAdapterFactory())
                .registerTypeAdapterFactory(new ProtobufAdapterFactory())
                .enableComplexMapKeySerialization()
                .setExclusionStrategies(new GsonExclusion())
                .disableHtmlEscaping()
                .setObjectToNumberStrategy(new NumberStrategy());
        serializer = gsonBuilder.create();
    }

    @Override
    public String name() {
        return "gson";
    }

    @Override
    public String serialize(Object object) {
        if (object == null) {
            return null;
        }
        return serializer.toJson(object);
    }

    @Override
    public <T> T deserialize(String json, Class<T> clazz) {
        if (StringUtil.isEmpty(json) || clazz == null) {
            return null;
        }
        return serializer.fromJson(json, clazz);
    }

    @Override
    public <T> T deserialize(String json, Type type) {
        if (StringUtil.isEmpty(json) || type == null) {
            return null;
        }

        return serializer.fromJson(json, type);
    }

    @Override
    public StringSerializable reCreateSerializer() {
        return new GsonSerializer();
    }

}

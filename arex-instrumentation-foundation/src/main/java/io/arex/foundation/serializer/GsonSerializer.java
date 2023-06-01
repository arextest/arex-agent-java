package io.arex.foundation.serializer;

import com.google.auto.service.AutoService;

import io.arex.foundation.serializer.JacksonSerializer.DateFormatParser;
import io.arex.foundation.util.NumberTypeAdaptor;
import io.arex.agent.bootstrap.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;

import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.util.TypeUtil;
import java.sql.Time;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.*;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@AutoService(StringSerializable.class)
public class GsonSerializer implements StringSerializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GsonSerializer.class);

    private static final JsonSerializer<LocalDateTime> LOCAL_DATE_TIME_JSON_SERIALIZER =
        ((src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern(JacksonSerializer.DatePatternConstants.localDateTimeFormat))));
    private static final JsonDeserializer<LocalDateTime> LOCAL_DATE_TIME_JSON_DESERIALIZER = (json, type, context) ->
            LocalDateTime.parse(json.getAsString(), JacksonSerializer.DateFormatParser.INSTANCE.getFormatter(JacksonSerializer.DatePatternConstants.localDateTimeFormat));
    private static final JsonSerializer<LocalDate> LOCAL_DATE_JSON_SERIALIZER =
        ((src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern(JacksonSerializer.DatePatternConstants.SHORT_DATE_FORMAT))));

    private static final JsonDeserializer<LocalDate> LOCAL_DATE_JSON_DESERIALIZER = (json, type, context) ->
            LocalDate.parse(json.getAsString(), JacksonSerializer.DateFormatParser.INSTANCE.getFormatter(JacksonSerializer.DatePatternConstants.SHORT_DATE_FORMAT));

    private static final JsonSerializer<LocalTime> LOCAL_TIME_JSON_SERIALIZER =
        ((src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern(JacksonSerializer.DatePatternConstants.localTimeFormat))));

    private static final JsonDeserializer<LocalTime> LOCAL_TIME_JSON_DESERIALIZER = (json, type, context) ->
            LocalTime.parse(json.getAsString(), JacksonSerializer.DateFormatParser.INSTANCE.getFormatter(JacksonSerializer.DatePatternConstants.localTimeFormat));

    private static final JsonSerializer<Calendar> CALENDAR_JSON_SERIALIZER =
        (((src, typeOfSrc, context) -> new JsonPrimitive(
            DateFormatUtils.format(src, JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE, src.getTimeZone()))));

    private static final JsonDeserializer<Calendar> CALENDAR_JSON_DESERIALIZER = (json, type, context) ->
            JacksonSerializer.DateFormatParser.INSTANCE.parseCalendar(json.getAsString());

    private static final JsonSerializer<GregorianCalendar> GREGORIAN_CALENDAR_JSON_SERIALIZER =
        (((src, typeOfSrc, context) -> new JsonPrimitive(DateFormatUtils.format(src, JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE, src.getTimeZone()))));

    private static final JsonDeserializer<GregorianCalendar> GREGORIAN_CALENDAR_JSON_DESERIALIZER = (json, type, context) ->
            JacksonSerializer.DateFormatParser.INSTANCE.parseGregorianCalendar(json.getAsString());

    private static final JsonSerializer<XMLGregorianCalendar> XML_GREGORIAN_CALENDAR_JSON_SERIALIZER =
        (((src, typeOfSrc, context) -> {
            GregorianCalendar calendar = src.toGregorianCalendar();
            return new JsonPrimitive(DateFormatUtils.format(calendar, JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE, calendar.getTimeZone()));
        }));

    private static final JsonDeserializer<XMLGregorianCalendar> XML_GREGORIAN_CALENDAR_JSON_DESERIALIZER =
            (json, type, context) -> {
                GregorianCalendar calendar = JacksonSerializer.DateFormatParser.INSTANCE.parseGregorianCalendar(json.getAsString());
                XMLGregorianCalendar xmlGregorianCalendar = null;
                try {
                    xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
                } catch (Exception ex) {
                    LOGGER.warn("XMLGregorianCalendar.deserialize", ex);
                }
                return xmlGregorianCalendar;
            };

    private static final JsonSerializer<Timestamp> TIMESTAMP_JSON_SERIALIZER =
        (((src, typeOfSrc, context) -> new JsonPrimitive(DateFormatUtils.format(src, JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS))));

    private static final JsonDeserializer<Timestamp> TIMESTAMP_JSON_DESERIALIZER = (json, typeOfT, context) ->
            Optional.ofNullable(JacksonSerializer.DateFormatParser.INSTANCE.parseDate(json.getAsString()))
                    .map(date -> new Timestamp(date.getTime())).orElse(new Timestamp(System.currentTimeMillis()));

    private static final JsonSerializer<Date> DATE_JSON_SERIALIZER =
        (((src, typeOfSrc, context) -> new JsonPrimitive(DateFormatUtils.format(src, JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS))));
    private static final JsonDeserializer<Date> DATE_JSON_DESERIALIZER = (json, type, context) ->
            DateFormatParser.INSTANCE.parseDate(json.getAsString());

    private static final JsonSerializer<java.sql.Date> SQL_DATE_JSON_SERIALIZER =
        (((src, typeOfSrc, context) -> new JsonPrimitive(DateFormatUtils.format(src, JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS))));

    private static final JsonDeserializer<java.sql.Date> SQL_DATE_JSON_DESERIALIZER = (json, type, context) ->{
        Date date = DateFormatParser.INSTANCE.parseDate(json.getAsString());
        return new java.sql.Date(date.getTime());
    };

    private static final JsonSerializer<Time> TIME_JSON_SERIALIZER =
        (((src, typeOfSrc, context) -> new JsonPrimitive(DateFormatUtils.format(src, JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS))));

    private static final JsonDeserializer<Time> TIME_JSON_DESERIALIZER = (json, type, context) -> {
        Date date = DateFormatParser.INSTANCE.parseDate(json.getAsString());
        return new Time(date.getTime());
    };

    private static final JsonSerializer<Instant> INSTANT_JSON_SERIALIZER =
        ((src, typeOfSrc, context) -> new JsonPrimitive(src.toString()));

    private static final JsonDeserializer<Instant> INSTANT_JSON_DESERIALIZER = (json, typeOfT, context) ->
        Instant.parse(json.getAsString());

    private static final JsonSerializer<Class> CLASS_JSON_SERIALIZER =
        ((src, typeOfSrc, context) -> new JsonPrimitive(src.getName()));

    private static final JsonDeserializer<Class> CLASS_JSON_DESERIALIZER = (json, typeOfT, context) -> {
        return (Class) TypeUtil.forName(json.getAsString());
    };

    private static final ExclusionStrategy EXCLUSION_STRATEGY = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            String fieldName = f.getName();
            if ("stackTrace".equals(fieldName) || "suppressedExceptions".equals(fieldName)) {
                return true;
            }
            String className = f.getDeclaringClass().getName();
            List<String> fieldNameList = JacksonSerializer.INSTANCE.getSkipFieldNameList(className);

            if (fieldNameList == null) {
                return false;
            }

            if (fieldNameList.isEmpty()) {
                return true;
            }
            return fieldNameList.contains(fieldName);
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    };
    public static final GsonSerializer INSTANCE = new GsonSerializer();
    private final Gson serializer;
    public GsonSerializer() {
        serializer = new GsonBuilder().registerTypeAdapterFactory(NumberTypeAdaptor.FACTORY)
//              .registerTypeAdapter(org.joda.time.DateTime.class, DATE_TIME_JSON_SERIALIZER)
//              .registerTypeAdapter(org.joda.time.DateTime.class, DATE_TIME_JSON_DESERIALIZER)
//              .registerTypeAdapter(org.joda.time.LocalDateTime.class, JODA_LOCAL_DATE_TIME_JSON_SERIALIZER)
//              .registerTypeAdapter(org.joda.time.LocalDateTime.class, JODA_LOCAL_DATE_TIME_JSON_DESERIALIZER)
//              .registerTypeAdapter(org.joda.time.LocalDate.class, JODA_LOCAL_DATE_JSON_SERIALIZER)
//              .registerTypeAdapter(org.joda.time.LocalDate.class, JODA_LOCAL_DATE_JSON_DESERIALIZER)
//              .registerTypeAdapter(org.joda.time.LocalTime.class, JODA_LOCAL_TIME_JSON_SERIALIZER)
//              .registerTypeAdapter(org.joda.time.LocalTime.class, JODA_LOCAL_TIME_JSON_DESERIALIZER)
                .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_JSON_SERIALIZER)
                .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_JSON_DESERIALIZER)
                .registerTypeAdapter(LocalDate.class, LOCAL_DATE_JSON_SERIALIZER)
                .registerTypeAdapter(LocalDate.class, LOCAL_DATE_JSON_DESERIALIZER)
                .registerTypeAdapter(LocalTime.class, LOCAL_TIME_JSON_SERIALIZER)
                .registerTypeAdapter(LocalTime.class, LOCAL_TIME_JSON_DESERIALIZER)
                .registerTypeAdapter(Calendar.class, CALENDAR_JSON_SERIALIZER)
                .registerTypeAdapter(Calendar.class, CALENDAR_JSON_DESERIALIZER)
                .registerTypeAdapter(GregorianCalendar.class, GREGORIAN_CALENDAR_JSON_SERIALIZER)
                .registerTypeAdapter(GregorianCalendar.class, GREGORIAN_CALENDAR_JSON_DESERIALIZER)
                .registerTypeAdapter(XMLGregorianCalendar.class, XML_GREGORIAN_CALENDAR_JSON_SERIALIZER)
                .registerTypeAdapter(XMLGregorianCalendar.class, XML_GREGORIAN_CALENDAR_JSON_DESERIALIZER)
                .registerTypeAdapter(Timestamp.class, TIMESTAMP_JSON_SERIALIZER)
                .registerTypeAdapter(Timestamp.class, TIMESTAMP_JSON_DESERIALIZER)
                .registerTypeAdapter(Date.class, DATE_JSON_SERIALIZER)
                .registerTypeAdapter(Date.class, DATE_JSON_DESERIALIZER)
                .registerTypeAdapter(java.sql.Date.class, SQL_DATE_JSON_SERIALIZER)
                .registerTypeAdapter(java.sql.Date.class, SQL_DATE_JSON_DESERIALIZER)
                .registerTypeAdapter(Time.class, TIME_JSON_SERIALIZER)
                .registerTypeAdapter(Time.class, TIME_JSON_DESERIALIZER)
                .registerTypeAdapter(Instant.class, INSTANT_JSON_SERIALIZER)
                .registerTypeAdapter(Instant.class, INSTANT_JSON_DESERIALIZER)
                .registerTypeAdapter(Class.class, CLASS_JSON_SERIALIZER)
                .registerTypeAdapter(Class.class, CLASS_JSON_DESERIALIZER)
                .enableComplexMapKeySerialization()
                .setExclusionStrategies(EXCLUSION_STRATEGY)
                .disableHtmlEscaping().create();

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
        try {
            return serializer.toJson(object);
        } catch (Throwable ex) {
            LOGGER.warn("serialize", ex);
            return null;
        }
    }

    @Override
    public <T> T deserialize(String json, Class<T> clazz) {
        if (StringUtil.isEmpty(json) || clazz == null) {
            return null;
        }
        try {
            return serializer.fromJson(json, clazz);
        } catch (Throwable ex) {
            LOGGER.warn("gson-deserialize-clazz", ex);
            return null;
        }
    }

    @Override
    public <T> T deserialize(String json, Type type) {
        if (StringUtil.isEmpty(json) || type == null) {
            return null;
        }
        try {
            return serializer.fromJson(json, type);
        } catch (Throwable ex) {
            LOGGER.warn("gson-deserialize-type", ex);
            return null;
        }
    }

    @Override
    public StringSerializable reCreateSerializer() {
        return new GsonSerializer();
    }

}

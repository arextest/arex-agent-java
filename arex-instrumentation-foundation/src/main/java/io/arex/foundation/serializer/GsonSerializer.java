package io.arex.foundation.serializer;

import com.google.auto.service.AutoService;

import com.google.common.collect.Range;
import io.arex.agent.thirdparty.util.time.DateFormatUtils;
import io.arex.foundation.serializer.JacksonSerializer.DateFormatParser;
import io.arex.foundation.serializer.custom.FastUtilAdapterFactory;
import io.arex.foundation.serializer.custom.GuavaRangeSerializer;
import io.arex.agent.bootstrap.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;

import io.arex.foundation.serializer.custom.NumberStrategy;
import io.arex.foundation.serializer.custom.ProtobufAdapterFactory;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.util.TypeUtil;

import java.sql.Time;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map.Entry;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
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

    private static final JsonSerializer<DateTime> DATE_TIME_JSON_SERIALIZER =
            ((src, typeOfSrc, context) -> new JsonPrimitive(src.toString(JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE_DATETIME)));
    private static final JsonDeserializer<DateTime> DATE_TIME_JSON_DESERIALIZER =
            (json, type, context) -> {
                TimeZone timeZone = JacksonSerializer.TimezoneParser.INSTANCE.parse(json.getAsString());
                DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(timeZone);
                Date date = DateFormatParser.INSTANCE.parseDate(json.getAsString(), timeZone);
                if (date == null) {
                    return null;
                }
                return new DateTime(date.getTime()).withZone(dateTimeZone);
            };

    private static final JsonSerializer<org.joda.time.LocalDateTime> JODA_LOCAL_DATE_TIME_JSON_SERIALIZER =
            (src, typeOfSrc, context) -> new JsonPrimitive(src.toString(JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS));
    private static final JsonDeserializer<org.joda.time.LocalDateTime> JODA_LOCAL_DATE_TIME_JSON_DESERIALIZER = (json, type, context) ->
            org.joda.time.LocalDateTime.parse(json.getAsString(), DateTimeFormat.forPattern(JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS));

    private static final JsonSerializer<org.joda.time.LocalDate> JODA_LOCAL_DATE_JSON_SERIALIZER =
            (src, typeOfSrc, context) -> new JsonPrimitive(src.toString(JacksonSerializer.DatePatternConstants.SHORT_DATE_FORMAT));
    private static final JsonDeserializer<org.joda.time.LocalDate> JODA_LOCAL_DATE_JSON_DESERIALIZER = (json, type, context) ->
            org.joda.time.LocalDate.parse(json.getAsString(), DateTimeFormat.forPattern(JacksonSerializer.DatePatternConstants.SHORT_DATE_FORMAT));

    private static final JsonSerializer<org.joda.time.LocalTime> JODA_LOCAL_TIME_JSON_SERIALIZER =
            (src, typeOfSrc, context) -> new JsonPrimitive(src.toString(JacksonSerializer.DatePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));
    private static final JsonDeserializer<org.joda.time.LocalTime> JODA_LOCAL_TIME_JSON_DESERIALIZER = (json, type, context) ->
            org.joda.time.LocalTime.parse(json.getAsString(), DateTimeFormat.forPattern(JacksonSerializer.DatePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));

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

    private static final JsonSerializer<OffsetDateTime> OFFSET_DATE_TIME_JSON_SERIALIZER =
        ((src, typeOfSrc, context) -> new JsonPrimitive(DateFormatUtils.format(src, JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE_DATETIME)));

    private static final JsonDeserializer<OffsetDateTime> OFFSET_DATE_TIME_JSON_DESERIALIZER = (json, typeOfT, context) ->
            OffsetDateTime.parse(json.getAsString(), DateFormatParser.INSTANCE.getFormatter(JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE_DATETIME));

    private static final JsonSerializer<TimeZone> TIMEZONE_JSON_SERIALIZER = (timeZone, type, context) -> new JsonPrimitive(timeZone.getID());

    private static final JsonDeserializer<TimeZone> TIMEZONE_JSON_DESERIALIZER =
            (json, typeOfT, context) -> TimeZone.getTimeZone(json.getAsString());

    public static final GsonSerializer INSTANCE = new GsonSerializer();
    private Gson serializer;
    private GsonBuilder gsonBuilder;

    @Override
    public void addTypeSerializer(Class<?> clazz, Object typeSerializer) {
        if (typeSerializer == null) {
            // map<String, Object> custom serializer
            this.gsonBuilder.registerTypeAdapter(clazz, new MapSerializer());
        } else {
            this.gsonBuilder.registerTypeAdapter(clazz, typeSerializer);
        }
        this.serializer = gsonBuilder.create();
    }

    public GsonSerializer() {
        gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, DATE_TIME_JSON_SERIALIZER)
                .registerTypeAdapter(DateTime.class, DATE_TIME_JSON_DESERIALIZER)
                .registerTypeAdapter(org.joda.time.LocalDateTime.class, JODA_LOCAL_DATE_TIME_JSON_SERIALIZER)
                .registerTypeAdapter(org.joda.time.LocalDateTime.class, JODA_LOCAL_DATE_TIME_JSON_DESERIALIZER)
                .registerTypeAdapter(org.joda.time.LocalDate.class, JODA_LOCAL_DATE_JSON_SERIALIZER)
                .registerTypeAdapter(org.joda.time.LocalDate.class, JODA_LOCAL_DATE_JSON_DESERIALIZER)
                .registerTypeAdapter(org.joda.time.LocalTime.class, JODA_LOCAL_TIME_JSON_SERIALIZER)
                .registerTypeAdapter(org.joda.time.LocalTime.class, JODA_LOCAL_TIME_JSON_DESERIALIZER)
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
                .registerTypeAdapter(OffsetDateTime.class, OFFSET_DATE_TIME_JSON_SERIALIZER)
                .registerTypeAdapter(OffsetDateTime.class, OFFSET_DATE_TIME_JSON_DESERIALIZER)
                .registerTypeHierarchyAdapter(TimeZone.class, TIMEZONE_JSON_SERIALIZER)
                .registerTypeAdapter(TimeZone.class, TIMEZONE_JSON_DESERIALIZER)
                .registerTypeAdapter(Range.class, new GuavaRangeSerializer.GsonRangeSerializer())
                .registerTypeAdapterFactory(new FastUtilAdapterFactory())
                .registerTypeAdapterFactory(new ProtobufAdapterFactory())
                .enableComplexMapKeySerialization()
                .setExclusionStrategies(new ExcludeField())
                .disableHtmlEscaping()
                .setObjectToNumberStrategy(new NumberStrategy());
        serializer = gsonBuilder.create();
    }


    static class MapSerializer implements JsonSerializer<Map<String, Object>>, JsonDeserializer<Map<String, Object>> {

        private static final Character SEPARATOR = '-';

        @Override
        public JsonElement serialize(Map<String, Object> document, Type type, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<String, Object> entry : document.entrySet()) {
                final Object value = entry.getValue();

                if (value == null) {
                    jsonObject.add(entry.getKey(), null);
                    continue;
                }

                if (value instanceof String) {
                    jsonObject.addProperty(entry.getKey(), (String) value);
                    continue;
                }
                jsonObject.add(entry.getKey() + SEPARATOR + TypeUtil.getName(value),
                        context.serialize(value));
            }
            return jsonObject;
        }

        @Override
        public Map<String, Object> deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {

            final JsonObject jsonObject = json.getAsJsonObject();
            try {
                // only support no-arg constructor
                final Map<String, Object> map = (Map<String, Object>) ((Class) typeOfT).getDeclaredConstructor(null).newInstance();
                for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    final String[] split = StringUtil.splitByFirstSeparator(entry.getKey(), SEPARATOR);
                    if (split.length < 2) {
                        map.put(entry.getKey(), context.deserialize(entry.getValue(), String.class));
                        continue;
                    }
                    String valueClazz = split[1];
                    String key = split[0];
                    final JsonElement valueJson = entry.getValue();
                    final Type valueType = TypeUtil.forName(valueClazz);
                    final Object value = context.deserialize(valueJson, valueType);
                    map.put(key, value);
                }
                return map;
            } catch (Exception e) {
                LogManager.warn("MapSerializer.deserialize", e);
                return null;
            }

        }
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

    static class ExcludeField implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            String fieldName = f.getName();
            if ("stackTrace".equals(fieldName) || "suppressedExceptions".equals(fieldName)) {
                return true;
            }
            String className = f.getDeclaringClass().getName();
            if (MONGO_CLASS_LIST.contains(className) && !MONGO_FIELD_LIST.contains(fieldName)) {
                return true;
            }
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
    }


}

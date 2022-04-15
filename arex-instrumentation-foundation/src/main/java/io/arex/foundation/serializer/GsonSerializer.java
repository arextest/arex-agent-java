package io.arex.foundation.serializer;

import io.arex.foundation.util.NumberTypeAdaptor;
import io.arex.foundation.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
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

public class GsonSerializer implements SerializeUtils.StringSerializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GsonSerializer.class);
    public static final GsonSerializer INSTANCE = new GsonSerializer();

    private static final JsonDeserializer<LocalDateTime> LOCAL_DATE_TIME_JSON_DESERIALIZER = (json, type, context) ->
            LocalDateTime.parse(json.getAsString(), JacksonSerializer.DateFormatParser.INSTANCE.getFormatter(JacksonSerializer.DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS));

    private static final JsonDeserializer<LocalDate> LOCAL_DATE_JSON_DESERIALIZER = (json, type, context) ->
            LocalDate.parse(json.getAsString(), JacksonSerializer.DateFormatParser.INSTANCE.getFormatter(JacksonSerializer.DatePatternConstants.SHORT_DATE_FORMAT));

    private static final JsonDeserializer<LocalTime> LOCAL_TIME_JSON_DESERIALIZER = (json, type, context) ->
            LocalTime.parse(json.getAsString(), JacksonSerializer.DateFormatParser.INSTANCE.getFormatter(JacksonSerializer.DatePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));


    private static final JsonDeserializer<Calendar> CALENDAR_JSON_DESERIALIZER = (json, type, context) ->
            JacksonSerializer.DateFormatParser.INSTANCE.parseCalendar(json.getAsString());

    private static final JsonDeserializer<GregorianCalendar> GREGORIAN_CALENDAR_JSON_DESERIALIZER = (json, type, context) ->
            JacksonSerializer.DateFormatParser.INSTANCE.parseGregorianCalendar(json.getAsString());

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

    private static final JsonDeserializer<Timestamp> TIMESTAMP_JSON_DESERIALIZER = (json, typeOfT, context) ->
            Optional.ofNullable(JacksonSerializer.DateFormatParser.INSTANCE.parseDate(json.getAsString()))
                    .map(date -> new Timestamp(date.getTime())).orElse(new Timestamp(System.currentTimeMillis()));

    private static final JsonDeserializer<Date> DATE_JSON_DESERIALIZER = (json, type, context) ->
            new Date(Long.parseLong(json.getAsString()));

    private static final Gson SERIALIZER = new GsonBuilder().registerTypeAdapterFactory(NumberTypeAdaptor.FACTORY)
            .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_JSON_DESERIALIZER)
            .registerTypeAdapter(LocalDate.class, LOCAL_DATE_JSON_DESERIALIZER)
            .registerTypeAdapter(LocalTime.class, LOCAL_TIME_JSON_DESERIALIZER)
            .registerTypeAdapter(Calendar.class, CALENDAR_JSON_DESERIALIZER)
            .registerTypeAdapter(GregorianCalendar.class, GREGORIAN_CALENDAR_JSON_DESERIALIZER)
            .registerTypeAdapter(XMLGregorianCalendar.class, XML_GREGORIAN_CALENDAR_JSON_DESERIALIZER)
            .registerTypeAdapter(Timestamp.class, TIMESTAMP_JSON_DESERIALIZER)
            .registerTypeAdapter(Date.class, DATE_JSON_DESERIALIZER)
            .disableHtmlEscaping().create();

    @Override
    public String serialize(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return SERIALIZER.toJson(object);
        } catch (Exception ex) {
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
            return SERIALIZER.fromJson(json, clazz);
        } catch (Exception ex) {
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
            return SERIALIZER.fromJson(json, type);
        } catch (Exception ex) {
            LOGGER.warn("gson-deserialize-type", ex);
            return null;
        }
    }

}

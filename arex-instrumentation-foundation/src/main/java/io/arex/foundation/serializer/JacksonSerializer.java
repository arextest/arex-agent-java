package io.arex.foundation.serializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.arex.foundation.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JacksonSerializer implements SerializeUtils.StringSerializable {
    public static final String EXTENSION = "json";

    private static List<String> MYBATIS_PLUS_CLASS_LIST = Arrays.asList(
            "com.baomidou.mybatisplus.core.conditions.query.QueryWrapper",
            "com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper",
            "com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper",
            "com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper");

    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonSerializer.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SimpleModule MODULE = new SimpleModule() {
        @Override
        public void setupModule(SetupContext context) {
            super.setupModule(context);
            context.addBeanSerializerModifier(new CustomSerializerModifier());
        }
    };

    public static final JacksonSerializer INSTANCE = new JacksonSerializer();

    private JacksonSerializer() {
        configMapper();
        customTimeFormatSerializer(MODULE);
        customTimeFormatDeserializer(MODULE);

        MAPPER.registerModule(MODULE);
    }

    @Override
    public String serialize(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception ex) {
            LOGGER.warn("jackson-serialize", ex);
        }
        return null;
    }

    @Override
    public <T> T deserialize(String json, Class<T> clazz) {
        if (StringUtil.isEmpty(json) || clazz == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception ex) {
            LOGGER.warn("jackson-deserialize-clazz", ex);
        }
        return null;
    }

    @Override
    public <T> T deserialize(String json, Type type) {
        if (StringUtil.isEmpty(json) || type == null) {
            return null;
        }

        JavaType javaType = MAPPER.getTypeFactory().constructType(type);
        return JacksonSerializer.INSTANCE.deserialize(json, javaType, type);
    }

    public <T> T deserialize(String json, JavaType javaType, Type type) {
        try {
            return MAPPER.readValue(json, javaType);
        } catch (Exception ex) {
            LOGGER.warn("jackson-deserialize-type", ex);
        }
        return null;
    }

    private void configMapper() {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        //MAPPER.configure(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature(), true);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        MAPPER.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    private void customTimeFormatSerializer(SimpleModule module) {
        //module.addSerializer(DateTime.class, new DateTimeSerialize());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerialize());
        module.addSerializer(LocalDate.class, new LocalDateSerialize());
        module.addSerializer(LocalTime.class, new LocalTimeSerialize());
        //module.addSerializer(org.joda.time.LocalDateTime.class, new JodaLocalDateTimeSerialize());
        //module.addSerializer(org.joda.time.LocalDate.class, new JodaLocalDateSerialize());
        //module.addSerializer(org.joda.time.LocalTime.class, new JodaLocalTimeSerialize());
        module.addSerializer(Calendar.class, new CalendarSerialize());
        module.addSerializer(GregorianCalendar.class, new GregorianCalendarSerialize());
        module.addSerializer(Timestamp.class, new TimestampSerialize());
        module.addSerializer(XMLGregorianCalendar.class, new XMLGregorianCalendarSerialize());
    }

    private void customTimeFormatDeserializer(SimpleModule module) {
        //module.addDeserializer(DateTime.class, new DateTimeDeserialize());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserialize());
        module.addDeserializer(LocalDate.class, new LocalDateDeserialize());
        module.addDeserializer(LocalTime.class, new LocalTimeDeserialize());
        //module.addDeserializer(org.joda.time.LocalDateTime.class, new JodaLocalDateTimeDeserialize());
        //module.addDeserializer(org.joda.time.LocalDate.class, new JodaLocalDateDeserialize());
        //module.addDeserializer(org.joda.time.LocalTime.class, new JodaLocalTimeDeserialize());
        module.addDeserializer(Calendar.class, new CalendarDeserialize());
        module.addDeserializer(GregorianCalendar.class, new GregorianCalendarDeserialize());
        module.addDeserializer(Timestamp.class, new TimestampDeserialize());
        module.addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserialize());
    }

    /**
     * custom serialized fields
     */
    public static class CustomSerializerModifier extends BeanSerializerModifier {

        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                         BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {

            String className = beanDesc.getBeanClass().getName();
            // Special treatment MybatisPlus, only serializes the paramNameValuePairs field of QueryWrapper or UpdateWrapper.
            if (MYBATIS_PLUS_CLASS_LIST.contains(className)) {
                beanProperties.removeIf(beanPropertyWriter -> !StringUtils.equals(beanPropertyWriter.getName(), "paramNameValuePairs"));
            }

            return beanProperties;
        }
    }


    // region Custom Serializer/Deserialize



    static class LocalDateTimeSerialize extends com.fasterxml.jackson.databind.JsonSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(
                    value.format(DateFormatParser.INSTANCE.getFormatter(DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS)));
        }
    }


    static class LocalDateTimeDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return LocalDateTime.parse(node.asText(),
                    DateFormatParser.INSTANCE.getFormatter(node.asText(), DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS));
        }
    }


    static class LocalDateSerialize extends com.fasterxml.jackson.databind.JsonSerializer<LocalDate> {

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(
                    value.format(DateFormatParser.INSTANCE.getFormatter(DatePatternConstants.SHORT_DATE_FORMAT)));
        }
    }


    static class LocalDateDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return LocalDate.parse(node.asText(),
                    DateFormatParser.INSTANCE.getFormatter(node.asText(), DatePatternConstants.SHORT_DATE_FORMAT));
        }
    }


    static class LocalTimeSerialize extends com.fasterxml.jackson.databind.JsonSerializer<LocalTime> {

        @Override
        public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value
                    .format(DateFormatParser.INSTANCE.getFormatter(DatePatternConstants.SHORT_TIME_FORMAT_MILLISECOND)));
        }
    }


    static class LocalTimeDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<LocalTime> {

        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return LocalTime.parse(node.asText(), DateFormatParser.INSTANCE
                    .getFormatter(node.asText(), DatePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));
        }
    }




    static class CalendarSerialize extends com.fasterxml.jackson.databind.JsonSerializer<Calendar> {

        @Override
        public void serialize(Calendar value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(DateFormatUtils
                    .format(value, DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE, value.getTimeZone()));
        }
    }


    static class CalendarDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<Calendar> {

        @Override
        public Calendar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return DateFormatParser.INSTANCE.parseCalendar(node.asText());
        }
    }


    static class GregorianCalendarSerialize extends com.fasterxml.jackson.databind.JsonSerializer<GregorianCalendar> {

        @Override
        public void serialize(GregorianCalendar value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(DateFormatUtils
                    .format(value, DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE, value.getTimeZone()));
        }
    }


    static class GregorianCalendarDeserialize
            extends com.fasterxml.jackson.databind.JsonDeserializer<GregorianCalendar> {

        @Override
        public GregorianCalendar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return DateFormatParser.INSTANCE.parseGregorianCalendar(node.asText());
        }
    }



    static class XMLGregorianCalendarSerialize
            extends com.fasterxml.jackson.databind.JsonSerializer<XMLGregorianCalendar> {

        @Override
        public void serialize(XMLGregorianCalendar value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            GregorianCalendar calendar = value.toGregorianCalendar();
            gen.writeString(DateFormatUtils
                    .format(calendar, DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE, calendar.getTimeZone()));
        }
    }


    static class XMLGregorianCalendarDeserialize
            extends com.fasterxml.jackson.databind.JsonDeserializer<XMLGregorianCalendar> {

        @Override
        public XMLGregorianCalendar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            GregorianCalendar gregorianCalendar = DateFormatParser.INSTANCE.parseGregorianCalendar(node.asText());
            XMLGregorianCalendar xmlGregorianCalendar = null;
            try {
                xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
            } catch (Exception ex) {
                LOGGER.warn("XMLGregorianCalendar.deserialize", ex);
            }
            return xmlGregorianCalendar;
        }
    }


    static class TimestampSerialize extends com.fasterxml.jackson.databind.JsonSerializer<Timestamp> {

        @Override
        public void serialize(Timestamp value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(DateFormatUtils.format(value, DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS));

        }
    }


    static class TimestampDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<Timestamp> {

        @Override
        public Timestamp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return Optional.ofNullable(DateFormatParser.INSTANCE.parseDate(node.asText()))
                    .map(date -> new Timestamp(date.getTime())).orElse(new Timestamp(System.currentTimeMillis()));
        }
    }



    // endregion


    @JsonIgnoreType
    static class IgnoreType {
    }


    static final class DatePatternConstants {
        /**
         * yyyy-MM-dd HH:mm:ss.SSS/yyyy-MM-dd'T'HH:mm:ss.SSSZZZ
         */
        public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String SIMPLE_DATE_FORMAT_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";
        /**
         *  2020-06-09T09:00:00.000+08:00
         */
        public static final String SIMPLE_DATE_FORMAT_WITH_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ";
        /**
         * yyyy-MM-dd
         */
        public static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
        /**
         * HH:mm:ss.SSS
         */
        public static final String SHORT_TIME_FORMAT = "HH:mm:ss";
        public static final String SHORT_TIME_FORMAT_MILLISECOND = "HH:mm:ss.SSS";
    }


    static final class TimezoneParser {
        public static final TimezoneParser INSTANCE = new TimezoneParser();
        private static final String DEFAULT_TIME_ZONE_FORMAT = "(([+\\-])\\d{2}:\\d{2})|Z";
        private static final Pattern DEFAULT_TIME_ZONE_PATTERN = Pattern.compile(DEFAULT_TIME_ZONE_FORMAT);
        private final ConcurrentHashMap<String, TimeZone> timeZonesMap = new ConcurrentHashMap<>();

        public TimeZone parse(String date) {
            if (StringUtil.isBlank(date)) {
                return TimeZone.getDefault();
            }

            // 2020-06-09T09:00:00.000+08:00 length=29
            if (date.length() <= DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE.length()) {
                return TimeZone.getDefault();
            }

            // 2020-06-09T09:00:00.000+08:00 substring from index 23, result is +08:00
            date = date.substring(DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS.length());

            Matcher matcher = DEFAULT_TIME_ZONE_PATTERN.matcher(date);
            if (!matcher.matches()) {
                return TimeZone.getDefault();
            }

            return getTimeZone(date);
        }

        private TimeZone getTimeZone(final String zoneId) {
            return timeZonesMap.computeIfAbsent(zoneId, (id) -> TimeZone.getTimeZone("GMT" + zoneId));
        }
    }


    static final class DateFormatParser {
        private static final Map<String, DateTimeFormatter> DEFAULT_FORMATTER_MAP = new ConcurrentHashMap<>();
        private static final Map<Integer, String> DATE_PATTERN_MAP = new HashMap<>();

        public static final DateFormatParser INSTANCE = new DateFormatParser();

        private DateFormatParser() {
            initDatePatternMap();
        }

        public void initDatePatternMap() {
            DATE_PATTERN_MAP
                    .put(DatePatternConstants.SIMPLE_DATE_FORMAT.length(), DatePatternConstants.SIMPLE_DATE_FORMAT);
            DATE_PATTERN_MAP
                    .put(DatePatternConstants.SHORT_TIME_FORMAT.length(), DatePatternConstants.SHORT_TIME_FORMAT);
            DATE_PATTERN_MAP.put(DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS.length(),
                    DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS);
            DATE_PATTERN_MAP.put(DatePatternConstants.SHORT_TIME_FORMAT_MILLISECOND.length(),
                    DatePatternConstants.SHORT_TIME_FORMAT_MILLISECOND);
            DATE_PATTERN_MAP.put(DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE.length() + 1,
                    DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE);
        }


        public Calendar parseCalendar(final String source) {
            TimeZone timeZone = TimezoneParser.INSTANCE.parse(source);
            Calendar calendar = Calendar.getInstance(timeZone);
            return Optional.ofNullable(parseDate(source, timeZone)).map(date -> {
                calendar.setTime(date);
                return calendar;
            }).orElse(calendar);
        }

        public GregorianCalendar parseGregorianCalendar(final String source) {
            TimeZone timeZone = TimezoneParser.INSTANCE.parse(source);
            GregorianCalendar gregorianCalendar = new GregorianCalendar(timeZone);
            return Optional.ofNullable(parseDate(source, timeZone)).map(date -> {
                gregorianCalendar.setTime(date);
                return gregorianCalendar;
            }).orElse(gregorianCalendar);
        }

        /**
         * Parse date without timezone
         *
         * @param source datetime string
         * @return Date
         */
        public Date parseDate(final String source) {
            return parseDate(source, null);
        }

        /**
         * Parse date with timezone
         *
         * @param source datetime string
         * @return Date
         */
        public Date parseDate(String source, TimeZone timeZone) {
            if (StringUtil.isEmpty(source)) {
                return null;
            }

            if (timeZone == null) {
                timeZone = TimezoneParser.INSTANCE.parse(source);
            }

            try {
                if (source.length() >= DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE.length()) {
                    source = source.substring(0, DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS.length()).replace('T', ' ');
                }

                String pattern =
                        DATE_PATTERN_MAP.getOrDefault(source.length(), DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS);
                return FastDateFormat.getInstance(pattern, timeZone).parse(source);
            } catch (ParseException e) {
                LOGGER.error("parseDate", e);
                return null;
            }
        }

        /**
         * Get datetime formatter
         *
         * @param source datetime string
         * @param defaultPattern default datetime pattern
         * @return DateTimeFormatter instance
         */
        public DateTimeFormatter getFormatter(final String source, final String defaultPattern) {
            String pattern = DATE_PATTERN_MAP.getOrDefault(source.length(), defaultPattern);
            return DEFAULT_FORMATTER_MAP.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
        }

        /**
         * Get datetime formatter
         *
         * @param pattern datetime pattern
         * @return DateTimeFormatter instance
         */
        public DateTimeFormatter getFormatter(final String pattern) {
            return DEFAULT_FORMATTER_MAP.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
        }
    }

}

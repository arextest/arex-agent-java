package io.arex.foundation.serializer;

import com.google.auto.service.AutoService;

import io.arex.agent.bootstrap.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import io.arex.agent.thirdparty.util.time.DateFormatUtils;
import io.arex.agent.thirdparty.util.time.FastDateFormat;
import io.arex.foundation.util.JdkUtils;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.SerializeSkipInfo;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.util.TypeUtil;

import java.sql.Time;
import java.time.Instant;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
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

@AutoService(StringSerializable.class)
public final class JacksonSerializer implements StringSerializable {
    public static final String EXTENSION = "json";

    private static final String SKIP_INFO_LIST_TYPE = "java.util.ArrayList-io.arex.inst.runtime.model.SerializeSkipInfo";

    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonSerializer.class);

    private final ObjectMapper MAPPER = new ObjectMapper();
    private final Map<String, List<String>> skipInfoMap = new ConcurrentHashMap<>();
    private static final SimpleModule MODULE = new JacksonSimpleModule();

    public static JacksonSerializer INSTANCE = new JacksonSerializer();

    @Override
    public String name() {
        return "jackson";
    }

    public JacksonSerializer() {
        buildSkipInfoMap();
        configMapper();
        customTimeFormatSerializer(MODULE);
        customTimeFormatDeserializer(MODULE);

        MAPPER.registerModule(MODULE);
    }

    private void buildSkipInfoMap() {
        try {
            Config config = Config.get();
            if (config == null) {
                return;
            }
            String skipInfoString = config
                    .getString(ArexConstants.SERIALIZE_SKIP_INFO_CONFIG_KEY, StringUtil.EMPTY);
            if (StringUtil.isBlank(skipInfoString)) {
                return;
            }
            JavaType javaType = MAPPER.getTypeFactory().constructType(TypeUtil.forName(SKIP_INFO_LIST_TYPE));
            List<SerializeSkipInfo> serializeSkipInfos = MAPPER.readValue(skipInfoString, javaType);

            if (serializeSkipInfos == null || serializeSkipInfos.isEmpty()) {
                return;
            }
            for (SerializeSkipInfo skipInfo : serializeSkipInfos) {
                String className = skipInfo.getFullClassName();
                List<String> fieldNameList = skipInfo.getFieldNameList();
                if (StringUtil.isBlank(className) || fieldNameList == null) {
                    continue;
                }
                skipInfoMap.put(className, fieldNameList);
            }
        } catch (Throwable ex) {
            LOGGER.warn("buildSkipInfoMap", ex);
        }
    }

    public List<String> getSkipFieldNameList(String className) {
        return skipInfoMap.get(className);
    }

    @Override
    public String serialize(Object object) throws Throwable {
        if (object == null) {
            return null;
        }

        return MAPPER.writeValueAsString(object);
    }

    @Override
    public <T> T deserialize(String json, Class<T> clazz) throws Throwable {
        if (StringUtil.isEmpty(json) || clazz == null) {
            return null;
        }

        return MAPPER.readValue(json, clazz);
    }

    @Override
    public <T> T deserialize(String json, Type type) throws Throwable {
        if (StringUtil.isEmpty(json) || type == null) {
            return null;
        }

        JavaType javaType = MAPPER.getTypeFactory().constructType(type);
        return deserialize(json, javaType);
    }

    @Override
    public StringSerializable reCreateSerializer() {
        INSTANCE = new JacksonSerializer();
        return INSTANCE;
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    public <T> T deserialize(String json, JavaType javaType) {
        try {
            return MAPPER.readValue(json, javaType);
        } catch (Throwable ex) {
            LogManager.warn("jackson-deserialize-type", ex);
        }
        return null;
    }

    private void configMapper() {
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
        module.addSerializer(DateTime.class, new DateTimeSerialize());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerialize());
        module.addSerializer(LocalDate.class, new LocalDateSerialize());
        module.addSerializer(LocalTime.class, new LocalTimeSerialize());
        module.addSerializer(org.joda.time.LocalDateTime.class, new JodaLocalDateTimeSerialize());
        module.addSerializer(org.joda.time.LocalDate.class, new JodaLocalDateSerialize());
        module.addSerializer(org.joda.time.LocalTime.class, new JodaLocalTimeSerialize());
        module.addSerializer(Calendar.class, new CalendarSerialize());
        module.addSerializer(GregorianCalendar.class, new GregorianCalendarSerialize());
        module.addSerializer(Timestamp.class, new TimestampSerialize());
        module.addSerializer(XMLGregorianCalendar.class, new XMLGregorianCalendarSerialize());
        // java.sql.Date/Time serialize same as java.util.Date
        module.addSerializer(Date.class, new DateSerialize());
        module.addSerializer(Instant.class, new InstantSerialize());
    }

    private void customTimeFormatDeserializer(SimpleModule module) {
        module.addDeserializer(DateTime.class, new DateTimeDeserialize());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserialize());
        module.addDeserializer(LocalDate.class, new LocalDateDeserialize());
        module.addDeserializer(LocalTime.class, new LocalTimeDeserialize());
        module.addDeserializer(org.joda.time.LocalDateTime.class, new JodaLocalDateTimeDeserialize());
        module.addDeserializer(org.joda.time.LocalDate.class, new JodaLocalDateDeserialize());
        module.addDeserializer(org.joda.time.LocalTime.class, new JodaLocalTimeDeserialize());
        module.addDeserializer(Calendar.class, new CalendarDeserialize());
        module.addDeserializer(GregorianCalendar.class, new GregorianCalendarDeserialize());
        module.addDeserializer(Timestamp.class, new TimestampDeserialize());
        module.addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserialize());
        module.addDeserializer(Date.class, new DateDeserialize());
        module.addDeserializer(java.sql.Date.class, new SqlDateDeserialize());
        module.addDeserializer(Time.class, new SqlTimeDeserialize());
        module.addDeserializer(Instant.class, new InstantDeserialize());
    }

    private static class JacksonSimpleModule extends SimpleModule {

        @Override
        public void setupModule(SetupContext context) {
            super.setupModule(context);
            context.addBeanSerializerModifier(new CustomSerializerModifier());
        }
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
                beanProperties.removeIf(beanPropertyWriter -> !StringUtil.equals(beanPropertyWriter.getName(), "paramNameValuePairs"));
            }
            if (TK_MYBATIS_PLUS_CLASS_LIST.contains(className)){
                beanProperties.removeIf(beanPropertyWriter -> StringUtil.equals(beanPropertyWriter.getName(),"table"));
            }

            List<String> fieldNameList = JacksonSerializer.INSTANCE.getSkipFieldNameList(className);

            if (fieldNameList == null) {
                return beanProperties;
            }

            if (fieldNameList.isEmpty()) {
                beanProperties.clear();
                return beanProperties;
            }

            beanProperties.removeIf(beanPropertyWriter -> fieldNameList.contains(beanPropertyWriter.getName()));
            return beanProperties;
        }
    }


    // region Custom Serializer/Deserialize


    static class DateTimeSerialize extends com.fasterxml.jackson.databind.JsonSerializer<DateTime> {

        @Override
        public void serialize(DateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString(DatePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE_DATETIME));
        }
    }


    static class DateTimeDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<DateTime> {

        @Override
        public DateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            TimeZone timeZone = TimezoneParser.INSTANCE.parse(node.asText());
            DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(timeZone);
            Date date = DateFormatParser.INSTANCE.parseDate(node.asText(), timeZone);
            if (date == null) {
                return null;
            }
            return new DateTime(date.getTime()).withZone(dateTimeZone);
        }
    }

    static class JodaLocalDateTimeSerialize extends com.fasterxml.jackson.databind.JsonSerializer<org.joda.time.LocalDateTime> {

        @Override
        public void serialize(org.joda.time.LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString(DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS));
        }
    }


    static class JodaLocalDateTimeDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<org.joda.time.LocalDateTime> {

        @Override
        public org.joda.time.LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return org.joda.time.LocalDateTime.parse(node.asText(), DateTimeFormat.forPattern(DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS));
        }
    }

    static class JodaLocalDateSerialize extends com.fasterxml.jackson.databind.JsonSerializer<org.joda.time.LocalDate> {

        @Override
        public void serialize(org.joda.time.LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString(DatePatternConstants.SHORT_DATE_FORMAT));
        }
    }


    static class JodaLocalDateDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<org.joda.time.LocalDate> {

        @Override
        public org.joda.time.LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return org.joda.time.LocalDate.parse(node.asText(), DateTimeFormat.forPattern(DatePatternConstants.SHORT_DATE_FORMAT));
        }
    }


    static class JodaLocalTimeSerialize extends com.fasterxml.jackson.databind.JsonSerializer<org.joda.time.LocalTime> {

        @Override
        public void serialize(org.joda.time.LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString(DatePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));
        }
    }


    static class JodaLocalTimeDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<org.joda.time.LocalTime> {

        @Override
        public org.joda.time.LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return org.joda.time.LocalTime.parse(node.asText(), DateTimeFormat.forPattern(DatePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));
        }
    }

    static class DateSerialize extends com.fasterxml.jackson.databind.JsonSerializer<Date> {

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(DateFormatUtils.format(value, DatePatternConstants.SIMPLE_DATE_FORMAT_MILLIS));
        }
    }


    static class DateDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return DateFormatParser.INSTANCE.parseDate(node.asText());
        }
    }

    static class SqlDateDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<java.sql.Date> {

        @Override
        public java.sql.Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            Date date = DateFormatParser.INSTANCE.parseDate(node.asText());
            if (date == null) {
                return new java.sql.Date(System.currentTimeMillis());
            }
            return new java.sql.Date(date.getTime());
        }
    }

    static class SqlTimeDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<Time> {

        @Override
        public Time deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            Date date = DateFormatParser.INSTANCE.parseDate(node.asText());
            if (date == null) {
                date = new Date();
            }
            return new Time(date.getTime());
        }
    }

    static class LocalDateTimeSerialize extends com.fasterxml.jackson.databind.JsonSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(
                    value.format(DateFormatParser.INSTANCE.getFormatter(DatePatternConstants.localDateTimeFormat)));
        }
    }


    static class LocalDateTimeDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return LocalDateTime.parse(node.asText(),
                    DateFormatParser.INSTANCE.getFormatter(DatePatternConstants.localDateTimeFormat));
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
                    .format(DateFormatParser.INSTANCE.getFormatter(DatePatternConstants.localTimeFormat)));
        }
    }


    static class LocalTimeDeserialize extends com.fasterxml.jackson.databind.JsonDeserializer<LocalTime> {

        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return LocalTime.parse(node.asText(), DateFormatParser.INSTANCE
                    .getFormatter(DatePatternConstants.localTimeFormat));
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

    static class InstantSerialize extends JsonSerializer<Instant> {

        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeString(value.toString());
        }
    }

    static class InstantDeserialize extends JsonDeserializer<Instant> {

        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return Instant.parse(node.asText());
        }
    }

    // endregion


    @JsonIgnoreType
    static class IgnoreType {
    }


    public static final class DatePatternConstants {
        /**
         * yyyy-MM-dd HH:mm:ss.SSS/yyyy-MM-dd'T'HH:mm:ss.SSSZZZ
         */
        public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String SIMPLE_DATE_FORMAT_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";
        public static final String SIMPLE_DATE_FORMAT_NANOSECOND = "yyyy-MM-dd HH:mm:ss.SSSSSSSSS";
        /**
         *  2020-06-09T09:00:00.000+08:00
         */
        public static final String SIMPLE_DATE_FORMAT_WITH_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ";
        public static final String SIMPLE_DATE_FORMAT_WITH_TIMEZONE_DATETIME = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
        /**
         * yyyy-MM-dd
         */
        public static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
        /**
         * HH:mm:ss.SSS
         */
        public static final String SHORT_TIME_FORMAT = "HH:mm:ss";
        public static final String SHORT_TIME_FORMAT_MILLISECOND = "HH:mm:ss.SSS";
        public static final String SHORT_TIME_FORMAT_NANOSECOND = "HH:mm:ss.SSSSSSSSS";

        public static String localDateTimeFormat = SIMPLE_DATE_FORMAT_MILLIS;

        public static String localTimeFormat = SHORT_TIME_FORMAT_MILLISECOND;

        static {
            if (JdkUtils.isJdk11()) {
                localDateTimeFormat = SIMPLE_DATE_FORMAT_NANOSECOND;
                localTimeFormat = SHORT_TIME_FORMAT_NANOSECOND;
            }
        }
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

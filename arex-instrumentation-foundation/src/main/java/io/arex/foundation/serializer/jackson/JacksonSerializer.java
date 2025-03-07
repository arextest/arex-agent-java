package io.arex.foundation.serializer.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.serializer.gson.adapter.FastUtilAdapterFactory;
import io.arex.foundation.serializer.jackson.adapter.CalendarAdapter;
import io.arex.foundation.serializer.jackson.adapter.CustomBeanModifier;
import io.arex.foundation.serializer.jackson.adapter.DateAdapter;
import io.arex.foundation.serializer.jackson.adapter.GregorianCalendarAdapter;
import io.arex.foundation.serializer.jackson.adapter.ImmutableListAdapter;
import io.arex.foundation.serializer.jackson.adapter.InstantAdapter;
import io.arex.foundation.serializer.jackson.adapter.JacksonExclusion;
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
import javax.xml.datatype.XMLGregorianCalendar;

@AutoService(StringSerializable.class)
public final class JacksonSerializer implements StringSerializable {

    private final ObjectMapper MAPPER = new ArexObjectMapper();
    private static final SimpleModule MODULE = new JacksonSimpleModule();

    public static JacksonSerializer INSTANCE = new JacksonSerializer();

    @Override
    public String name() {
        return "jackson";
    }

    public JacksonSerializer() {
        configMapper();
        MODULE.setSerializers(new CustomBeanModifier.Serializers());
        MODULE.setDeserializers(new CustomBeanModifier.Deserializers());
        customTimeFormatSerializer(MODULE);
        customTimeFormatDeserializer(MODULE);
        customTypeResolver();
        MAPPER.registerModule(MODULE);
    }

    private void customTypeResolver() {
        try {
            TypeResolverBuilder<?> typeResolver = new CustomTypeResolverBuilder();
            typeResolver.init(JsonTypeInfo.Id.CLASS, null);
            typeResolver.inclusion(JsonTypeInfo.As.PROPERTY);
            typeResolver.typeProperty("@CLASS");
            MAPPER.setDefaultTyping(typeResolver);
        } catch (Throwable ignored) {
            // jackson version is too low, ignore
        }
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
    }

    private void customTimeFormatSerializer(SimpleModule module) {
        DateAdapter.Serializer dateSerializer = new DateAdapter.Serializer();
        CalendarAdapter.Serializer calendarSerializer = new CalendarAdapter.Serializer();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeAdapter.Serializer());
        module.addSerializer(LocalDate.class, new LocalDateAdapter.Serializer());
        module.addSerializer(LocalTime.class, new LocalTimeAdapter.Serializer());
        module.addSerializer(Calendar.class, calendarSerializer);
        module.addSerializer(GregorianCalendar.class, calendarSerializer);
        module.addSerializer(Timestamp.class, dateSerializer);
        module.addSerializer(XMLGregorianCalendar.class, new XMLGregorianCalendarAdapter.Serializer());
        // java.sql.Date/Time serialize same as java.util.Date
        module.addSerializer(Date.class, dateSerializer);
        module.addSerializer(Instant.class, new InstantAdapter.Serializer());
        module.addSerializer(OffsetDateTime.class, new OffsetDateTimeAdapter.Serializer());
    }

    private void customTimeFormatDeserializer(SimpleModule module) {
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeAdapter.Deserializer());
        module.addDeserializer(LocalDate.class, new LocalDateAdapter.Deserializer());
        module.addDeserializer(LocalTime.class, new LocalTimeAdapter.Deserializer());
        module.addDeserializer(Calendar.class, new CalendarAdapter.Deserializer());
        module.addDeserializer(GregorianCalendar.class, new GregorianCalendarAdapter.Deserializer());
        module.addDeserializer(Timestamp.class, new TimestampAdapter.Deserializer());
        module.addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarAdapter.Deserializer());
        module.addDeserializer(Date.class, new DateAdapter.Deserializer());
        module.addDeserializer(Instant.class, new InstantAdapter.Deserializer());
        module.addDeserializer(OffsetDateTime.class, new OffsetDateTimeAdapter.Deserializer());
        module.addDeserializer(java.sql.Date.class, new SqlDateAdapter.Deserializer());
        module.addDeserializer(Time.class, new SqlTimeAdapter.Deserializer());
        module.addDeserializer(ImmutableList.class, new ImmutableListAdapter.Deserializer());
    }

    public static class JacksonSimpleModule extends SimpleModule {

        @Override
        public void setupModule(SetupContext context) {
            super.setupModule(context);
            context.addBeanSerializerModifier(new JacksonExclusion());
        }
    }

    @JsonIgnoreType
    static class IgnoreType {

    }

    private static class CustomTypeResolverBuilder extends DefaultTypeResolverBuilder {

        public CustomTypeResolverBuilder() {
            super(DefaultTyping.NON_FINAL, LaissezFaireSubTypeValidator.instance);
        }

        /**
         * @return true will serialize with runtime type info
         */
        @Override
        public boolean useForType(JavaType type) {
            Class<?> rawClass = type.getRawClass();
            return rawClass.isInterface() &&
                StringUtil.startWith(rawClass.getName(), FastUtilAdapterFactory.FASTUTIL_PACKAGE);
        }
    }
}

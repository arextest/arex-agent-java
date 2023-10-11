package io.arex.foundation.serializer;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.util.JsonFormat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.util.TypeUtil;


public class ProtoJsonSerializer implements StringSerializable{
    private static final ProtoJsonSerializer INSTANCE = new ProtoJsonSerializer();
    private static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer().omittingInsignificantWhitespace();
    private static final JsonFormat.Parser JSON_PARSER = JsonFormat.parser().ignoringUnknownFields();

    public static ProtoJsonSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public String name() {
        return "protoJson";
    }

    /**
     * for the type inside the Collection is the PB type,
     * traverse the elements inside the Collection and serialize them one by one with separators
     */
    @Override
    public String serialize(Object object) {
        try {
            return JSON_PRINTER.print((AbstractMessage) object);
        } catch (Throwable e) {
            LogManager.warn("proto-serialize", e);
            return StringUtil.EMPTY;
        }
    }

    @Override
    public <T> T deserialize(String value, Class<T> clazz) {
        if (StringUtil.isEmpty(value) || clazz == null) {
            return null;
        }

        try {
            Builder<?> builder = getMessageBuilder(clazz);

            JSON_PARSER.merge(value, builder);

            return (T) builder.build();
        } catch (Throwable e) {
            LogManager.warn("proto-deserialize", e);
            return null;
        }
    }

    @Override
    public <T> T deserialize(String value, Type type) {
        Class<T> rawClass = (Class<T>) TypeUtil.getRawClass(type);
        return deserialize(value, rawClass);
    }

    @Override
    public StringSerializable reCreateSerializer() {
        return INSTANCE;
    }

    /**
     * Create Class.Builder based on Class reflection for Json deserialization
     * @param clazz
     * @return Class.Builder
     */
    private Builder<?> getMessageBuilder(Class<?> clazz)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = clazz.getDeclaredMethod("newBuilder");
        return (Builder<?>) method.invoke(null);
    }

}

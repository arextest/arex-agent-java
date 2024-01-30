package io.arex.foundation.serializer;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.util.JsonFormat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;


public class ProtoJsonSerializer{
    private static final ProtoJsonSerializer INSTANCE = new ProtoJsonSerializer();
    private final JsonFormat.Printer jsonPrinter;
    private final JsonFormat.Parser jsonParser;

    private ProtoJsonSerializer() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String name = classLoader == null ? null : classLoader.getClass().getName();
        LogManager.info("proto-init", StringUtil.format("ProtoJsonSerializer init, classLoader: %s", name));
        this.jsonPrinter = JsonFormat.printer().omittingInsignificantWhitespace();
        this.jsonParser = JsonFormat.parser().ignoringUnknownFields();
    }

    public static ProtoJsonSerializer getInstance() {
        return INSTANCE;
    }

    /**
     * for the type inside the Collection is the PB type,
     * traverse the elements inside the Collection and serialize them one by one with separators
     */
    public String serialize(Object object) {
        try {
            return jsonPrinter.print((AbstractMessage) object);
        } catch (Throwable e) {
            LogManager.warn("proto-serialize", e);
            return StringUtil.EMPTY;
        }
    }

    public <T> T deserialize(String value, Class<T> clazz) {
        if (StringUtil.isEmpty(value) || clazz == null) {
            return null;
        }

        try {
            Builder<?> builder = getMessageBuilder(clazz);

            jsonParser.merge(value, builder);

            return (T) builder.build();
        } catch (Throwable e) {
            LogManager.warn("proto-deserialize", e);
            return null;
        }
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

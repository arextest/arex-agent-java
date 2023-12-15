package io.arex.foundation.serializer;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.util.JsonFormat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;


public class ProtoJsonSerializer{
    private static JsonFormat.Printer jsonPrinter;
    private static JsonFormat.Parser jsonParser;

    static {
        try {
            jsonPrinter = JsonFormat.printer().omittingInsignificantWhitespace();
            jsonParser = JsonFormat.parser().ignoringUnknownFields();
        } catch (Exception e) {
            LogManager.warn("proto-serializer", e);
        }
    }
    private static final ProtoJsonSerializer INSTANCE = new ProtoJsonSerializer();

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

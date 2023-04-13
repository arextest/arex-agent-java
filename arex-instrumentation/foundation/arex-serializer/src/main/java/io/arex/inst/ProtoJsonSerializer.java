package io.arex.inst;

import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.AbstractMessage;
import io.arex.agent.bootstrap.internal.WeakCache;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.TypeUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import org.slf4j.LoggerFactory;

public class ProtoJsonSerializer implements StringSerializable{
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProtoJsonSerializer.class);
    private static final ProtoJsonSerializer INSTANCE = new ProtoJsonSerializer();
    private static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer().omittingInsignificantWhitespace();
    private static final JsonFormat.Parser JSON_PARSER = JsonFormat.parser().ignoringUnknownFields();
    private static final WeakCache<String, AbstractMessage.Builder<?>> BUILDER_WEAK_CACHE = new WeakCache<>();

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
            if (object instanceof Collection<?>) {
                StringBuilder jsonBuilder = new StringBuilder();
                Collection<?> ans = (Collection<?>) object;
                for (Object innerObject : ans) {
                    jsonBuilder.append(serialize(innerObject));
                    jsonBuilder.append(Serializer.SERIALIZE_SEPARATOR);
                }
                return jsonBuilder.toString();
            }
            return JSON_PRINTER.print((AbstractMessage) object);
        } catch (Throwable e) {
            LOGGER.error(LogUtil.buildTitle("serialize"), e);
            return StringUtil.EMPTY;
        }
    }

    @Override
    public <T> T deserialize(String value, Class<T> clazz) {
        if (StringUtil.isEmpty(value) || clazz == null) {
            return null;
        }

        try {
            AbstractMessage.Builder<?> builder = getMessageBuilder(clazz);

            JSON_PARSER.merge(value, builder);

            return (T) builder.build();
        } catch (Throwable e) {
            LOGGER.error(LogUtil.buildTitle("deserialize"), e);
            return null;
        }
    }

    @Override
    public <T> T deserialize(String value, Type type) {
        Class<T> rawClass = (Class<T>) TypeUtil.getRawClass(type);
        if (rawClass != null && Collection.class.isAssignableFrom(rawClass) && type instanceof ParameterizedType) {
            final Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            return deserializeCollection(value, rawClass, actualTypeArguments);
        }
        return deserialize(value, rawClass);
    }

    /**
     * deserialize collection protoBuf result :
     * for Collection type PB, split the Json string by delimiter, then deserialize and add to Collection one by one
     * @param value : Json string, example:[{}Serializer.SERIALIZE_SEPARATOR{}]
     * @param types example:rawClass: java.util.List; types[0]: entity (entity extends com.google.protobuf.GeneratedMessageV3)
     * @return collection<Proto>
     */
    private <T> T deserializeCollection(String value, Class<?> rawClass, Type[] types) {
        if (types != null && types.length == 1) {
            String[] split = StringUtil.splitByWholeSeparator(value, Serializer.SERIALIZE_SEPARATOR);
            Collection<Object> collection = (Collection<Object>) Serializer.deserialize(Serializer.EMPTY_LIST_JSON, rawClass);
            for (String innerObject : split) {
                if (StringUtil.isEmpty(innerObject)) {
                    continue;
                }
                Object deserialize = deserialize(innerObject, (Class<T>) TypeUtil.getRawClass(types[0]));
                if (collection != null && deserialize != null) {
                    collection.add(deserialize);
                }
            }
            return (T) collection;
        }
        return null;
    }

    /**
     * Create Class.Builder based on Class reflection for Json deserialization
     * @param clazz
     * @return Class.Builder
     * JSON_PARSER.merge(value, builder) will modify the value of build, so clear() is required every time the build object is obtained from the cacheMap.
     */
    private AbstractMessage.Builder<?> getMessageBuilder(Class<?> clazz)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Builder<?> cacheBuild = BUILDER_WEAK_CACHE.get(clazz.getName());
        if (cacheBuild != null) {
            return cacheBuild.clear();
        }
        Method method = clazz.getDeclaredMethod("newBuilder");
        Builder<?> builder = (Builder<?>) method.invoke(null);
        BUILDER_WEAK_CACHE.put(clazz.getName(), builder);
        return builder;
    }

}

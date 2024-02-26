package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.protobuf.AbstractMessage;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.serializer.ProtoJsonSerializer;
import java.io.IOException;

public class ProtobufAdapterFactory implements TypeAdapterFactory {
    private static final String PROTOCOL_BUFFER_PACKAGE = "com.google.protobuf";

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type == null) {
            return null;
        }
        final Class<? super T> rawClass = type.getRawType();
        if (isProtobufClass(rawClass)) {
            return (TypeAdapter<T>) new ProtoBufTypeAdapter(rawClass);
        }
        return null;
    }

    private boolean isProtobufClass(Class<?> clazz) {
        if (clazz == null || clazz.isInterface()) {
            return false;
        }
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass == null) {
            return false;
        }
        return StringUtil.startWith(superclass.getName(), PROTOCOL_BUFFER_PACKAGE);
    }

    static class ProtoBufTypeAdapter extends TypeAdapter<AbstractMessage> {
        private static final Gson GSON = new Gson();
        private final Class<?> rawClass;

        public ProtoBufTypeAdapter(Class<?> rawClass) {
            this.rawClass = rawClass;
        }

        @Override
        public void write(JsonWriter out, AbstractMessage value) throws IOException {
            final String json = ProtoJsonSerializer.getInstance().serialize(value);
            out.jsonValue(json);
        }

        /**
         * Streams.parse(in) will return a JsonObject object, which needs to be serialized to json first,
         * and then deserialized and restored to the original type by ProtoJsonSerializer
         */
        @Override
        public AbstractMessage read(JsonReader in) throws IOException {
            final JsonElement parse = Streams.parse(in);
            final String json = GSON.toJson(parse);
            return (AbstractMessage) ProtoJsonSerializer.getInstance().deserialize(json, rawClass);
        }
    }
}

package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.protobuf.AbstractMessage;
import io.arex.foundation.serializer.ProtoJsonSerializer;

import java.io.IOException;

public class ProtoBufTypeAdapter extends TypeAdapter<AbstractMessage> {
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

package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.arex.foundation.serializer.jackson.JacksonSerializerWithType;
import io.arex.foundation.serializer.util.GuavaRangeManager;
import io.arex.inst.runtime.log.LogManager;

import java.io.IOException;

public class GuavaRangeAdapter extends TypeAdapter {

    @Override
    public void write(JsonWriter out, Object value) throws IOException {
        try {
            out.value(JacksonSerializerWithType.INSTANCE.serialize(value));
        } catch (Throwable e) {
            LogManager.warn("rang.gson.serialize", e);
        }
    }

    @Override
    public Object read(JsonReader in) throws IOException {
        return GuavaRangeManager.restoreRange(in.nextString());
    }
}

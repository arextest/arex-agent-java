package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.arex.foundation.serializer.jackson.JacksonSerializerWithType;
import io.arex.foundation.serializer.util.GuavaRangeManager;
import io.arex.inst.runtime.log.LogManager;

import java.io.IOException;


public class GuavaRangeAdapter {
    private GuavaRangeAdapter() {
    }

    public static class Serializer extends JsonSerializer {
        @Override
        public void serialize(Object range, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            try {
                gen.writeString(JacksonSerializerWithType.INSTANCE.serialize(range));
            } catch (Throwable e) {
                LogManager.warn("rang.jackson.serialize", e);
            }
        }
    }

    public static class Deserializer extends JsonDeserializer {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return GuavaRangeManager.restoreRange(node.asText());
        }
    }
}

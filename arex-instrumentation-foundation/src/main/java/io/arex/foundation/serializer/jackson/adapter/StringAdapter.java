package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.arex.foundation.serializer.util.DenoisingUtil;

import java.io.IOException;

public class StringAdapter {
    private StringAdapter() {
    }

    public static class Serializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (DenoisingUtil.isUUID(value)) {
                gen.writeString(DenoisingUtil.zeroUUID());
                return;
            }
            if (DenoisingUtil.isIP(value)) {
                gen.writeString(DenoisingUtil.zeroIP());
                return;
            }
            gen.writeString(value);
        }
    }
}

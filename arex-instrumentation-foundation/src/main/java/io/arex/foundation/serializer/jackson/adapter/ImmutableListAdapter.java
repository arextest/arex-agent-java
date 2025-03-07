package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;

public class ImmutableListAdapter {

    private ImmutableListAdapter() {
    }

    public static class Deserializer extends JsonDeserializer<ImmutableList<?>> {

        @Override
        public ImmutableList<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            List<?> list = mapper.readValue(p, new TypeReference<List<?>>() {
            });
            return ImmutableList.copyOf(list);
        }
    }
}

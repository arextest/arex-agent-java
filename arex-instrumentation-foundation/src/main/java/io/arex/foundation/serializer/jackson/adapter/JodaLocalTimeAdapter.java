package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import  io.arex.foundation.serializer.util.DenoisingUtil;
import  io.arex.foundation.serializer.util.TimePatternConstants;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;

public class JodaLocalTimeAdapter {
    private JodaLocalTimeAdapter() {
    }

    public static class RequestSerializer extends JsonSerializer<LocalTime> {
        @Override
        public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(DenoisingUtil.zeroSecondTime(value));
        }
    }

    public static class Serializer extends JsonSerializer<LocalTime> {
        @Override
        public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString(TimePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));
        }
    }


    public static class Deserializer extends JsonDeserializer<LocalTime> {

        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return LocalTime.parse(node.asText(), DateTimeFormat.forPattern(TimePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));
        }
    }
}

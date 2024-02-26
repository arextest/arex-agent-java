package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.arex.agent.thirdparty.util.time.DateFormatUtils;
import  io.arex.foundation.serializer.util.DateFormatParser;
import  io.arex.foundation.serializer.util.DenoisingUtil;
import  io.arex.foundation.serializer.util.TimePatternConstants;

import java.io.IOException;
import java.util.Date;

public class DateAdapter {
    private DateAdapter() {
    }

    public static class RequestSerializer extends JsonSerializer<Date> {
        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(DenoisingUtil.zeroSecondTime(value));
        }
    }

    public static class Serializer extends JsonSerializer<Date> {
        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(DateFormatUtils.format(value, TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS));
        }
    }


    public static class Deserializer extends JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return DateFormatParser.INSTANCE.parseDate(node.asText());
        }
    }
}

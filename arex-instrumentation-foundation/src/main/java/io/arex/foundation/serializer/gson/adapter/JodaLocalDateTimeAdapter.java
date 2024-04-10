package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import  io.arex.foundation.serializer.util.DenoisingUtil;
import  io.arex.foundation.serializer.util.TimePatternConstants;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;

public class JodaLocalDateTimeAdapter  {
    private JodaLocalDateTimeAdapter() {
    }

    public static class RequestSerializer extends TypeAdapter<LocalDateTime> {

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            out.value(DenoisingUtil.zeroSecondTime(value));
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            return LocalDateTime.parse(in.nextString(), DateTimeFormat.forPattern(TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS));
        }
    }

    public static class Serializer extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            out.value(value.toString(TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS));
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            return LocalDateTime.parse(in.nextString(), DateTimeFormat.forPattern(TimePatternConstants.SIMPLE_DATE_FORMAT_MILLIS));
        }
    }
}

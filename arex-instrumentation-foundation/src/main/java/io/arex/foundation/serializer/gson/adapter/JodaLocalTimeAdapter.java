package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import  io.arex.foundation.serializer.util.DenoisingUtil;
import  io.arex.foundation.serializer.util.TimePatternConstants;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;

public class JodaLocalTimeAdapter {
    private JodaLocalTimeAdapter() {
    }

    public static class RequestSerializer extends TypeAdapter<LocalTime> {
        @Override
        public void write(JsonWriter out, LocalTime value) throws IOException {
            out.value(DenoisingUtil.zeroSecondTime(value));
        }

        @Override
        public LocalTime read(JsonReader in) throws IOException {
            return LocalTime.parse(in.nextString(), DateTimeFormat.forPattern(TimePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));
        }
    }

    public static class Serializer extends TypeAdapter<LocalTime> {
        @Override
        public void write(JsonWriter out, LocalTime value) throws IOException {
            out.value(value.toString(TimePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));
        }

        @Override
        public LocalTime read(JsonReader in) throws IOException {
            return LocalTime.parse(in.nextString(), DateTimeFormat.forPattern(TimePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));
        }
    }
}

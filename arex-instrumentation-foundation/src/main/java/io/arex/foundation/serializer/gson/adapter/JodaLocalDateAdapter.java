package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import  io.arex.foundation.serializer.util.TimePatternConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;

public class JodaLocalDateAdapter {
    private JodaLocalDateAdapter() {
    }

    public static class Serializer extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            out.value(value.toString(TimePatternConstants.SHORT_DATE_FORMAT));
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            return LocalDate.parse(in.nextString(), DateTimeFormat.forPattern(TimePatternConstants.SHORT_DATE_FORMAT));
        }
    }
}

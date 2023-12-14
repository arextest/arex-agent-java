package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.ToNumberStrategy;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

public class NumberStrategy implements ToNumberStrategy {

    @Override
    public Number readNumber(JsonReader in) throws IOException {
        String source = in.nextString();
        try {
            if (source.contains(".")) {
                return Double.valueOf(source);
            }
            return Integer.valueOf(source);
        } catch (Exception e) {
            // Integer size limit exceeded
            return Long.valueOf(source);
        }
    }
}

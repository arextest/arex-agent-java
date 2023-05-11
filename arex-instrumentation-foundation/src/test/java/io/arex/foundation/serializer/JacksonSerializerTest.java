package io.arex.foundation.serializer;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class JacksonSerializerTest {
    @Test
    void testLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        String json = JacksonSerializer.INSTANCE.serialize(now);
        LocalDateTime actualResult = JacksonSerializer.INSTANCE.deserialize(json, LocalDateTime.class);
        assertEquals(now, actualResult);
    }

    @Test
    void testLocalTime() {
        LocalDateTime now = LocalDateTime.now();
        String json = JacksonSerializer.INSTANCE.serialize(now);
        LocalDateTime actualResult = JacksonSerializer.INSTANCE.deserialize(json, LocalDateTime.class);
        assertEquals(now, actualResult);
    }

}
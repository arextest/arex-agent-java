package io.arex.foundation.serializer;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.runtime.util.TypeUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Test
    void testNullList() {
        final List<Object> list = new ArrayList<>();
        list.add(null);
        String json = JacksonSerializer.INSTANCE.serialize(list);
        final String name = TypeUtil.getName(list);
        final List<Object> result = JacksonSerializer.INSTANCE.deserialize(json, TypeUtil.forName(name));
        assert result != null;
        assertEquals(list.size(), result.size());
        assertNull(result.get(0));
    }

}
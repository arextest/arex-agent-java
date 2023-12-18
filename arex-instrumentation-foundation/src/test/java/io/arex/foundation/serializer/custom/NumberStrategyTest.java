package io.arex.foundation.serializer.custom;

import com.google.gson.GsonBuilder;
import io.arex.foundation.serializer.GsonSerializer;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NumberStrategyTest {

    static GsonBuilder builder = null;
    @BeforeAll
    static void setUp() {
        builder = new GsonBuilder();
    }

    @AfterAll
    static void tearDown() {
        builder = null;
    }

    @Test
    void test() {
        NumberTest numberTest = new NumberTest();
        Map<String, Object> map = new HashMap<>();
        map.put("Integer", 1);
        map.put("Double", 1.1);
        map.put("Long", 1111111111111111111L);
        map.put("String", "1");
        map.put("nullValue", null);
        numberTest.setMap(map);
        String json = GsonSerializer.INSTANCE.serialize(numberTest);
        System.out.println(json);
        NumberTest deserialize = GsonSerializer.INSTANCE.deserialize(json, NumberTest.class);
        assertEquals(numberTest.getMap().get("Integer"), deserialize.getMap().get("Integer"));
        assertEquals(numberTest.getMap().get("Double"), deserialize.getMap().get("Double"));
        assertEquals(numberTest.getMap().get("Long"), deserialize.getMap().get("Long"));
        assertEquals(numberTest.getMap().get("String"), deserialize.getMap().get("String"));
        assertNull(deserialize.getMap().get("nullValue"));
    }

    static class NumberTest{
        private Map<String, Object> map;

        public Map<String, Object> getMap() {
            return map;
        }

        public void setMap(Map<String, Object> map) {
            this.map = map;
        }
    }
}

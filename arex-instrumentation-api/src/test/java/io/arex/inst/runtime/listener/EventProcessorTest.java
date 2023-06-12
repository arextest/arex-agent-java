package io.arex.inst.runtime.listener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.util.SPIUtil;
import java.lang.reflect.Type;
import java.util.Arrays;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class EventProcessorTest {
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(SPIUtil.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void testOnCreate() {
        EventProcessor.onCreate(EventSource.empty());
    }

    @Test
    void testInitSerializer() {
        // load two class
        Mockito.when(SPIUtil.load(StringSerializable.class, Thread.currentThread()
                .getContextClassLoader())).thenReturn(Arrays.asList(new TestStringSerializable(), new TestStringSerializer2()));
        EventProcessor.onRequest();
        Assertions.assertNotNull(Serializer.getINSTANCE());
        Assertions.assertEquals("jackson", Serializer.getINSTANCE().getSerializer().name());
        Assertions.assertEquals(1, Serializer.getINSTANCE().getSerializers().size());

        // atomic load, only load once
        Mockito.when(SPIUtil.load(StringSerializable.class, Thread.currentThread()
                .getContextClassLoader())).thenReturn(null);
        EventProcessor.onRequest();
        Assertions.assertNotNull(Serializer.getINSTANCE());
    }

    public static class TestStringSerializable implements StringSerializable {

        @Override
        public String name() {
            return "jackson";
        }

        @Override
        public String serialize(Object object) {
            return null;
        }

        @Override
        public <T> T deserialize(String value, Class<T> clazz) {
            return null;
        }

        @Override
        public <T> T deserialize(String value, Type type) {
            return null;
        }

        @Override
        public StringSerializable reCreateSerializer() {
            return null;
        }

        @Override
        public boolean isDefault() {
            return true;
        }
    }

    public static class TestStringSerializer2 implements StringSerializable {

        @Override
        public String name() {
            return "gson";
        }

        @Override
        public String serialize(Object object) {
            return null;
        }

        @Override
        public <T> T deserialize(String value, Class<T> clazz) {
            return null;
        }

        @Override
        public <T> T deserialize(String value, Type type) {
            return null;
        }

        @Override
        public StringSerializable reCreateSerializer() {
            return null;
        }
    }
}
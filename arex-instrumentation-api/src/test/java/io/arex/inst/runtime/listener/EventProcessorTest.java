package io.arex.inst.runtime.listener;

import static org.mockito.ArgumentMatchers.any;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.log.Logger;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.agent.bootstrap.util.ServiceLoader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class EventProcessorTest {
    static Logger logger = null;
    static MockedStatic<LogManager> mockedStatic = null;
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(ServiceLoader.class);
        logger = Mockito.mock(Logger.class);
        mockedStatic = Mockito.mockStatic(LogManager.class);
    }

    @AfterAll
    static void tearDown() {
        logger = null;
        mockedStatic = null;
        Mockito.clearAllCaches();
    }

    @Test
    void testOnCreate() {
        // null context
        EventProcessor.onCreate(EventSource.empty());
        // record scene
        String recordId = "testRecordId";
        ArexContext context = ArexContext.of(recordId);
        Mockito.when(ContextManager.currentContext()).thenReturn(context);
        EventProcessor.onCreate(EventSource.empty());
        mockedStatic.verify(() -> LogManager.info("enter", StringUtil.format("arex-record-id: %s", recordId)), Mockito.times(1));

        // replay scene
        String replayId = "testReplayId";
        context = ArexContext.of(recordId, replayId);
        Mockito.when(ContextManager.currentContext()).thenReturn(context);
        EventProcessor.onCreate(EventSource.empty());
        mockedStatic.verify(() -> LogManager.info("enter", StringUtil.format("arex-record-id: %s, arex-replay-id: %s", recordId, replayId)), Mockito.times(1));
    }

    @Test
    void testInit() {
        // load serializer
        Mockito.when(ServiceLoader.load(StringSerializable.class, Thread.currentThread()
                .getContextClassLoader())).thenReturn(Arrays.asList(new TestStringSerializable(), new TestStringSerializer2()));
        // logger
        Mockito.when(ServiceLoader.load(Logger.class, Thread.currentThread()
                .getContextClassLoader())).thenReturn(Collections.singletonList(logger));

        EventProcessor.onRequest();
        // logger
        mockedStatic.verify(() -> LogManager.build(any()), Mockito.times(1));
        // serializer
        Assertions.assertNotNull(Serializer.getINSTANCE());
        Assertions.assertEquals("jackson", Serializer.getINSTANCE().getSerializer().name());
        Assertions.assertEquals(1, Serializer.getINSTANCE().getSerializers().size());

        // atomic load, only load once
        Mockito.when(ServiceLoader.load(StringSerializable.class, Thread.currentThread()
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
            throw new RuntimeException("test");
        }

        @Override
        public <T> T deserialize(String value, Class<T> clazz) {
            throw new RuntimeException("test");
        }

        @Override
        public <T> T deserialize(String value, Type type) {
            throw new RuntimeException("test");
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
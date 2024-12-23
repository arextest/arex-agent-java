package io.arex.inst.runtime.listener;

import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.log.Logger;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.agent.bootstrap.util.ServiceLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventProcessorTest {
    static Logger logger = null;
    static MockedStatic<LogManager> mockedStatic = null;
    static MockedStatic<ContextManager> contextMockedStatic = null;
    static ConfigBuilder configBuilder;
    @BeforeAll
    static void setUp() {
        contextMockedStatic = Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(ServiceLoader.class);
        logger = Mockito.mock(Logger.class);
        mockedStatic = Mockito.mockStatic(LogManager.class);
        configBuilder = ConfigBuilder.create("testInitClass");
        // disPlay
        configBuilder.build();
    }

    @AfterAll
    static void tearDown() {
        logger = null;
        mockedStatic = null;
        contextMockedStatic = null;
        Mockito.clearAllCaches();
        configBuilder.build();
        configBuilder = null;
    }

    @Test
    @Order(2)
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
    @Order(1)
    void testInit() throws InterruptedException {
        // not Init Complete
        EventProcessor.onCreate(EventSource.empty());
        contextMockedStatic.verify(() -> ContextManager.currentContext(true, null), Mockito.times(0));
        EventProcessor.onRequest();
        Thread.sleep(1000);
        contextMockedStatic.verify(() -> ContextManager.remove(), Mockito.times(1));
    }

    @Test
    @Order(3)
    void testInitClass() throws Exception {
        Method initClassMethod = EventProcessor.class.getDeclaredMethod("initClass", ClassLoader.class);
        initClassMethod.setAccessible(true);
        // disPlay
        initClassMethod.invoke(null, Thread.currentThread().getContextClassLoader());
        mockedStatic.verify(() -> LogManager.buildTitle("init.class"), Mockito.never());
        // not display but no initClass
        configBuilder.addProperty(ConfigConstants.DISABLE_REPLAY, "true");
        configBuilder.build();
        initClassMethod.invoke(null, Thread.currentThread().getContextClassLoader());
        mockedStatic.verify(() -> LogManager.buildTitle("init.class"), Mockito.never());
        // no display and initClass
        configBuilder.addProperty(ConfigConstants.DISABLE_REPLAY, "false");
        configBuilder.addProperty(ConfigConstants.AREX_STATIC_CLASS_INIT, "test1,io.arex.inst.runtime.listener.EventProcessorTest");
        configBuilder.build();
        initClassMethod.invoke(null, Thread.currentThread().getContextClassLoader());
        mockedStatic.verify(() -> LogManager.buildTitle("init.class"), Mockito.times(1));
    }

    @Test
    void onExit() {
        Assertions.assertDoesNotThrow(EventProcessor::onExit);
    }

    public static class TestJacksonSerializable implements StringSerializable {
        private final ObjectMapper MAPPER = new ObjectMapper();
        @Override
        public String name() {
            return "jackson";
        }

        @Override
        public String serialize(Object object) throws JsonProcessingException {
            return MAPPER.writeValueAsString(object);
        }

        @Override
        public <T> T deserialize(String json, Class<T> clazz) throws JsonProcessingException {
            if (StringUtil.isEmpty(json) || clazz == null) {
                return null;
            }

            return MAPPER.readValue(json, clazz);
        }

        @Override
        public <T> T deserialize(String json, Type type) throws JsonProcessingException {
            if (StringUtil.isEmpty(json) || type == null) {
                return null;
            }

            JavaType javaType = MAPPER.getTypeFactory().constructType(type);
            return MAPPER.readValue(json, javaType);
        }

        @Override
        public StringSerializable reCreateSerializer() {
            return new TestJacksonSerializable();
        }
    }

    public static class TestGsonSerializer implements StringSerializable {
        private final Gson serializer = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
        @Override
        public String name() {
            return "gson";
        }

        @Override
        public String serialize(Object object) {
            if (object == null) {
                return null;
            }
            return serializer.toJson(object);
        }

        @Override
        public <T> T deserialize(String json, Class<T> clazz) {
            if (StringUtil.isEmpty(json) || clazz == null) {
                return null;
            }
            return serializer.fromJson(json, clazz);
        }

        @Override
        public <T> T deserialize(String json, Type type) {
            if (StringUtil.isEmpty(json) || type == null) {
                return null;
            }

            return serializer.fromJson(json, type);
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
}

package io.arex.inst.common.util;

import static io.arex.inst.common.util.FluxReplayUtil.FLUX_FROM_ARRAY;
import static io.arex.inst.common.util.FluxReplayUtil.FLUX_FROM_ITERATOR;
import static io.arex.inst.common.util.FluxReplayUtil.FLUX_FROM_STREAM;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.common.util.FluxReplayUtil.FluxElementResult;
import io.arex.inst.common.util.FluxReplayUtil.FluxResult;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.util.TypeUtil;
import io.lettuce.core.KeyValue;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxReplayUtilTest {
    @BeforeAll
    static void setUp() {
        Serializer.builder(new TestJacksonSerializable()).build();
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }
    @Test
    void restore() {
        // flux is empty
        Flux<?> result = FluxReplayUtil.restore(null);
        StepVerifier.create(result)
            .expectComplete()
            .verify();

        List<FluxElementResult> list = new ArrayList<>();
        FluxResult fluxResult = new FluxResult(null, list);
        result = FluxReplayUtil.restore(fluxResult);
        StepVerifier.create(result)
            .expectComplete()
            .verify();

        // flux is not empty
        FluxElementResult fluxElement1 = new FluxElementResult(1, "1", "java.lang.Integer");
        FluxElementResult fluxException1 = new FluxElementResult(2, null, "java.lang.RuntimeException");
        list.add(fluxElement1);
        list.add(fluxException1);

        // Flux.just()
        fluxResult = new FluxResult(null, list);
        result = FluxReplayUtil.restore(fluxResult);
        assertEquals(TypeUtil.getName(result),"reactor.core.publisher.FluxIterable-");
        StepVerifier.create(result)
            .expectNextMatches(item-> item.equals(1))
            .expectError(NullPointerException.class)
            .verify();

        // Flux.fromIterable()
        fluxResult = new FluxResult(FLUX_FROM_ITERATOR, list);
        result = FluxReplayUtil.restore(fluxResult);
        assertEquals(TypeUtil.getName(result),FLUX_FROM_ITERATOR);
        StepVerifier.create(result)
            .expectNextMatches(item-> item.equals(1))
            .expectError(NullPointerException.class)
            .verify();

        // Flux.fromArray()
        fluxResult = new FluxResult(FLUX_FROM_ARRAY, list);
        result = FluxReplayUtil.restore(fluxResult);
        assertEquals(TypeUtil.getName(result),FLUX_FROM_ARRAY);
        StepVerifier.create(result)
            .expectNextMatches(item-> item.equals(1))
            .expectError(NullPointerException.class)
            .verify();

        // Flux.fromStream()
        fluxResult = new FluxResult(FLUX_FROM_STREAM, list);
        result = FluxReplayUtil.restore(fluxResult);
        assertEquals(TypeUtil.getName(result),FLUX_FROM_STREAM);
        StepVerifier.create(result)
            .expectNextMatches(item-> item.equals(1))
            .expectError(NullPointerException.class)
            .verify();
    }

    @Test
    void testReactiveMGet() {
        List<FluxElementResult> fluxElementResults = new ArrayList<>(2);
        FluxElementResult elementResult1 = new FluxElementResult(1, "{\"key\":\"mget-key1\",\"value\":\"mget-value1-2024-04-02 16:37\"}", "io.lettuce.core.KeyValue-java.lang.String,java.lang.String");
        FluxElementResult elementResult2 = new FluxElementResult(1, "{\"key\":\"mget-key2\",\"value\":\"mget-value2-2024-04-02 16:37\"}", "io.lettuce.core.KeyValue-java.lang.String,java.lang.String");
        fluxElementResults.add(elementResult1);
        fluxElementResults.add(elementResult2);
        FluxResult fluxResult = new FluxResult("reactor.core.publisher.FluxSource-", fluxElementResults);
        Flux<KeyValue<String, String>> restore = FluxReplayUtil.restore(fluxResult);
        StepVerifier.create(restore)
            .expectNextMatches(item-> item.getKey().equals("mget-key1") && item.getValue().equals("mget-value1-2024-04-02 16:37"))
            .expectNextMatches(item-> item.getKey().equals("mget-key2") && item.getValue().equals("mget-value2-2024-04-02 16:37"))
            .expectComplete().verify();
    }

    public static class TestJacksonSerializable implements StringSerializable {
        private final ObjectMapper MAPPER = new ObjectMapper();

        @Override
        public boolean isDefault() {
            return true;
        }

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
}

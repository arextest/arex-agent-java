package io.arex.inst.spring.data.redis;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.google.common.collect.Lists;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.spring.data.redis.RedisTemplateInstrumentation.ExecuteAdvice;
import io.arex.inst.spring.data.redis.RedisTemplateInstrumentation.OneKeyAdvice;
import io.arex.inst.spring.data.redis.RedisTemplateInstrumentation.SortAdvice;
import io.arex.inst.spring.data.redis.RedisTemplateInstrumentation.TwoKeysAdvice;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@ExtendWith(MockitoExtension.class)
class RedisTemplateInstrumentationTest {

    private static RedisTemplateInstrumentation target;
    private static RedisTemplate redisTemplate = new RedisTemplate();
    private static final String KEY = "key";
    private static final String RESTULT = "result";
    private static final String OTHER_KEY = "otherKey";

    @BeforeAll
    static void setUp() {
        redisTemplate = Mockito.mock(RedisTemplate.class);
        target = new RedisTemplateInstrumentation();
    }


    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void methodAdvices() {
        assertEquals(5, target.methodAdvices().size());
    }

    @Test
    void typeMatcher() {
        assertNotNull(target.typeMatcher());
    }

    @Test
    void oneKeyAdvice() {
        // onEnter
        assertFalse(OneKeyAdvice.onEnter(redisTemplate, "getExpire", KEY, MockResult.success(true, "mock")));
        // test methodOnExit at different mockReuslts
        getMockResults().forEach(mockResult -> {
            assertDoesNotThrow(() -> OneKeyAdvice.onExit(redisTemplate, "getExpire", KEY, RESTULT, null, mockResult));
            }
        );
    }

    @Test
    void twoKeysAdvice() {
        // onEnter
        assertFalse(
            TwoKeysAdvice.onEnter(redisTemplate, "getExpire", KEY, OTHER_KEY, MockResult.success(true, "mock")));
        // test methodOnExit at different mockReuslts
        getMockResults().forEach(mockResult -> {
            assertDoesNotThrow(
                () -> TwoKeysAdvice.onExit(redisTemplate, "renameIfAbsent", KEY, OTHER_KEY, RESTULT, null, mockResult));
            }
        );
    }

    @Test
    void ExecuteAdvice() {
        RedisScript<Boolean> lockScript = new DefaultRedisScript<>("LOCK_SCRIPT", Boolean.class);
        Object[] params = new Object[]{lockScript, new ArrayList<>()};
        Object[] paramsWithSerializer = new Object[]{lockScript, null, null, new ArrayList<>(), "val"};

        // onEnter with different arguments
        assertFalse(ExecuteAdvice.onEnter(redisTemplate, "execute", params, MockResult.success(true, "mock")));
        assertFalse(
            ExecuteAdvice.onEnter(redisTemplate, "execute", paramsWithSerializer, MockResult.success(true, "mock")));
        // onExit with different arguments and different mockReuslts
        getMockResults().forEach(mockResult -> {
            assertDoesNotThrow(
                () -> ExecuteAdvice.onExit(redisTemplate, "execute", params, RESTULT, null, mockResult));
            assertDoesNotThrow(
                () -> ExecuteAdvice.onExit(redisTemplate, "execute", paramsWithSerializer, RESTULT, null, mockResult));
        });
    }

    @Test
    void SortAdvice(){
        // onEnter
        assertFalse(SortAdvice.onEnter(redisTemplate, "sort", null, MockResult.success(true, "mock")));
        // onExit with different mockReuslts
        getMockResults().forEach(mockResult -> {
            assertDoesNotThrow(() -> SortAdvice.onExit(redisTemplate, "sort", null, RESTULT, null, mockResult));
            }
        );
    }

    private static List<MockResult> getMockResults() {
        return Lists.newArrayList(
            MockResult.success(true, "mock"),
            MockResult.success(false, "mock"),
            MockResult.success(new Throwable()),
            null);
    }
}

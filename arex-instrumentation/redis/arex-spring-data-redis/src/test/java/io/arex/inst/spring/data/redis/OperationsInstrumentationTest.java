package io.arex.inst.spring.data.redis;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.google.common.collect.Lists;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.spring.data.redis.OperationsInstrumentation.OneKeyAdvice;
import io.arex.inst.spring.data.redis.OperationsInstrumentation.TwoKeysAdvice;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class OperationsInstrumentationTest {

    private static OperationsInstrumentation target;
    private static RedisTemplate redisTemplate = new RedisTemplate();
    private static final String KEY = "key";
    private static final String RESTULT = "result";
    private static final String OTHER_KEY = "otherKey";
    private static final String METHOD_NAME = "get";

    @BeforeAll
    static void setUp() {
        redisTemplate = Mockito.mock(RedisTemplate.class);
        target = new OperationsInstrumentation();
    }


    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void methodAdvices() {
        assertEquals(6, target.methodAdvices().size());
    }

    @Test
    void typeMatcher() {
        assertNotNull(target.typeMatcher());
    }

    @Test
    void oneKeyAdvice() {
        // onEnter
        assertFalse(OneKeyAdvice.onEnter(redisTemplate, METHOD_NAME, KEY, MockResult.success(true, "mock")));
        // test methodOnExit at different mockReuslts
        getMockResults().forEach(mockResult -> {
                assertDoesNotThrow(() -> OneKeyAdvice.onExit(redisTemplate, METHOD_NAME, KEY, RESTULT, null, mockResult));
            }
        );
    }

    @Test
    void twoKeysAdvice() {
        // onEnter
        assertFalse(
            TwoKeysAdvice.onEnter(redisTemplate, METHOD_NAME, KEY, OTHER_KEY, MockResult.success(true, "mock")));
        // test methodOnExit at different mockReuslts
        getMockResults().forEach(mockResult -> {
                assertDoesNotThrow(
                    () -> TwoKeysAdvice.onExit(redisTemplate, METHOD_NAME, KEY, OTHER_KEY, RESTULT, null, mockResult));
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

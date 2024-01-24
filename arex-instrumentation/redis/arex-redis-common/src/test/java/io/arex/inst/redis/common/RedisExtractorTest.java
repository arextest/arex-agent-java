package io.arex.inst.redis.common;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.sizeof.AgentSizeOf;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class RedisExtractorTest {

    static RedisExtractor target;
    static AgentSizeOf agentSizeOf;

    @BeforeAll
    static void setUp() {
        agentSizeOf = Mockito.mock(AgentSizeOf.class);
        Mockito.mockStatic(AgentSizeOf.class);
        Mockito.when(AgentSizeOf.newInstance(any())).thenReturn(agentSizeOf);
        target = new RedisExtractor("", "", "", "");
    }

    @AfterAll
    static void tearDown() {
        target = null;
        agentSizeOf = null;
        Mockito.clearAllCaches();
    }

    @Test
    void record() {
        try (MockedStatic<MockUtils> mockService = mockStatic(MockUtils.class)) {
            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            mockService.when(() -> MockUtils.createRedis(any())).thenReturn(mocker);
            mockService.when(() -> MockUtils.recordMocker(any())).then((Answer<Void>) invocationOnMock -> {
                System.out.println("mock MockService.recordMocker");
                return null;
            });

            Mockito.when(agentSizeOf.checkMemorySizeLimit(any(), any(long.class))).thenReturn(true);
            assertDoesNotThrow(() -> target.record(new Object()));

            // test exceed memory size
            Mockito.when(agentSizeOf.checkMemorySizeLimit(any(), any(long.class))).thenReturn(false);
            assertDoesNotThrow(() -> target.record(new Object()));
        }
    }

    @Test
    void replay() {
        try (MockedStatic<MockUtils> mockService = mockStatic(MockUtils.class);
            MockedStatic<IgnoreUtils> ignoreService = mockStatic(IgnoreUtils.class)) {

            ignoreService.when(() -> IgnoreUtils.ignoreMockResult(any(), any())).thenReturn(true);

            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            mockService.when(() -> MockUtils.createRedis(any())).thenReturn(mocker);
            mockService.when(() -> MockUtils.replayBody(any())).thenReturn(mocker);

            assertNotNull(target.replay());
        }
    }

    @Test
    void redisMultiKey() {
        RedisExtractor.RedisMultiKey redisMultiKey = new RedisExtractor.RedisMultiKey();
        redisMultiKey.setKey("mock");
        redisMultiKey.getKey();
        redisMultiKey.setField("mock");
        assertNotNull(redisMultiKey.getField());
    }
}
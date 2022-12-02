package io.arex.inst.redis.common;

import io.arex.foundation.model.RedisMocker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisExtractorTest {
    static RedisExtractor target;
    RedisMocker redisMocker;

    @BeforeAll
    static void setUp() {
        target = new RedisExtractor("", "", "", "");
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void record() {
        try (MockedConstruction<RedisMocker> mocked = Mockito.mockConstruction(RedisMocker.class, (mock, context) -> {
            redisMocker = mock;
        })) {
            target.record(new Object());
            verify(redisMocker).record();
        }
    }

    @Test
    void testRecord() {
        try (MockedConstruction<RedisMocker> mocked = Mockito.mockConstruction(RedisMocker.class, (mock, context) -> {
            redisMocker = mock;
        })) {
            target.record(new NullPointerException());
            verify(redisMocker).record();
        }
    }

    @Test
    void replay() {
        try (MockedConstruction<RedisMocker> mocked = Mockito.mockConstruction(RedisMocker.class)) {
            assertNotNull(target.replay());
        }
    }
}
package io.arex.inst.redis.common;

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
        target.record(new Object());
    }

    @Test
    void testRecord() {
        target.record(new NullPointerException());
    }

    @Test
    void replay() {
        assertNotNull(target.replay());
    }
}
package io.arex.inst.redisson.v3;

import io.arex.inst.redisson.v3.common.RedissonHelper;
import io.arex.inst.redisson.v3.wrapper.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.Redisson;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedissonInstrumentationTest {
    static RedissonInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new RedissonInstrumentation();
        Mockito.mockConstruction(RedissonBucketWrapper.class);
        Mockito.mockConstruction(RedissonBucketsWrapper.class);
        Mockito.mockConstruction(RedissonKeysWrapper.class);
        Mockito.mockConstruction(RedissonListWrapper.class);
        Mockito.mockConstruction(RedissonSetWrapper.class);
        Mockito.mockConstruction(RedissonMapWrapper.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertNotNull(target.typeMatcher());
    }

    @Test
    void methodAdvices() {
        assertNotNull(target.methodAdvices());
    }

    @Test
    void onEnter() {
        assertTrue(RedissonInstrumentation.GetBucketAdvice.onEnter());
    }

    @Test
    void onExit() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetBucketAdvice.onExit(
                null, null, null));
    }

    @Test
    void onEnterGetBucketWithCodecAdvice() {
        assertTrue(RedissonInstrumentation.GetBucketWithCodecAdvice.onEnter());
    }

    @Test
    void onExitGetBucketWithCodecAdvice() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetBucketWithCodecAdvice.onExit(
                null, null, null, null));
    }

    @Test
    void onEnterGetBucketsAdvice() {
        assertTrue(RedissonInstrumentation.GetBucketsAdvice.onEnter());
    }

    @Test
    void onExitGetBucketsAdvice() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetBucketsAdvice.onExit(
                null, null));
    }

    @Test
    void onEnterGetBucketsWithCodecAdvice() {
        assertTrue(RedissonInstrumentation.GetBucketsWithCodecAdvice.onEnter());
    }

    @Test
    void onExitGetBucketsWithCodecAdvice() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetBucketsWithCodecAdvice.onExit(
                null, null, null));
    }

    @Test
    void onEnterGetKeysAdvice() {
        assertTrue(RedissonInstrumentation.GetKeysAdvice.onEnter());
    }

    @Test
    void onExitGetKeysAdvice() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetKeysAdvice.onExit(
                null, null));
    }

    @Test
    void onEnterGetListAdvice() {
        assertTrue(RedissonInstrumentation.GetListAdvice.onEnter());
    }

    @Test
    void onExitGetListAdvice() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetListAdvice.onExit(
            null, null, null));
    }

    @Test
    void onEnterGetListWithCodecAdvice() {
        assertTrue(RedissonInstrumentation.GetListWithCodecAdvice.onEnter());
    }

    @Test
    void onExitGetListWithCodecAdvice() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetListWithCodecAdvice.onExit(
            null, null, null, null));
    }

    @Test
    void onEnterGetSetAdvice() {
        assertTrue(RedissonInstrumentation.GetSetAdvice.onEnter());
    }

    @Test
    void onExitGetSetAdvice() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetSetAdvice.onExit(
            null, null, null));
    }

    @Test
    void onEnterGetSetWithCodecAdvice() {
        assertTrue(RedissonInstrumentation.GetSetWithCodecAdvice.onEnter());
    }

    @Test
    void onExitGetSetWithCodecAdvice() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetSetWithCodecAdvice.onExit(
            null, null, null, null));
    }

    @Test
    void onEnterGetMapAdvice() {
        assertTrue(RedissonInstrumentation.GetMapAdvice.onEnter());
    }

    @Test
    void onExitGetMapAdvice() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetMapAdvice.onExit(
            null, null, null));
    }

    @Test
    void onEnterGetMapWithOptionsAdvice() {
        assertTrue(RedissonInstrumentation.GetMapWithOptionsAdvice.onEnter());
    }

    @Test
    void onExitGetMapWithOptionsAdvice() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetMapWithOptionsAdvice.onExit(
            null, null, null, null, null));
    }

    @Test
    void onEnterGetMapWithCodecAdvice() {
        assertTrue(RedissonInstrumentation.GetMapWithCodecAdvice.onEnter());
    }

    @Test
    void onExitGetMapWithCodecAdvice() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetMapWithCodecAdvice.onExit(
            null, null, null, null));
    }

    @Test
    void onEnterGetMapWithCodecOptionsAdvice() {
        assertTrue(RedissonInstrumentation.GetMapWithCodecOptionsAdvice.onEnter());
    }

    @Test
    void onExitGetMapWithCodecOptionsAdvice() {
        assertDoesNotThrow(() -> RedissonInstrumentation.GetMapWithCodecOptionsAdvice.onExit(
            null, null, null, null, null, null));
    }

}
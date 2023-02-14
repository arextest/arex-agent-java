package io.arex.inst.runtime.context;

import io.arex.inst.runtime.config.ConfigBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecordLimiterTest {

    @Test
    void acquire() {
        RecordLimiter.init(mock -> true);
        ConfigBuilder.create("mock").enableDebug(true).recordRate(1).build();
        assertTrue(RecordLimiter.acquire("mock"));
    }
}
package io.arex.inst.config.apollo;

import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ApolloRemoteConfigRepositoryInstrumentationTest {
    static ApolloRemoteConfigRepositoryInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new ApolloRemoteConfigRepositoryInstrumentation();
    }

    @AfterAll
    static void tearDown() {
        target = null;
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
        AtomicReference<ApolloConfig> configCache = new AtomicReference<>();
        assertDoesNotThrow(() -> ApolloRemoteConfigRepositoryInstrumentation.LoadAdvice.onEnter(configCache, null, null));
    }

    @Test
    void onExit() {
        assertDoesNotThrow(() -> ApolloRemoteConfigRepositoryInstrumentation.LoadAdvice.onExit(new ApolloConfig(), null));
    }
}
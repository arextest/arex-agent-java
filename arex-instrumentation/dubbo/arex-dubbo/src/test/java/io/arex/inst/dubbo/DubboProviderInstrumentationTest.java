package io.arex.inst.dubbo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DubboProviderInstrumentationTest {
    static DubboProviderInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new DubboProviderInstrumentation();
        Mockito.mockStatic(DubboProviderExtractor.class);
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
    void onEnter() throws SQLException {
        DubboProviderInstrumentation.InvokeAdvice.onEnter(null, null);
    }

    @Test
    void onExit() throws SQLException {
        DubboProviderInstrumentation.InvokeAdvice.onExit(null, null, null);
    }
}
package io.arex.inst.dubbo.apache.v2;

import org.apache.dubbo.rpc.Invocation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class DubboProviderInstrumentationTest {
    static DubboProviderInstrumentation target;
    static MockedStatic<DubboProviderExtractor> extractor;

    @BeforeAll
    static void setUp() {
        target = new DubboProviderInstrumentation();
        extractor = Mockito.mockStatic(DubboProviderExtractor.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        extractor = null;
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
        Invocation invocation = Mockito.mock(Invocation.class);
        DubboProviderInstrumentation.InvokeAdvice.onEnter(null, invocation);
        extractor.verify(() -> DubboProviderExtractor.onServiceEnter(any(), any()));
    }

    @Test
    void onExit() {
        DubboProviderInstrumentation.InvokeAdvice.onExit(null, null, null);
        extractor.verify(() -> DubboProviderExtractor.onServiceExit(any(), any(), any()));
    }
}
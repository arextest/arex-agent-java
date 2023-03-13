package io.arex.inst.httpservlet.inst;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.inst.httpservlet.ServletAdviceHelper;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;

class FilterInstrumentationV3Test {

    FilterInstrumentationV3 inst = new FilterInstrumentationV3();

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ServletAdviceHelper.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertFalse(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(Filter.class)));
    }

    @Test
    void methodAdvices() {
        assertEquals(1, inst.methodAdvices().size());
    }

    @Test
    void adviceClassNames() {
        assertEquals(10, inst.adviceClassNames().size());
    }

    @Test
    void ServiceAdvice_onEnter() {
        Mockito.when(ServletAdviceHelper.onServiceEnter(any(), any(), any())).thenReturn(null);
        assertDoesNotThrow(() -> ServletInstrumentationV3.ServiceAdvice.onEnter(null, null));

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
        Mockito.when(ServletAdviceHelper.onServiceEnter(any(), any(), any())).thenReturn(Pair.of(mockRequest, mockResponse));
        assertDoesNotThrow(() -> FilterInstrumentationV3.FilterAdvice.onEnter(null, null));
    }

    @Test
    void ServiceAdvice_onExit() {
        assertDoesNotThrow(() -> FilterInstrumentationV3.FilterAdvice.onExit(null, null));
    }
}
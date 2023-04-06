package io.arex.inst.httpservlet.inst;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.inst.httpservlet.ServletAdviceHelper;
import io.arex.inst.httpservlet.inst.ServletInstrumentationV5.ServiceAdvice;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ServletInstrumentationV5Test {

    ServletInstrumentationV5 inst = new ServletInstrumentationV5();

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
        assertFalse(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(HttpServlet.class)));
    }

    @Test
    void methodAdvices() {
        assertEquals(1, inst.methodAdvices().size());
    }

    @Test
    void ServiceAdvice_onEnter() {
        Mockito.when(ServletAdviceHelper.onServiceEnter(any(), any(), any())).thenReturn(null);
        assertDoesNotThrow(() -> ServiceAdvice.onEnter(null, null));

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
        Mockito.when(ServletAdviceHelper.onServiceEnter(any(), any(), any())).thenReturn(Pair.of(mockRequest, mockResponse));
        assertDoesNotThrow(() -> ServiceAdvice.onEnter(null, null));
    }

    @Test
    void ServiceAdvice_onExit() {
        assertDoesNotThrow(() -> ServiceAdvice.onExit(null, null));
    }
}

package io.arex.inst.httpservlet.inst;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.inst.httpservlet.ServletAdviceHelper;
import io.arex.inst.httpservlet.inst.ServletInstrumentationV3.ServiceAdvice;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.FrameworkServlet;

class ServletInstrumentationV3Test {

    ServletInstrumentationV3 inst = new ServletInstrumentationV3();

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
        assertTrue(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(FrameworkServlet.class)));
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

package io.arex.inst.httpservlet.inst;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.httpservlet.ServletAdviceHelper;
import io.arex.inst.httpservlet.inst.InvocableHandlerInstrumentationV5.InvokeAdvice;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.method.support.InvocableHandlerMethod;

class InvocableHandlerInstrumentationV5Test {

    InvocableHandlerInstrumentationV5 inst = new InvocableHandlerInstrumentationV5();

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
        assertTrue(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(InvocableHandlerMethod.class)));
    }

    @Test
    void methodAdvices() {
        assertEquals(1, inst.methodAdvices().size());
    }

    @Test
    void InvokeAdvice_onExit() {
        // test in io.arex.inst.httpservlet.ServletAdviceHelperTest#onInvokeForRequestExit
        assertDoesNotThrow(() -> InvokeAdvice.onExit(null, null, null));
    }
}

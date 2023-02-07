package io.arex.inst.httpservlet.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3;
import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ServletAsyncListenerV3Test {
    ServletAsyncListenerV3 listener = new ServletAsyncListenerV3(ServletAdapterImplV3.getInstance());

    static AsyncEvent asyncEvent = null;
    @BeforeAll
    static void setUp() {
        asyncEvent = Mockito.mock(AsyncEvent.class);
        Mockito.when(asyncEvent.getSuppliedRequest()).thenReturn(Mockito.mock(HttpServletRequest.class));
        Mockito.when(asyncEvent.getSuppliedResponse()).thenReturn(Mockito.mock(HttpServletResponse.class));
        Mockito.when(asyncEvent.getAsyncContext()).thenReturn(Mockito.mock(AsyncContext.class));
    }

    @AfterAll
    static void tearDown() {
        asyncEvent = null;
        Mockito.clearAllCaches();
    }

    @Test
    void onComplete() throws IOException {
        assertDoesNotThrow(() -> listener.onComplete(asyncEvent));
    }

    @Test
    void onTimeout() {
        assertDoesNotThrow(() -> listener.onTimeout(asyncEvent));
    }

    @Test
    void onError() {
        assertDoesNotThrow(() -> listener.onError(asyncEvent));
    }

    @Test
    void onStartAsync() {
        assertDoesNotThrow(() -> listener.onStartAsync(asyncEvent));
    }
}

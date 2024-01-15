package io.arex.inst.runtime.request;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.util.ServiceLoader;
import java.util.Arrays;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RequestHandlerManagerTest {
    private static RequestHandler requestHandler = null;
    private static RequestHandler requestHandlerError = null;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ServiceLoader.class);
        requestHandler = Mockito.mock(RequestHandler.class);
        requestHandlerError = Mockito.mock(RequestHandler.class);
        Mockito.when(requestHandler.name()).thenReturn("test");
        Mockito.when(requestHandlerError.name()).thenReturn("testError");
        Mockito.doThrow(new RuntimeException()).when(requestHandlerError).preHandle("request");
        Mockito.doThrow(new RuntimeException()).when(requestHandlerError).handleAfterCreateContext("request");
        Mockito.doThrow(new RuntimeException()).when(requestHandlerError).postHandle("request", "response");
        Mockito.when(ServiceLoader.load(RequestHandler.class)).thenReturn(Arrays.asList(requestHandler, requestHandlerError));
        RequestHandlerManager.init();
    }

    @AfterAll
    static void tearDown() {
        requestHandler = null;
        requestHandlerError = null;
        Mockito.clearAllCaches();
    }

    @Test
    void preHandle() {
        // null handler
        RequestHandlerManager.preHandle(null, "test2");
        Mockito.verify(requestHandler, Mockito.never()).preHandle("test2");
        // normal
        RequestHandlerManager.preHandle("request", "test");
        Mockito.verify(requestHandler, Mockito.times(1)).preHandle("request");
        // error
        assertDoesNotThrow(() -> RequestHandlerManager.preHandle("request", "testError"));

    }

    @Test
    void handleAfterCreateContext() {
        // null handler
        RequestHandlerManager.handleAfterCreateContext(null, "test2");
        Mockito.verify(requestHandler, Mockito.never()).handleAfterCreateContext("test2");
        // normal
        RequestHandlerManager.handleAfterCreateContext("request", "test");
        Mockito.verify(requestHandler, Mockito.times(1)).handleAfterCreateContext("request");
        // error
        assertDoesNotThrow(() -> RequestHandlerManager.handleAfterCreateContext("request", "testError"));

    }

    @Test
    void postHandle() {
        // null handler
        RequestHandlerManager.postHandle(null, "response", "test2");
        Mockito.verify(requestHandler, Mockito.never()).postHandle("test2", "response");
        // normal
        RequestHandlerManager.postHandle("request", "response", "test");
        Mockito.verify(requestHandler, Mockito.times(1)).postHandle("request", "response");
        // error
        assertDoesNotThrow(() -> RequestHandlerManager.postHandle("request", "response", "testError"));
    }
}
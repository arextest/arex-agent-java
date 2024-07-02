package io.arex.inst.httpclient.asynchttpclient.listener;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.httpclient.asynchttpclient.wrapper.ResponseWrapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.asynchttpclient.AsyncHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AsyncHttpClientListenableFutureTest {
    private static AsyncHttpClientListenableFuture future;
    private static AsyncHttpClientListenableFuture futureException;
    private static AsyncHandler handler;
    private static ResponseWrapper responseWrapper;

    @BeforeAll
    static void setUp() throws Exception {
        handler = Mockito.mock(AsyncHandler.class);
        responseWrapper = Mockito.mock(ResponseWrapper.class);
        Mockito.when(responseWrapper.getUri()).thenReturn("http://test");
        Mockito.when(responseWrapper.getProtocolMajorVersion()).thenReturn(1);
        Mockito.when(responseWrapper.getProtocolMinorVersion()).thenReturn(1);
        Mockito.when(responseWrapper.getProtocolName()).thenReturn("http");
        Mockito.when(responseWrapper.getProtocolText()).thenReturn("http");
        Mockito.when(responseWrapper.getStatusCode()).thenReturn(200);
        Mockito.when(responseWrapper.getStatusText()).thenReturn("OK");
        Mockito.when(responseWrapper.getLocalAddress()).thenReturn("localAddress");
        Mockito.when(responseWrapper.getRemoteAddress()).thenReturn("remoteAddress");

        Mockito.when(handler.onCompleted()).thenReturn(responseWrapper);

        future = new AsyncHttpClientListenableFuture(responseWrapper, null, handler);
        futureException = new AsyncHttpClientListenableFuture(null, new RuntimeException(), handler);
    }

    @AfterAll
    static void tearDown() {
        future = null;
        futureException = null;
        handler = null;
        responseWrapper = null;
        Mockito.clearAllCaches();
    }

    @Order(1)
    @Test
    void done() throws Exception {
        // beforeAll will call onCompleted once
        Mockito.verify(handler, Mockito.times(1)).onCompleted();
        // with exception
        Mockito.verify(handler, Mockito.times(1)).onThrowable(Mockito.any(RuntimeException.class));

        // responseWrapper null bodyPart hava header
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("content-type", Arrays.asList("application/json"));
        headers.put("content-length", Arrays.asList("100"));
        headers.put("connection", Arrays.asList("keep-alive"));
        Mockito.when(responseWrapper.getHeaders()).thenReturn(headers);
        future.done();
        Mockito.verify(handler, Mockito.times(2)).onCompleted();

        // responseWrapper null header hava bodyPart
        Mockito.when(responseWrapper.getHeaders()).thenReturn(null);
        Mockito.when(responseWrapper.getContent()).thenReturn("test".getBytes());
        future.done();
        Mockito.verify(handler, Mockito.times(3)).onCompleted();
    }

    @Test
    void abort() {
        final RuntimeException runtimeException = new RuntimeException();
        future.abort(runtimeException);
        Mockito.verify(handler, Mockito.times(1)).onThrowable(runtimeException);
    }

    @Test
    void touch() {
        future.touch();
    }

    @Test
    void addListener() {
        future.addListener(() -> System.out.println("test"), null);
        future.addListener(() -> System.out.println("test"), Mockito.mock(Executor.class));
    }

    @Test
    void toCompletableFuture() throws Exception {
        assertEquals(future.toCompletableFuture().get(), responseWrapper);
    }

    @Test
    void cancel() {
        assertFalse(future.cancel(true));
    }

    @Test
    void isCancelled() {
        assertFalse(future.isCancelled());
    }

    @Test
    void isDone() {
        assertTrue(future.isDone());
    }

    @Test
    void get() throws Exception {
        assertEquals(future.get(), responseWrapper);
    }

    @Test
    void testGet() throws Exception {
        assertEquals(future.get(1, TimeUnit.MINUTES), responseWrapper);
    }
}
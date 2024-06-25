package io.arex.inst.httpclient.ning;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class ResponseFutureWrapperTest {
    @Test
    void done() {
        ResponseFutureWrapper responseFutureWrapper = new ResponseFutureWrapper("Test");
        responseFutureWrapper.done();
        assertTrue(responseFutureWrapper.isDone());
    }

    @Test
    void abort() {
        ResponseFutureWrapper responseFutureWrapper = new ResponseFutureWrapper("Test");
        responseFutureWrapper.abort(new Throwable());
        assertTrue(responseFutureWrapper.isDone());
    }

    @Test
    void cancel() {
        ResponseFutureWrapper responseFutureWrapper = new ResponseFutureWrapper("Test");
        assertTrue(responseFutureWrapper.cancel(true));
    }

    @Test
    void isCancelled() {
        ResponseFutureWrapper responseFutureWrapper = new ResponseFutureWrapper("Test");
        assertFalse(responseFutureWrapper.isCancelled());
    }

    @Test
    void isDone() {
        ResponseFutureWrapper responseFutureWrapper = new ResponseFutureWrapper("Test");
        assertTrue(responseFutureWrapper.isDone());
    }

    @Test
    void get() throws ExecutionException, InterruptedException {
        ResponseFutureWrapper responseFutureWrapper = new ResponseFutureWrapper("Test");
        assertEquals("Test", responseFutureWrapper.get());
    }

    @Test
    void getWithTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        ResponseFutureWrapper responseFutureWrapper = new ResponseFutureWrapper("Test");
        assertEquals("Test", responseFutureWrapper.get(1, java.util.concurrent.TimeUnit.SECONDS));
    }

}

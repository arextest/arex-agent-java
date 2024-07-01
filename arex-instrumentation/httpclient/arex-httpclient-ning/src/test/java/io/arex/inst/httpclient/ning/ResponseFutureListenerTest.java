package io.arex.inst.httpclient.ning;

import com.ning.http.client.ListenableFuture;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class ResponseFutureListenerTest {

    @Test
    void run() throws ExecutionException, InterruptedException, TimeoutException {
        HttpClientExtractor extractor = mock(HttpClientExtractor.class);
        ListenableFuture responseFuture = mock(ListenableFuture.class);

        when(responseFuture.get(1, TimeUnit.SECONDS)).thenReturn("Test");
        ResponseFutureListener responseFutureListener = new ResponseFutureListener(extractor, responseFuture);
        responseFutureListener.run();

        verify(extractor, times(1)).record("Test");
        // exception
        when(responseFuture.get(1, TimeUnit.SECONDS)).thenThrow(new InterruptedException());
        assertDoesNotThrow(responseFutureListener::run);
    }
}

package io.arex.inst.httpclient.asynchttpclient.listener;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.httpclient.asynchttpclient.AsyncHttpClientExtractor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AsyncHttpClientConsumerTest {
    private static AsyncHttpClientExtractor extractor;
    private static AsyncHttpClientConsumer consumer;

    @BeforeAll
    static void setUp() {
        extractor = Mockito.mock(AsyncHttpClientExtractor.class);
        consumer = new AsyncHttpClientConsumer(extractor);
    }

    @Test
    void accept() {
        // exception
        final RuntimeException runtimeException = new RuntimeException();
        consumer.accept(null, runtimeException);
        Mockito.verify(extractor, Mockito.times(1)).record(runtimeException);
        // no exception
        String o = "test";
        consumer.accept(o, null);
        Mockito.verify(extractor, Mockito.times(1)).record();
    }
}
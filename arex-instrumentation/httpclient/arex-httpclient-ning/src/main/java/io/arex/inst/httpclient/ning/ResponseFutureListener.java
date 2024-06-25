package io.arex.inst.httpclient.ning;

import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.runtime.log.LogManager;

import java.util.concurrent.TimeUnit;


public class ResponseFutureListener implements Runnable {
    private final ListenableFuture<?> responseFuture;
    private final HttpClientExtractor<Request, Object> extractor;
    private final TraceTransmitter transmitter;
    public ResponseFutureListener(HttpClientExtractor<Request, Object> extractor, ListenableFuture<?> responseFuture) {
        this.responseFuture = responseFuture;
        this.extractor = extractor;
        this.transmitter = TraceTransmitter.create();
    }

    /**
     * responseFuture is com.ning.http.client.ListenableFuture,
     * only Future.get() to get the response.
     * only when the responseFuture is done, run will be called, record response.
     */
    @Override
    public void run() {
        try (TraceTransmitter transmit = transmitter.transmit()){
            extractor.record(responseFuture.get(1, TimeUnit.SECONDS));
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            LogManager.warn("ResponseFutureListener.run", interruptedException);
        } catch (Exception exception) {
            LogManager.warn("ResponseFutureListener.run", exception);
        }
    }
}

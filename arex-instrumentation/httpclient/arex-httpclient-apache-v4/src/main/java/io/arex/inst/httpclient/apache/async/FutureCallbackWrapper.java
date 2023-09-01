package io.arex.inst.httpclient.apache.async;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import java.util.concurrent.Future;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FutureCallbackWrapper<T> implements FutureCallback<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FutureCallbackWrapper.class);
    private final FutureCallback<T> delegate;
    private final TraceTransmitter traceTransmitter;

    // Maybe null, Just to pass the trace
    private final HttpClientExtractor<HttpRequest, HttpResponse> extractor;

    public FutureCallbackWrapper(FutureCallback<T> delegate) {
        this(null, delegate);
    }

    public FutureCallbackWrapper(HttpClientExtractor<HttpRequest, HttpResponse> extractor, FutureCallback<T> delegate) {
        this.traceTransmitter = TraceTransmitter.create();
        this.delegate = delegate;
        this.extractor = extractor;
    }

    @Override
    public void completed(T t) {
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
            if (extractor != null && t instanceof HttpResponse) {
                HttpResponse response = (HttpResponse) t;
                extractor.record(response);
            }
            if (delegate != null) {
                delegate.completed(t);
            }
        }
    }

    @Override
    public void failed(Exception e) {
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
            if (extractor != null) {
                extractor.record(e);
            }
            if (delegate != null) {
                delegate.failed(e);
            }
        }
    }

    @Override
    public void cancelled() {
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
            if (delegate != null) {
                delegate.cancelled();
            }
        }
    }

    public MockResult replay() {
        return extractor.replay();
    }

    public Future<T> replay(MockResult mockResult) {
        BasicFuture<T> basicFuture = new BasicFuture<>(this.delegate);
        if (mockResult.getThrowable() != null) {
            basicFuture.failed((Exception) mockResult.getThrowable());
        } else{
            basicFuture.completed((T) mockResult.getResult());
        }
        return basicFuture;
    }

    public static <T> FutureCallbackWrapper<T> get(HttpAsyncRequestProducer requestProducer, FutureCallback<T> delegate) {
        if (delegate instanceof FutureCallbackWrapper) {
            return ((FutureCallbackWrapper<T>) delegate);
        }
        ApacheHttpClientAdapter adapter;
        HttpClientExtractor<HttpRequest, HttpResponse> extractor;

        try {
            adapter = new ApacheHttpClientAdapter(requestProducer.generateRequest());
            if (adapter.skipRemoteStorageRequest()) {
                return null;
            }
            extractor = new HttpClientExtractor<>(adapter);
        } catch (Throwable ex) {
            LOGGER.warn("create async wrapper error:{}, record or replay was skipped", ex.getMessage(), ex);
            return null;
        }
        return new FutureCallbackWrapper<>(extractor, delegate);
    }

    public static <T> FutureCallback<T> wrap(FutureCallback<T> delegate) {
        return new FutureCallbackWrapper<>(delegate);
    }
}
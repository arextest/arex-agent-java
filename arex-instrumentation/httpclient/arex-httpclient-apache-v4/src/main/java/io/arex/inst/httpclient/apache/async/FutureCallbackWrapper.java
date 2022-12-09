package io.arex.inst.httpclient.apache.async;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter;
import io.arex.inst.httpclient.common.ArexDataException;
import io.arex.inst.httpclient.common.ExceptionWrapper;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FutureCallbackWrapper<T> implements FutureCallback<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FutureCallbackWrapper.class);
    private final FutureCallback<T> delegate;
    private final TraceTransmitter traceTransmitter;

    private final HttpClientExtractor<HttpRequest, MockResult> extractor;

    public FutureCallbackWrapper(HttpClientExtractor<HttpRequest, MockResult> extractor, FutureCallback<T> delegate) {
        this.traceTransmitter = TraceTransmitter.create();
        this.delegate = delegate;
        this.extractor = extractor;
    }

    @Override
    public void completed(T t) {
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
            if (t instanceof HttpResponse) {
                HttpResponse response = (HttpResponse) t;
                recordResponse(response);
            }
            delegate.completed(t);
        }
    }

    @Override
    public void failed(Exception e) {
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
            recordException(e);
            delegate.failed(e);
        }
    }

    @Override
    public void cancelled() {
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
            recordException(null);
            delegate.cancelled();
        }
    }

    public boolean replay() {
        try {
            return mockResult(extractor.fetchMockResult());
        } catch (Exception ex) {
            delegate.failed(new ArexDataException("mock data failed.", ex));
        }
        return true;
    }

    private boolean mockResult(HttpResponseWrapper wrapped) {
        if (wrapped == null) {
            delegate.failed(new ArexDataException("mock data failed."));
            return true;
        }
        boolean notIgnoreMockResult = !wrapped.isIgnoreMockResult();
        if (notIgnoreMockResult) {
            ExceptionWrapper exception = wrapped.getException();
            if (exception != null) {
                if (exception.isCancelled()) {
                    delegate.cancelled();
                } else {
                    delegate.failed(exception.getOriginalException());
                }
                return true;
            }
            // noinspection unchecked
            MockResult mockResult = extractor.replay(wrapped);
            delegate.completed((T) mockResult.getResult());
        }
        return notIgnoreMockResult;
    }

    private void recordResponse(HttpResponse response) {
        try {
            if (extractor != null && RepeatedCollectManager.exitAndValidate()) {
                extractor.record(MockResult.success(response));
            }
        } catch (Exception ex) {
            LOGGER.warn("consume response content failed:{}", ex.getMessage(), ex);
        }
    }

    private void recordException(Exception exception) {
        try {
            if (extractor != null && RepeatedCollectManager.exitAndValidate()) {
                extractor.record(exception);
            }
        } catch (Exception ex) {
            LOGGER.warn("consume response content failed:{}", ex.getMessage(), ex);
        }
    }

    public static <T> FutureCallbackWrapper<T> get(HttpAsyncRequestProducer requestProducer, FutureCallback<T> delegate) {
        if (delegate instanceof FutureCallbackWrapper) {
            return ((FutureCallbackWrapper<T>) delegate);
        }
        ApacheHttpClientAdapter adapter;
        HttpClientExtractor<HttpRequest, MockResult> extractor;

        try {
            adapter = new ApacheHttpClientAdapter(requestProducer.generateRequest());
            if (adapter.skipRemoteStorageRequest()) {
                return null;
            }
            extractor = new HttpClientExtractor<>(adapter);
        } catch (Exception ex) {
            LOGGER.warn("create async wrapper error:{}, record or replay was skipped", ex.getMessage(), ex);
            return null;
        }
        return new FutureCallbackWrapper<>(extractor, delegate);
    }
}
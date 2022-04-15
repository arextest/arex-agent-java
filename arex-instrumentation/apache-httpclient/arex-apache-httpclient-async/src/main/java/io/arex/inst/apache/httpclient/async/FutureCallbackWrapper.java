package io.arex.inst.apache.httpclient.async;

import io.arex.foundation.util.LogUtil;
import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.inst.apache.httpclient.common.ApacheClientExtractor;
import io.arex.inst.apache.httpclient.common.ArexDataException;
import io.arex.inst.apache.httpclient.common.ExceptionWrapper;
import io.arex.inst.apache.httpclient.common.HttpResponseWrapper;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;

public class FutureCallbackWrapper<T> implements FutureCallback<T> {
    private final FutureCallback<T> delegate;
    private final TraceTransmitter traceTransmitter;
    private String target;
    private ApacheClientExtractor extractor;

    public FutureCallbackWrapper(ApacheClientExtractor extractor, FutureCallback<T> delegate) {
        this.traceTransmitter = TraceTransmitter.create();
        this.delegate = delegate;
        this.extractor = extractor;
    }

    public boolean isMockEnabled() {
        return extractor.isMockEnabled();
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

    public void replay() {
        try {
            mockResult(extractor.mock());
        } catch (Exception ex) {
            delegate.failed(new ArexDataException("mock data failed.", ex));
            return;
        }
    }

    private void mockResult(HttpResponseWrapper wrapped) {
        if (wrapped == null) {
            delegate.failed(new ArexDataException("mock data failed."));
            return;
        }

        ExceptionWrapper exception = wrapped.getException();
        if (exception != null) {
            if (exception.isCancelled()) {
                delegate.cancelled();
            } else {
                delegate.failed(exception.getOriginalException());
            }
        } else {
            delegate.completed((T) HttpResponseWrapper.to(wrapped));
        }
    }

    private void recordResponse(HttpResponse response) {
        try {
            if (extractor != null) {
                extractor.record(response);
            }
        } catch (Exception ex) {
            LogUtil.warn("consume response content failed.", ex);
        }
    }

    private void recordException(Exception exception) {
        try {
            if (extractor != null) {
                extractor.record(exception);
            }
        } catch (Exception ex) {
            LogUtil.warn("consume response content failed.", ex);
        }
    }

    public static <T> FutureCallbackWrapper<T> get(HttpAsyncRequestProducer requestProducer, FutureCallback<T> delegate) {
        if (delegate instanceof FutureCallbackWrapper) {
            return ((FutureCallbackWrapper) delegate);
        }

        ApacheClientExtractor extractor;
        try {
            extractor = new ApacheClientExtractor(requestProducer.generateRequest());
        } catch (Exception ex) {
            extractor = null;
        }

        return new FutureCallbackWrapper<>(extractor, delegate);
    }
}

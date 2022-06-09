package io.arex.inst.httpclient.common;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCallbackWrapper<TResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCallbackWrapper.class);
    private final HttpClientExtractor<?, TResponse> extractor;

    protected final TraceTransmitter traceTransmitter;

    protected AbstractCallbackWrapper(HttpClientExtractor<?, TResponse> extractor) {
        this.extractor = extractor;
        this.traceTransmitter = TraceTransmitter.create();
    }

    public void replay() {
        try {
            mockResult(extractor.fetchMockResult());
        } catch (Exception ex) {
            this.doFailed(new ArexDataException("mock data failed.", ex));
        }
    }

    protected void doCancelled() {

    }

    protected abstract void doFailed(Throwable throwable);

    protected abstract void doCompleted(TResponse tResponse);

    private void mockResult(HttpResponseWrapper wrapped) {
        if (wrapped == null) {
            this.doFailed(new ArexDataException("mock data failed."));
            return;
        }

        ExceptionWrapper exception = wrapped.getException();
        if (exception != null) {
            if (exception.isCancelled()) {
                this.doCancelled();
            } else {
                this.doFailed(exception.getOriginalException());
            }
            return;
        }

        this.doCompleted(extractor.replay(wrapped));

    }

    protected void recordResponse(TResponse response) {
        try {
            if (extractor != null) {
                extractor.record(response);
            }
        } catch (Exception ex) {
            LOGGER.warn("consume response content failed:{} ", ex.getMessage(), ex);
        }
    }

    protected void recordException(Exception exception) {
        try {
            if (extractor != null) {
                extractor.record(exception);
            }
        } catch (Exception ex) {
            LOGGER.warn("consume response content failed:{}", ex.getMessage(), ex);
        }
    }
}
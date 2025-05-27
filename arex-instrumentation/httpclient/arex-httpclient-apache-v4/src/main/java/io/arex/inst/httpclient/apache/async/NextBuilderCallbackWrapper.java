package io.arex.inst.httpclient.apache.async;


import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter;
import io.arex.inst.httpclient.apache.common.NextBuilderExtractor;
import java.util.concurrent.Future;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;

/**
 * NextBuilderCallbackWrapper
 *
 * @author ywqiu
 * @date 2025/4/23 21:30
 */
public class NextBuilderCallbackWrapper<T> implements FutureCallback<T> {

    private final FutureCallback<T> delegate;
    private final TraceTransmitter traceTransmitter;

    // Maybe null, Just to pass the trace
    private final NextBuilderExtractor<HttpRequest, HttpResponse> extractor;

    private boolean needMock;

    public NextBuilderCallbackWrapper(FutureCallback<T> delegate) {
        this(null, delegate);
    }

    public NextBuilderCallbackWrapper(NextBuilderExtractor<HttpRequest, HttpResponse> extractor,
        FutureCallback<T> delegate) {
        this.traceTransmitter = TraceTransmitter.create();
        this.delegate = delegate;
        this.extractor = extractor;
    }

    @Override
    public void completed(T t) {
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
            if (delegate != null) {
                delegate.completed(t);
            }
        }
    }

    @Override
    public void failed(Exception e) {
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
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

    public void setNeedMock(boolean needMock) {
        this.needMock = needMock;
    }

    public boolean isNeedMock() {
        return needMock;
    }

    public MockResult mock() {
        return extractor.mock();
    }

    public Future<T> mock(MockResult mockResult) {
        BasicFuture<T> basicFuture = new BasicFuture<>(this.delegate);
        if (mockResult.getThrowable() != null) {
            basicFuture.failed((Exception) mockResult.getThrowable());
        } else {
            basicFuture.completed((T) mockResult.getResult());
        }
        return basicFuture;
    }

    public static <T> FutureCallback<T> wrap(HttpRequest httpRequest, FutureCallback<T> delegate) {
        if (delegate instanceof NextBuilderCallbackWrapper) {
            return delegate;
        }
        ApacheHttpClientAdapter adapter = new ApacheHttpClientAdapter(httpRequest);
        if (adapter.skipRemoteStorageRequest()) {
            return null;
        }
        return new NextBuilderCallbackWrapper<>(new NextBuilderExtractor<>(adapter), delegate);
    }

    /**
     * Wrap the delegate with NextBuilderCallbackWrapper for arex trace propagation
     */
    public static <T> FutureCallback<T> wrap(FutureCallback<T> delegate) {
        if (delegate instanceof NextBuilderCallbackWrapper) {
            return delegate;
        }
        return new NextBuilderCallbackWrapper<>(delegate);
    }
}

package io.arex.inst.httpclient.okhttp.v3;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.inst.httpclient.common.ExceptionWrapper;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author jmo
 * @since 2022/6/13
 */
public class OkHttpCallbackWrapper implements Callback {
    private final Call call;
    private final HttpClientExtractor<Request, Response> extractor;
    private final Callback delegate;
    private final TraceTransmitter traceTransmitter;

    public OkHttpCallbackWrapper(Call call, Callback delegate) {
        this.call = call;
        final HttpClientAdapter<Request, Response> adapter;
        adapter = new OkHttpClientAdapter(call.request().newBuilder().build());
        this.extractor = new HttpClientExtractor<>(adapter);
        this.delegate = delegate;
        this.traceTransmitter = TraceTransmitter.create();
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        // call from record
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
            extractor.record(e);
            delegate.onFailure(call, e);
        }
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        // call from record
        try (TraceTransmitter tm = traceTransmitter.transmit()) {
            extractor.record(response);
            delegate.onResponse(call, response);
        }
    }

    public void replay() {
        HttpResponseWrapper wrapped = extractor.fetchMockResult();
        if (wrapped == null) {
            this.delegate.onFailure(this.call, new IOException("not found mock resource"));
            return;
        }
        if (wrapped.getException() != null) {
            this.delegate.onFailure(this.call, unwrap(wrapped.getException()));
            return;
        }
        try {
            this.delegate.onResponse(this.call, extractor.replay(wrapped));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private IOException unwrap(ExceptionWrapper exception) {
        if (exception.isCancelled()) {
            return new IOException("The request cancelled");
        }
        Exception origin = exception.getOriginalException();
        if (origin instanceof IOException) {
            return (IOException) origin;
        }
        return new IOException(origin);
    }
}
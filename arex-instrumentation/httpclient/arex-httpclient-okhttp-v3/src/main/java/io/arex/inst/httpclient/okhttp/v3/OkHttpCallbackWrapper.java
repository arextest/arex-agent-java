package io.arex.inst.httpclient.okhttp.v3;

import com.google.common.annotations.VisibleForTesting;
import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OkHttpCallbackWrapper implements Callback {
    private final Call call;
    private final HttpClientExtractor<Request, Response> extractor;
    private final Callback delegate;
    private final TraceTransmitter traceTransmitter;

    public OkHttpCallbackWrapper(Call call, Callback delegate) {
        this(call, delegate, new HttpClientExtractor<>(new OkHttpClientAdapter(call.request().newBuilder().build())));
    }

    @VisibleForTesting
    OkHttpCallbackWrapper(Call call, Callback delegate, HttpClientExtractor<Request, Response> extractor) {
        this.call = call;
        this.extractor = extractor;
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

    public MockResult replay() {
        return extractor.replay();
    }

    public void replay(MockResult mockResult) {
        if (mockResult.getThrowable() != null) {
            this.delegate.onFailure(this.call, (IOException) mockResult.getThrowable());
        } else {
            try {
                this.delegate.onResponse(this.call, (Response) mockResult.getResult());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
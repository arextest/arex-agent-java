package io.arex.inst.httpclient.asynchttpclient.listener;

import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.inst.httpclient.asynchttpclient.wrapper.ResponseWrapper;
import io.arex.inst.httpclient.asynchttpclient.wrapper.ResponseStatusWrapper;
import io.arex.inst.runtime.log.LogManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.netty.EagerResponseBodyPart;
import org.asynchttpclient.uri.Uri;

public class AsyncHttpClientListenableFuture<T> implements ListenableFuture<T> {
    private final CompletableFuture<T> future = new CompletableFuture<>();
    private Object response;
    private Throwable throwable;
    private AsyncHandler<T> handler;

    public AsyncHttpClientListenableFuture() {
    }
    public AsyncHttpClientListenableFuture(Object response, Throwable throwable, AsyncHandler<T> handler) {
        this.response = response;
        this.throwable = throwable;
        this.handler = handler;
        done();
    }

    @Override
    public void done() {
        if (throwable != null) {
            future.completeExceptionally(throwable);
            handler.onThrowable(throwable);
            return;
        }
        try {
            if (response instanceof ResponseWrapper) {
                ResponseWrapper responseWrapper = (ResponseWrapper) response;
                final ResponseStatusWrapper responseStatusWrapper = new ResponseStatusWrapper(Uri.create(responseWrapper.getUri()),
                        responseWrapper);
                handler.onStatusReceived(responseStatusWrapper);
                HttpHeaders httpHeaders = buildHttpHeaders(responseWrapper);
                handler.onHeadersReceived(httpHeaders);
                HttpResponseBodyPart bodyPart = buildBodyPart(responseWrapper);
                handler.onBodyPartReceived(bodyPart);
            }
            future.complete(handler.onCompleted());
        } catch (Exception e) {
            LogManager.warn("AsyncHttpClient.done", e);
            future.completeExceptionally(e);
        }

    }

    private HttpResponseBodyPart buildBodyPart(ResponseWrapper responseWrapper) {
        final byte[] content = responseWrapper.getContent();
        if (content == null || content.length == 0) {
            return new EagerResponseBodyPart(Unpooled.EMPTY_BUFFER, true);
        }
        ByteBuf byteBuf = Unpooled.wrappedBuffer(ByteBuffer.wrap(content));
        return new EagerResponseBodyPart(byteBuf, true);
    }

    private HttpHeaders buildHttpHeaders(ResponseWrapper responseWrapper) {
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        final Map<String, List<String>> headers = responseWrapper.getHeaders();
        if (MapUtils.isEmpty(headers)) {
            return httpHeaders;
        }
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            httpHeaders.add(entry.getKey(), entry.getValue());
        }
        return httpHeaders;
    }

    @Override
    public void abort(Throwable t) {
        handler.onThrowable(t);
    }

    @Override
    public void touch() {
        // do nothing
    }

    @Override
    public ListenableFuture<T> addListener(Runnable listener, Executor exec) {
        if (exec == null) {
            exec = Runnable::run;
        }
        future.whenCompleteAsync((r, v) -> listener.run(), exec);
        return this;
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return future;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }
}

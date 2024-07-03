package io.arex.inst.httpclient.ning;

import com.ning.http.client.listenable.AbstractListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ResponseFutureWrapper extends AbstractListenableFuture {
    private final Object response;

    public ResponseFutureWrapper(Object response) {
        this.response = response;
    }

    @Override
    public void done() {
        runListeners();
    }

    @Override
    public void abort(Throwable t) {
        runListeners();
    }

    @Override
    public void touch() {
        // do nothing
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        runListeners();
        return true;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return this.response;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return get();
    }
}

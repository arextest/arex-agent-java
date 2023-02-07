package io.arex.inst.httpservlet.listener;

import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.ServletExtractor;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ServletAsyncListenerV5
 *
 * @date 2022/03/03
 */
public class ServletAsyncListenerV5 implements AsyncListener {
    private final AtomicBoolean asyncCompleted = new AtomicBoolean(false);

    private final ServletAdapter<HttpServletRequest, HttpServletResponse> adapter;

    public ServletAsyncListenerV5(ServletAdapter<HttpServletRequest, HttpServletResponse> adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        if (asyncCompleted.compareAndSet(false, true)) {
            onError(event);
        }
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        onError(event);
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        new ServletExtractor<>(adapter, (HttpServletRequest) event.getSuppliedRequest(),
            (HttpServletResponse) event.getSuppliedResponse()).execute();
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        AsyncContext eventAsyncContext = event.getAsyncContext();
        if (eventAsyncContext != null) {
            eventAsyncContext.addListener(this, event.getSuppliedRequest(), event.getSuppliedResponse());
        }
    }
}

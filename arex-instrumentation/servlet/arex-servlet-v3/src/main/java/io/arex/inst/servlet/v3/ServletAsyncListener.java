package io.arex.inst.servlet.v3;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ServletAsyncListener
 *
 *
 * @date 2022/03/03
 */
public class ServletAsyncListener implements AsyncListener {
    private final AtomicBoolean asyncCompleted = new AtomicBoolean(false);

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        if (asyncCompleted.compareAndSet(false, true)) {
            new ServletWrapper(event.getSuppliedRequest(), event.getSuppliedResponse()).execute();
        }
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        new ServletWrapper(event.getSuppliedRequest(), event.getSuppliedResponse()).execute();
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        new ServletWrapper(event.getSuppliedRequest(), event.getSuppliedResponse()).execute();
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        javax.servlet.AsyncContext eventAsyncContext = event.getAsyncContext();
        if (eventAsyncContext != null) {
            eventAsyncContext.addListener(this, event.getSuppliedRequest(), event.getSuppliedResponse());
        }
    }
}

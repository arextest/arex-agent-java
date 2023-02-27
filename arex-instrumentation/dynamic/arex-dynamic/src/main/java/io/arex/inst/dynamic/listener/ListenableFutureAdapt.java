package io.arex.inst.dynamic.listener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import io.arex.inst.dynamic.DynamicClassExtractor;
import java.util.concurrent.Future;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ListenableFutureAdapt {

    private final DynamicClassExtractor dynamicClassExtractor;
    private final ResponseFutureCallback responseFutureCallback;

    public ListenableFutureAdapt(DynamicClassExtractor dynamicClassExtractor) {
        this.dynamicClassExtractor = dynamicClassExtractor;
        this.responseFutureCallback = new ResponseFutureCallback();
    }

    public void addCallBack(Future result) {
        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(result), this.responseFutureCallback, DirectExecutor.INSTANCE);
    }

    public final class ResponseFutureCallback implements FutureCallback<Object> {
        @Override
        public void onSuccess(@Nullable Object result) {
            dynamicClassExtractor.setResponse(result);
        }

        @Override
        public void onFailure(Throwable throwable) {
            dynamicClassExtractor.setResponse(throwable);
        }
    }

}

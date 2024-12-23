package io.arex.inst.dynamic.common.listener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.runtime.listener.DirectExecutor;


public class ListenableFutureAdapter {

    private ListenableFutureAdapter() {

    }

    public static void addCallBack(ListenableFuture<?> resultFuture, DynamicClassExtractor extractor) {
        Futures.addCallback(resultFuture, new ResponseFutureCallback(extractor), DirectExecutor.INSTANCE);
    }

    /**
     * The inner class is to avoid loading the FutureCallBack class when the application does not import the guava package, resulting in classNotDef error
     */
    private static final class ResponseFutureCallback implements FutureCallback<Object> {
        private final DynamicClassExtractor extractor;

        public ResponseFutureCallback(DynamicClassExtractor extractor) {
            this.extractor = extractor;
        }

        @Override
        public void onSuccess(Object result) {
            extractor.recordResponse(result);
        }

        @Override
        public void onFailure(Throwable throwable) {
            extractor.recordResponse(throwable);
        }
    }

}

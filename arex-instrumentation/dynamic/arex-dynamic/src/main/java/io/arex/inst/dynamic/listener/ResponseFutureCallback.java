package io.arex.inst.dynamic.listener;

import com.google.common.util.concurrent.FutureCallback;
import io.arex.inst.dynamic.DynamicClassExtractor;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ResponseFutureCallback implements FutureCallback<Object> {

    private final DynamicClassExtractor dynamicClassExtractor;

    public ResponseFutureCallback(DynamicClassExtractor dynamicClassExtractor) {
        this.dynamicClassExtractor = dynamicClassExtractor;
    }

    @Override
    public void onSuccess(@Nullable Object result) {
        dynamicClassExtractor.setResponse(result);
    }

    @Override
    public void onFailure(Throwable throwable) {
        dynamicClassExtractor.setResponse(throwable);
    }
}

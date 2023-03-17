package io.arex.inst.dynamic.common.listener;

import java.util.function.BiConsumer;

import io.arex.inst.dynamic.common.DynamicClassExtractor;

public class ResponseConsumer implements BiConsumer<Object, Throwable> {

    private final DynamicClassExtractor dynamicClassExtractor;

    public ResponseConsumer(DynamicClassExtractor dynamicClassExtractor){
        this.dynamicClassExtractor = dynamicClassExtractor;
    }

    @Override
    public void accept(Object result, Throwable throwable) {
        if (throwable != null) {
            dynamicClassExtractor.setResponse(throwable);
            return;
        }
        dynamicClassExtractor.setResponse(result);
    }
}

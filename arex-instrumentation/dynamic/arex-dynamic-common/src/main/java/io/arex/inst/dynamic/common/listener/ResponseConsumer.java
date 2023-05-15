package io.arex.inst.dynamic.common.listener;

import java.util.function.BiConsumer;

import io.arex.inst.dynamic.common.DynamicClassExtractor;

public class ResponseConsumer implements BiConsumer<Object, Throwable> {

    private final DynamicClassExtractor extractor;

    public ResponseConsumer(DynamicClassExtractor extractor){
        this.extractor = extractor;
    }

    @Override
    public void accept(Object result, Throwable throwable) {
        if (throwable != null) {
            extractor.recordResponse(throwable);
        } else {
            extractor.recordResponse(result);
        }
    }
}

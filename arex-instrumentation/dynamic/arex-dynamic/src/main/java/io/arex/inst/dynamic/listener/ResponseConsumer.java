package io.arex.inst.dynamic.listener;

import io.arex.inst.dynamic.DynamicClassExtractor;
import java.util.function.BiConsumer;

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

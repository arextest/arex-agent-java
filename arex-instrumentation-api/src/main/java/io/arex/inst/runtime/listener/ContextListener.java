package io.arex.inst.runtime.listener;

import io.arex.inst.runtime.context.ArexContext;

public interface ContextListener {
    void onCreate(ArexContext arexContext);
    void onComplete(ArexContext arexContext);
}

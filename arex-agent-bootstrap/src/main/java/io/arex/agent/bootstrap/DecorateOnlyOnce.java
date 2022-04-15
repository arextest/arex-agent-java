package io.arex.agent.bootstrap;

import java.util.HashMap;

public final class DecorateOnlyOnce {
    private volatile boolean hasDecorated = false;
    private static HashMap<Class<?>, DecorateOnlyOnce> callMap = new HashMap<>(5);

    private DecorateOnlyOnce() {
    }

    public static DecorateOnlyOnce forClass(Class<?> clazz) {
        return callMap.computeIfAbsent(clazz, c -> new DecorateOnlyOnce());
    }

    public boolean hasDecorated() {
        return hasDecorated;
    }

    public void setDecorated() {
        this.hasDecorated = true;
    }
}

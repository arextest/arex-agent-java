package io.arex.agent.bootstrap;

import java.util.HashMap;

public final class DecorateControl {
    private volatile boolean hasDecorated = false;
    private static HashMap<Class<?>, DecorateControl> callMap = new HashMap<>(10);

    private DecorateControl() {
    }

    public static DecorateControl forClass(Class<?> clazz) {
        return callMap.computeIfAbsent(clazz, c -> new DecorateControl());
    }

    public boolean hasDecorated() {
        return hasDecorated;
    }

    public void setDecorated() {
        this.hasDecorated = true;
    }
}

package io.arex.inst.dynamic.common.listener;

import java.util.concurrent.Executor;

public enum DirectExecutor implements Executor {
    /**
     * singleton object
     */
    INSTANCE;
    private DirectExecutor() {
    }
    @Override public void execute(Runnable command) {
        command.run();
    }
}

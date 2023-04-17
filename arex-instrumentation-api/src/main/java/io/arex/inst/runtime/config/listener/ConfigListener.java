package io.arex.inst.runtime.config.listener;

import io.arex.inst.runtime.config.Config;

public interface ConfigListener {
    boolean validate(Config config);
    void load(Config config);
}

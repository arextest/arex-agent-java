package io.arex.inst.runtime.config.listener;

import io.arex.inst.runtime.config.Config;

public interface ConfigListener {
    void load(Config config);
}

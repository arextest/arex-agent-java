package io.arex.api.config;

import io.arex.api.Mode;

/**
 * ConfigService
 */
public interface ConfigService extends Mode {
    void initial(String agentArgs);
}

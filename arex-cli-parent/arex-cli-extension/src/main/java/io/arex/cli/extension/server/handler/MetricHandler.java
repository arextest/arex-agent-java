package io.arex.cli.extension.server.handler;

import io.arex.foundation.config.ConfigManager;

public class MetricHandler extends ApiHandler {
    @Override
    public String process(String args) throws Exception {
        return ConfigManager.INSTANCE.toString();
    }
}

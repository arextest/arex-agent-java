package io.arex.cli.api.extension;

import io.arex.api.Mode;

/**
 * CliServer
 *
 * @Date: Created in 2022/4/2
 */
public interface CliServer extends Mode {
    int start() throws Exception;
}

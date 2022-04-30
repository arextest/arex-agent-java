package io.arex.foundation.services;

import io.arex.foundation.util.SPIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Server Service
 * @Date: Created in 2022/4/2
 * @Modified By:
 */
public abstract class ServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerService.class);

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    public static ServerService INSTANCE;

    public static void init() {
        try {
            if (RUNNING.compareAndSet(false, true)) {
                ServiceLoader<ServerService> services = SPIUtil.load(ServerService.class,
                        "arex-cli-parent" + File.separator + "arex-server-extension", "arex-server-extension");
                if (services != null) {
                    int port = 0;
                    for (ServerService service : services) {
                        port = service.start();
                        INSTANCE = service;
                    }
                    if (INSTANCE != null) {
                        LOGGER.info("Arex server start on 127.0.0.1:{}", port);
                    }
                }
            }
        } catch (Throwable e) {
            RUNNING.set(false);
            LOGGER.warn("unable to load server service instance", e);
        }
    }

    public abstract int start() throws Exception;
}

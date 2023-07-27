package io.arex.foundation.util.httpclient.async;

/**
 * AutoCleanedPoolingNHttpClientConnectionManager
 *
 *
 * @date 2021/11/09
 */
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * AutoCleanedPoolingNHttpClientConnectionManager
 *
 *
 * @date 2021/02/04
 */
public final class AutoCleanedPoolingNHttpClientConnectionManager extends PoolingNHttpClientConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoCleanedPoolingNHttpClientConnectionManager.class);

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE =
        new ScheduledThreadPoolExecutor(1, new ThreadFactoryImpl("IdleNConnectionMonitorTask"));

    public static AutoCleanedPoolingNHttpClientConnectionManager createDefault() {
        AutoCleanedPoolingNHttpClientConnectionManager connectionManager =
            new AutoCleanedPoolingNHttpClientConnectionManager(ClientConnectionManagerConfig.DEFAULT_CONNECTION_TTL,
                ClientConnectionManagerConfig.DEFAULT_CONNECTION_IDLE_TIME,
                ClientConnectionManagerConfig.DEFAULT_CLEAN_CHECK_INTERVAL);

        // Number of concurrent connections for a per route
        connectionManager.setDefaultMaxPerRoute(ClientConnectionManagerConfig.DEFAULT_MAX_PER_ROUTE);
        // Maximum number of connections for all routes
        connectionManager.setMaxTotal(ClientConnectionManagerConfig.DEFAULT_MAX_PER_ROUTE * 10);

        return connectionManager;
    }

    private AutoCleanedPoolingNHttpClientConnectionManager(int connectionTtl, int connectionIdleTime,
        int cleanCheckInterval) {
        super(createConnectingIOReactor(), null, getDefaultRegistry(), null, null, connectionTtl,
            TimeUnit.MILLISECONDS);

        IdleNConnectionMonitorTask idleNConnectionMonitorTask =
            new IdleNConnectionMonitorTask(this, connectionIdleTime);
        SCHEDULED_EXECUTOR_SERVICE
            .scheduleWithFixedDelay(idleNConnectionMonitorTask, 0, cleanCheckInterval, TimeUnit.MILLISECONDS);
    }

    static Registry<SchemeIOSessionStrategy> getDefaultRegistry() {
        return RegistryBuilder.<SchemeIOSessionStrategy>create()
            .register("http", NoopIOSessionStrategy.INSTANCE)
            .register("https", SSLIOSessionStrategy.getDefaultStrategy())
            .build();
    }

    static ConnectingIOReactor createConnectingIOReactor() {
        // Configure IO thread
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
            .setIoThreadCount(Runtime.getRuntime().availableProcessors() * 2)
            .setSelectInterval(ClientConnectionManagerConfig.DEFAULT_SELECT_INTERVAL)
            .setSoKeepAlive(true)
            .build();
        try {
            return new DefaultConnectingIOReactor(ioReactorConfig);
        } catch (Exception ex) {
            LOGGER.warn("[[title=arex.createConnectingIOReactor]]", ex);
            return null;
        }
    }

    static class ClientConnectionManagerConfig {
        private static final int DEFAULT_SELECT_INTERVAL = 100;
        private static final int DEFAULT_CONNECTION_TTL = 30 * 1000;
        private static final int DEFAULT_CONNECTION_IDLE_TIME = 10 * 1000;
        private static final int DEFAULT_CLEAN_CHECK_INTERVAL = 5 * 1000;

        private static final int DEFAULT_MAX_PER_ROUTE = 200;
    }
}

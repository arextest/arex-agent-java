package io.arex.foundation.util.httpclient.async;

import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * IdleNConnectionMonitorTask
 *
 *
 * @date 2021/02/04
 */
public class IdleNConnectionMonitorTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdleNConnectionMonitorTask.class);

    private final PoolingNHttpClientConnectionManager connectionManager;
    private final int idleTimeoutMills;

    public IdleNConnectionMonitorTask(PoolingNHttpClientConnectionManager connMgr, int idleTimeoutMills) {
        this.connectionManager = connMgr;
        this.idleTimeoutMills = idleTimeoutMills > 0 ? idleTimeoutMills : 10 * 1000;
    }

    @Override
    public void run() {
        try {
            connectionManager.closeExpiredConnections();
        } catch (final Exception t) {
            LOGGER.warn("[[title=arex.closeExpiredConnections]]Error closing expired connections for async pool", t);
        }

        try {
            connectionManager.closeIdleConnections(idleTimeoutMills, TimeUnit.MILLISECONDS);
        } catch (final Exception t) {
            LOGGER.warn("[[title=arex.closeIdleConnections]]Error closing idle connections for async pool", t);
        }

    }
}

package io.arex.foundation.logger;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import org.junit.jupiter.api.Test;

/**
 * @since 2024/4/23
 */
class AgentLoggerFactoryTest {

    @Test
    void getAgentLogger() {
        System.setProperty(ConfigConstants.LOG_PATH, "var/log");
        AgentLogger agentLogger = AgentLoggerFactory.getAgentLogger(AgentLoggerFactoryTest.class);
        assertEquals(AgentLoggerFactoryTest.class.getName(), agentLogger.getName());

        agentLogger.error("{} message test", "error");

        agentLogger.error("{} message test", "error", new RuntimeException("error"));
    }
}

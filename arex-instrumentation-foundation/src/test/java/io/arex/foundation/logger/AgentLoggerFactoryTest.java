package io.arex.foundation.logger;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

/**
 * @since 2024/4/23
 */
class AgentLoggerFactoryTest {

    @Test
    void getAgentLogger() throws IOException {
        System.setProperty(ConfigConstants.LOG_PATH, "/var/tmp");
        AgentLogger agentLogger = AgentLoggerFactory.getAgentLogger(AgentLoggerFactoryTest.class);
        assertEquals(AgentLoggerFactoryTest.class.getName(), agentLogger.getName());

        agentLogger.info("{} message test", "info");

        agentLogger.warn("warn message test");
        agentLogger.warn("{} message test", "warn arg");
        agentLogger.warn("{} message test {}", "warn", "arg2");


        agentLogger.error("{} message test", "error");
        agentLogger.error("error message test", new Throwable("error"));

        String logFilePath = "/var/tmp/arex.startup." + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".log";
        assertTrue(Files.deleteIfExists(Paths.get(logFilePath)));
    }
}

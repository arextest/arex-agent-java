package io.arex.foundation.logger;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.util.StringUtil;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 2024/4/23
 */
public class AgentLoggerFactory {
    static final ConcurrentMap<String, AgentLogger> loggerMap;

    private static final PrintStream agentPrintStream;

    static {
        loggerMap = new ConcurrentHashMap<>();
        agentPrintStream = getPrintStream("startup");
    }

    public static AgentLogger getAgentLogger(Class<?> clazz) {
        return loggerMap.computeIfAbsent(clazz.getName(), name -> createLogger(name, agentPrintStream));
    }

    private static AgentLogger createLogger(String name, PrintStream printStream) {
        return new AgentLogger(name, printStream);
    }

    private static PrintStream getPrintStream(String fileName) {
        try {
            String logPath = System.getProperty(ConfigConstants.LOG_PATH);
            if (StringUtil.isEmpty(logPath)) {
                return System.out;
            }

            String logFilePath = logPath + "/arex." + fileName + "." + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".log";
            return new PrintStream(new FileOutputStream(logFilePath, true));
        } catch (FileNotFoundException e) {
            System.err.println("[AREX] Failed to create log file: " + e.getMessage());
            return System.out;
        }
    }

}

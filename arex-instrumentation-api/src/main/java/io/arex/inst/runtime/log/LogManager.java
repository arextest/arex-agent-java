package io.arex.inst.runtime.log;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;

public class LogManager {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LogManager.class);
    private static final List<Logger> EXTENSION_LOGGER_LIST = new ArrayList<>();

    public static void build(List<Logger> logger) {
        EXTENSION_LOGGER_LIST.addAll(logger);
    }

    public static void addReplayTag(String replayId) {
        if (StringUtil.isNotEmpty(replayId)) {
            MDC.put("arex-replay-id", replayId);
        } else {
            MDC.remove("arex-replay-id");
        }
    }

    public static void addCaseTag(String recordId) {
        if (StringUtil.isNotEmpty(recordId)) {
            MDC.put("arex-case-id", recordId);
        } else {
            MDC.remove("arex-case-id");
        }
    }

    public static void addTag(String caseId, String replayId) {
        addCaseTag(caseId);
        addReplayTag(replayId);
    }

    public static String buildTitle(String title) {
        return StringUtil.format("[[title=arex.%s]]", title);
    }

    public static String buildTitle(String prefix, String title) {
        return StringUtil.format("[[title=arex.%s%s]]", prefix, title);
    }

    public static void info(String title, String message) {
        String logMessage = buildMessage(buildTitle(title), message);
        if (useExtensionLog()) {
            for (Logger extensionLogger : EXTENSION_LOGGER_LIST) {
                extensionLogger.info(logMessage);
            }
            return;
        }

        LOGGER.info(logMessage);
    }

    public static void warn(String title, Throwable ex) {
        warn(title, null, ex);
    }

    public static void warn(String title, String message) {
        warn(title, message, null);
    }

    public static void warn(String title, String message, Throwable exception) {
        String logMessage = buildMessage(buildTitle(title), message);
        if (useExtensionLog()) {
            for (Logger logger : EXTENSION_LOGGER_LIST) {
                logger.warn(logMessage, exception);
            }
            return;
        }

        LOGGER.warn(logMessage, exception);
    }


    private static String buildMessage(String title, String message) {
        if (StringUtil.isEmpty(message)) {
            return title;
        }
        return title + message;
    }

    private static boolean useExtensionLog() {
        return CollectionUtil.isNotEmpty(EXTENSION_LOGGER_LIST);
    }

    public static void setContextMap(Map<String, String> contextMap) {
        MDC.setContextMap(Objects.isNull(contextMap) ? new HashMap<>() : contextMap);
    }
}

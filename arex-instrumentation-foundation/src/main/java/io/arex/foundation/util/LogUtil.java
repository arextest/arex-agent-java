package io.arex.foundation.util;

import io.arex.agent.bootstrap.TraceContextManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LogUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogUtil.class);
    private static final String TITLE = "[[title=arex]]";

    public static void addReplayTag(String replayId) {
        if (StringUtils.isNotEmpty(replayId)) {
            MDC.put("arex-replay-id", replayId);
        } else {
            MDC.remove("arex-replay-id");
        }
    }

    public static void addCaseTag(String recordId) {
        if (StringUtils.isNotEmpty(recordId)) {
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
        return String.format("[[title=arex.%s]]", title);
    }

    public static String buildTitle(String prefix, String title) {
        return String.format("[[title=arex.%s%s]]", prefix, title);
    }

    public static void info(String message) {
        LOGGER.info(buildMessage(message));
    }

    public static void info(String title, String message) {
        LOGGER.info(buildMessage(title, message));
    }

    public static void warn(String message) {
        LOGGER.warn(buildMessage(message));
    }

    public static void warn(String message, Throwable exception) {
        LOGGER.warn(buildMessage(message), exception);
    }

    private static String buildMessage(String message) {
        String traceId = TraceContextManager.get();
        if (StringUtils.isNotEmpty(traceId)) {
            return String.format("%s[%s]%s", TITLE, traceId, message);
        } else {
            return String.format("%s%s", TITLE, message);
        }
    }

    private static String buildMessage(String title, String message) {
        String traceId = TraceContextManager.get();
        if (StringUtils.isNotEmpty(traceId)) {
            return String.format("%s[%s][%s]%s", TITLE, title, traceId, message);
        } else {
            return String.format("%s[%s]%s", TITLE, title, message);
        }
    }

    public static void setContextMap(Map<String, String> contextMap) {
        MDC.setContextMap(Objects.isNull(contextMap) ? new HashMap<>() : contextMap);
    }

}

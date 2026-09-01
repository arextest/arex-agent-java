package io.arex.agent.compare.utils;

import io.arex.agent.bootstrap.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogUtil.class);

    public static String buildTitle(String title) {
        return StringUtil.format("[[title=arex.%s]]", title);
    }

    public static void info(String title, String message) {
        String logMessage = buildMessage(buildTitle(title), message);
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
        LOGGER.warn(logMessage, exception);
    }

    private static String buildMessage(String title, String message) {
        if (StringUtil.isEmpty(message)) {
            return title;
        }
        return title + message;
    }
}

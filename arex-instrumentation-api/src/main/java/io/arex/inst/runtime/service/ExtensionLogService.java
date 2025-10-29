package io.arex.inst.runtime.service;

import io.arex.agent.bootstrap.util.ServiceLoader;
import java.util.List;
import java.util.Map;

/**
 * ExtensionLogService
 *
 * @author ywqiu
 * @date 2025/10/21 15:41
 */
public class ExtensionLogService {

    private static ExtensionLogService INSTANCE;

    private final ExtensionLogCollector saver;

    public static ExtensionLogService getInstance() {
        if (INSTANCE == null) {
            List<ExtensionLogCollector> dataCollectors = ServiceLoader.load(
                ExtensionLogCollector.class);
            if (!dataCollectors.isEmpty()) {
                INSTANCE = new ExtensionLogService(dataCollectors.get(0));
            }
        }
        return INSTANCE;
    }


    ExtensionLogService(ExtensionLogCollector dataSaver) {
        this.saver = dataSaver;
    }

    public void info(String title, Object message) {
        saver.info(title, message);
    }

    public void info(String title, String message) {
        saver.info(title, message);
    }

    public void info(String title, String message, Map<String, String> tagMaps) {
        saver.info(title, message, tagMaps);
    }

    public void warn(String title, String message) {
        saver.warn(title, message);
    }

    public void warn(String title, Object message) {
        saver.warn(title, message);
    }

    public void warn(String title, Throwable throwable) {
        saver.warn(title, throwable);
    }

    public void warn(String title, Throwable throwable, Map<String, String> tagMaps) {
        saver.warn(title, throwable, tagMaps);
    }

    public void warn(String title, String throwable, Map<String, String> tagMaps) {
        saver.warn(title, throwable, tagMaps);
    }

    public void error(String title, String message) {
        saver.error(title, message);
    }

    public void error(String title, Throwable throwable) {
        saver.error(title, throwable);
    }

    public void error(String title, Throwable throwable, Map<String, String> tagMaps) {
        saver.error(title, throwable, tagMaps);
    }

    public void soaLog(String title, Object request, Object response, long starTime) {
        saver.soaLog(title, request, response, starTime);
    }

    public void soaLog(String title, Object request, Object response, long starTime, Map<String, String> tagMaps) {
        saver.soaLog(title, request, response, starTime, tagMaps);
    }

    public void soaLog(String title, String request, String response, long starTime, Map<String, String> tagMaps) {
        saver.soaLog(title, request, response, starTime, tagMaps);
    }
}

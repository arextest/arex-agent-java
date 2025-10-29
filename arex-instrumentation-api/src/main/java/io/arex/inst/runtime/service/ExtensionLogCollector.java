package io.arex.inst.runtime.service;

import java.util.Map;

/**
 * ExtensionLogCollector
 *
 * @author ywqiu
 * @date 2025/10/21 15:36
 */
public interface ExtensionLogCollector {

    void info(String title, Object message);

    void info(String title, String message);

    void info(String title, String message, Map<String, String> tagMaps);

    void warn(String title, String message);

    void warn(String title, Object message);

    void warn(String title, Throwable throwable);

    void warn(String title, Throwable throwable, Map<String, String> tagMaps);

    void warn(String title, String message, Map<String, String> tagMaps);

    void error(String title, String message);

    void error(String title, Throwable throwable);

    void error(String title, Throwable throwable, Map<String, String> tagMaps);

    void soaLog(String title,Object request, Object response, long starTime);

    void soaLog(String title, Object request, Object response, long starTime, Map<String, String> tagMaps);

    void soaLog(String title, String request, String response, long starTime, Map<String, String> tagMaps);
}

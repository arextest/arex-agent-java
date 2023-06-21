package io.arex.inst.runtime.log;

public interface Logger {
    void info(String message);
    void warn(String message);
    void warn(String message, Throwable exception);
    void error(String message);
    void error(String message, Throwable exception);
}

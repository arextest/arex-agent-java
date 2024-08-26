package io.arex.foundation.logger;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * @since 2024/4/22
 */
public class AgentLogger {
    private final String name;
    private final PrintStream printStream;
    public AgentLogger(String name, PrintStream printStream) {
        this.name = name;
        this.printStream = printStream;
    }

    public void warn(String format) {
        warn(format, null);
    }

    public void warn(String format, Object arg) {
        warn(format, arg, null);
    }

    public void warn(String format, Object arg1, Object arg2) {
        formatAndLog(format, arg1, arg2);
    }

    public void info(String format, Object... arguments) {
        formatAndLog(format, arguments);
    }

    public void error(String format, Object... arguments) {
        formatAndLog(format, arguments);
    }

    public void error(String msg, Throwable t) {
        write(msg, t);
    }

    private void formatAndLog(String format, Object arg1, Object arg2) {
        FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
        write(tp.getMessage(), tp.getThrowable());
    }

    private void formatAndLog(String format, Object... arguments) {
        FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
        write(tp.getMessage(), tp.getThrowable());
    }

    private void write(String message, Throwable t) {
        StringBuilder builder = new StringBuilder(32);
        builder.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))).append(" ");
        builder.append("[").append(Thread.currentThread().getName()).append("] ").append(" ");
        builder.append(name).append(" - ");
        builder.append(message);

        printStream.println(builder);
        writeThrowable(t, printStream);
        printStream.flush();
    }

    private void writeThrowable(Throwable t, PrintStream targetStream) {
        if (t != null) {
            t.printStackTrace(targetStream);
        }
    }

    public String getName() {
        return name;
    }
}

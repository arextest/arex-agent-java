package io.arex.cli.server.handler;

import io.arex.agent.bootstrap.model.ArexConstants;
import io.termd.core.readline.Function;
import io.termd.core.readline.Keymap;
import io.termd.core.readline.Readline;
import io.termd.core.tty.TtyConnection;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ServerHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

    private static Map<String, ApiHandler> handlerMap;

    public static void handle(TtyConnection conn) {
        if (handlerMap == null) {
            init();
        }

        readline(new Readline(Keymap.getDefault()).addFunctions(Function.loadDefaults()), conn);
    }

    public static void readline(Readline readline, TtyConnection conn) {
        readline.readline(conn, ArexConstants.CLI_PROMPT, line -> {
            line = StringUtils.defaultString(line);
            if (StringUtils.equalsIgnoreCase(line, "exit")) {
                conn.write("exit\n" + ArexConstants.CLI_PROMPT).close();
            } else {
                String response;
                try {
                    String[] lines = line.split(" ");
                    String args = "";
                    String command = lines[0];
                    if (lines.length > 1) {
                        args = lines[1];
                    }
                    ApiHandler apiHandler = handlerMap.get(command);
                    if (apiHandler == null) {
                        response = "invalid command";
                    } else {
                        response = apiHandler.process(args);
                    }
                } catch (Throwable e) {
                    LOGGER.warn("command execute error", e);
                    response = e.getMessage();
                }
                conn.write((response == null ? "" : response) + "\n");
                // Read line again
                readline(readline, conn);
            }
        });
    }

    private static void init(){
        handlerMap = new HashMap<>();
        register(new ReplayHandler());
        register(new WatchHandler());
        register(new DebugHandler());
        register(new MetricHandler());
        register(new RecordHandler());
    }

    private static void register(ApiHandler handler){
        String key = handler.getClass().getSimpleName();
        handlerMap.put(key.substring(0, key.indexOf("Handler")).toLowerCase(), handler);
    }
}

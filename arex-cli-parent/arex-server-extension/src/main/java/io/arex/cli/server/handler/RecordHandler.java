package io.arex.cli.server.handler;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.services.DataService;
import java.util.Map;

public class RecordHandler extends ApiHandler {
    @Override
    public String process(String args) throws Exception {
        StringBuilder result = new StringBuilder();
        Map<String, String> argMap = parseArgs(args);
        if (argMap != null) {
            for (Map.Entry<String, String> argEntry : argMap.entrySet()) {
                switch (argEntry.getKey()) {
                    case "c": // close record
                        DataService.INSTANCE.stop();
                        result.append("turn off record.");
                        break;
                    case "r": // set record rate
                        ConfigManager.INSTANCE.setRecordRate(argEntry.getValue());
                        result.append("reset record rate.");
                        break;
                    default:
                        result.append("invalid option.");
                        break;
                }
            }
        } else {
            DataService.INSTANCE.start();
            result.append("startup record");
        }
        return result.toString();
    }
}

package io.arex.cli.server.handler;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.services.DataService;
import org.apache.commons.lang3.ArrayUtils;

public class RecordHandler extends ApiHandler {
    @Override
    public String process(String args) throws Exception {
        StringBuilder result = new StringBuilder();
        String[] options = parseArgs(args);
        if (ArrayUtils.isNotEmpty(options)) {
            for (String option : options) {
                String[] params = parseOption(option);
                switch (params[0]) {
                    case "c": // close record
                        DataService.INSTANCE.stop();
                        result.append("turn off record ");
                    case "r": // set record rate
                        ConfigManager.INSTANCE.setRecordRate(Integer.parseInt(params[1]));
                        result.append("reset record rate ");
                }
            }
        } else {
            DataService.INSTANCE.start();
        }
        return result.toString();
    }
}

package io.arex.cli.extension.server.handler;

import io.arex.foundation.services.DataService;
import org.apache.commons.lang3.ArrayUtils;

public class RecordHandler extends ApiHandler {
    @Override
    public String process(String args) throws Exception {
        String[] param = parseArgs(args);
        if (ArrayUtils.isNotEmpty(param)) {
            switch (param[0]) {
                case "-c":
                    DataService.INSTANCE.stop();
                    return "turn off record";
                case "-r":
                    // rate
            }
        }
        return null;
    }
}

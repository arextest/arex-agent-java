package io.arex.inst.config.apollo;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;

public class ApolloConfigChecker {

    private static boolean isLoadedApollo = false;

    private static final String APOLLO_MODULE = "apollo-config";

    static {
        try {
            Class.forName("com.ctrip.framework.apollo.ConfigService");
            isLoadedApollo = true;
        } catch (ClassNotFoundException e) {
            // ignore, means business application unLoad apollo-client
        }
    }

    public static boolean unloadApollo() {
        return !isLoadedApollo || Config.get().getString(ConfigConstants.DISABLE_MODULE, StringUtil.EMPTY).contains(APOLLO_MODULE);
    }
}

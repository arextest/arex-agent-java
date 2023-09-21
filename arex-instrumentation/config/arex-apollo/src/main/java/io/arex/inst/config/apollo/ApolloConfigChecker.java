package io.arex.inst.config.apollo;

public class ApolloConfigChecker {

    private static boolean isLoadedApollo = false;

    static {
        try {
            Class.forName("com.ctrip.framework.apollo.ConfigService");
            isLoadedApollo = true;
        } catch (ClassNotFoundException e) {
            // ignore, means business application unLoad apollo-client
        }
    }

    public static boolean unloadApollo() {
        return !isLoadedApollo;
    }
}

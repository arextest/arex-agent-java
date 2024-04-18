package io.arex.agent.bootstrap.util;

public class JdkUtils {
    public static final int JDK_11 = 11;

    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    public static boolean isJdk11OrHigher() {
        return getJavaVersion() >= JDK_11;
    }
}

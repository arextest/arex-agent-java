package io.arex.cli.util;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.security.CodeSource;
import java.util.*;

/**
 * SystemUtils
 * @Date: Created in 2022/4/20
 * @Modified By:
 */
public class SystemUtils {

    private static String JAVA_HOME = null;

    private static String TOOLS_JAR_PATH = null;

    private static final String JAVA_VERSION = System.getProperty("java.specification.version");

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

    static PlatformEnum platform;

    static {
        if (OS_NAME.startsWith("linux")) {
            platform = PlatformEnum.LINUX;
        } else if (OS_NAME.startsWith("mac") || OS_NAME.startsWith("darwin")) {
            platform = PlatformEnum.MACOSX;
        } else if (OS_NAME.startsWith("windows")) {
            platform = PlatformEnum.WINDOWS;
        } else {
            platform = PlatformEnum.UNKNOWN;
        }
    }

    public enum PlatformEnum {
        WINDOWS,
        LINUX,
        MACOSX,
        UNKNOWN
    }

    public static boolean isWindows() {
        return platform == PlatformEnum.WINDOWS;
    }

    public static boolean isLinux() {
        return platform == PlatformEnum.LINUX;
    }

    public static boolean isMac() {
        return platform == PlatformEnum.MACOSX;
    }

    public static Map<Long, String> javaPids() {
        Map<Long, String> result = new LinkedHashMap<>();
        String jps = "jps";
        File jpsFile = findJps();
        if (jpsFile != null) {
            jps = jpsFile.getAbsolutePath();
        }

        String[] command = new String[] { jps, "-l" };

        List<String> jpsList = runNative(command);

        long currentPid = currentPid();
        for (String jpsStr : jpsList) {
            String[] strings = jpsStr.trim().split("\\s+");
            if (strings.length < 1) {
                continue;
            }
            try {
                long pid = Long.parseLong(strings[0]);
                if (pid == currentPid) {
                    // exclude current command line pid
                    continue;
                }
                if (strings.length >= 2 && isJpsProcess(strings[1])) { // skip jps
                    continue;
                }

                result.put(pid, jpsStr);
            } catch (Throwable e) {
                // ignore
            }
        }

        return result;
    }

    private static long currentPid() {
        try {
            String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            int index = jvmName.indexOf('@');
            if (index > 0) {
                return Long.parseLong(jvmName.substring(0, index));
            }
        } catch (Throwable e) {
            // ignore
        }
        return -1;
    }

    private static File findJps() {
        // Try to find jps under java.home
        String javaHome = System.getProperty("java.home");
        String[] paths = { "bin/jps", "bin/jps.exe", "../bin/jps", "../bin/jps.exe" };
        List<File> jpsList = new ArrayList<>();
        for (String path : paths) {
            File jpsFile = new File(javaHome, path);
            if (jpsFile.exists()) {
                jpsList.add(jpsFile);
            }
        }

        // Try to find jps under env JAVA_HOME
        String javaHomeEnv = "";
        if (jpsList.isEmpty()) {
            javaHomeEnv = System.getenv("JAVA_HOME");
            for (String path : paths) {
                File jpsFile = new File(javaHomeEnv, path);
                if (jpsFile.exists()) {
                    jpsList.add(jpsFile);
                }
            }
        }
        if (jpsList.isEmpty()) {
            return null;
        }

        // find the shortest path, jre path longer than jdk path
        if (jpsList.size() > 1) {
            jpsList.sort((file1, file2) -> {
                try {
                    return file1.getCanonicalPath().length() - file2.getCanonicalPath().length();
                } catch (IOException e) {
                    // ignore
                }
                return -1;
            });
        }
        return jpsList.get(0);
    }

    private static boolean isJpsProcess(String mainClassName) {
        return mainClassName.contains("sun.tools.jps.Jps");
    }

    public static String javaBinDir(String javaHome) {
        // find java/java.exe
        File javaPath = findJava(javaHome);
        if (javaPath == null) {
            return null;
        }

        return javaPath.getAbsolutePath();
    }

    public static String getArexHomeDir() {
        CodeSource codeSource = SystemUtils.class.getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            try {
                File jarPath = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
                return jarPath.getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath();
            } catch (Throwable e) {
                // ignore
            }
        }
        return null;
    }

    public static String findModuleJarDir(String moduleDir, String jarNamePrefix) {
        String arexHomeDir = getArexHomeDir();
        File agentDir = new File(arexHomeDir + File.separator + moduleDir);
        File[] files = agentDir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            return null;
        }
        for (File file : files) {
            String name = file.getName();
            if (file.isFile() && name.startsWith(jarNamePrefix)) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static boolean lessThanJava9() {
        return Float.parseFloat(JAVA_VERSION) < 9.0f;
    }

    public static boolean greaterThanJava8() {
        return Float.parseFloat(JAVA_VERSION) > 1.8f;
    }

    public static String findJavaHome() {
        if (JAVA_HOME != null) {
            return JAVA_HOME;
        }

        String javaHome = System.getProperty("java.home");
        if (lessThanJava9()) {
            File toolsJar = new File(javaHome, "lib/tools.jar");
            if (!toolsJar.exists()) {
                toolsJar = new File(javaHome, "../lib/tools.jar");
            }
            if (!toolsJar.exists()) {
                toolsJar = new File(javaHome, "../../lib/tools.jar");
            }

            if (toolsJar.exists()) {
                JAVA_HOME = javaHome;
                TOOLS_JAR_PATH = toolsJar.getAbsolutePath();
                return JAVA_HOME;
            }

            if (!toolsJar.exists()) {
                String javaHomeEnv = System.getenv("JAVA_HOME");
                if (javaHomeEnv != null && !javaHomeEnv.isEmpty()) {
                    // $JAVA_HOME/lib/tools.jar
                    toolsJar = new File(javaHomeEnv, "lib/tools.jar");
                    if (!toolsJar.exists()) {
                        toolsJar = new File(javaHomeEnv, "../lib/tools.jar");
                    }
                }

                if (toolsJar.exists()) {
                    JAVA_HOME = javaHome;
                    TOOLS_JAR_PATH = toolsJar.getAbsolutePath();
                    return JAVA_HOME;
                }

                throw new IllegalArgumentException("Can not find tools.jar under java home: " + javaHome
                        + ", please try to start arex-cli with full path java. Such as /opt/jdk/bin/java " +
                        "java -cp arex-cli.jar io.arex.cli.ArexCli");
            }
        } else {
            JAVA_HOME = javaHome;
        }
        return JAVA_HOME;
    }

    private static File findJava(String javaHome) {
        String[] paths = { "bin/java", "bin/java.exe", "../bin/java", "../bin/java.exe" };

        List<File> javaList = new ArrayList<File>();
        for (String path : paths) {
            File javaFile = new File(javaHome, path);
            if (javaFile.exists()) {
                javaList.add(javaFile);
            }
        }

        if (javaList.isEmpty()) {
            return null;
        }

        sort(javaList);

        return javaList.get(0);
    }

    private static void sort(List<File> javaList) {
        // find the shortest path, jre path longer than jdk path
        if (javaList.size() > 1) {
            javaList.sort((file1, file2) -> {
                try {
                    return file1.getCanonicalPath().length() - file2.getCanonicalPath().length();
                } catch (IOException e) {
                    // ignore
                }
                return -1;
            });
        }
    }

    public static String getToolsJarDir() {
        return TOOLS_JAR_PATH;
    }

    public static long findTcpListenProcess(int port) {
        try {
            if (isWindows()) {
                String[] command = { "netstat", "-ano", "-p", "TCP" };
                List<String> lines = runNative(command);
                for (String line : lines) {
                    if (line.contains("LISTENING")) {
                        // TCP 127.0.0.1:4000 0.0.0.0:0 LISTENING 1234
                        String[] strings = line.trim().split("\\s+");
                        if (strings.length == 5) {
                            if (strings[1].endsWith(":" + port)) {
                                return Long.parseLong(strings[4]);
                            }
                        }
                    }
                }
            }

            if (isLinux() ||isMac()) {
                String pid = getAnswerAt("lsof -t -s TCP:LISTEN -i TCP:" + port, 0);
                if (!pid.trim().isEmpty()) {
                    return Long.parseLong(pid);
                }
            }
        } catch (Throwable e) {
            // ignore
        }

        return -1;
    }

    public static List<String> runNative(String[] cmdToRunWithArgs){
        Process p;
        try {
            p = Runtime.getRuntime().exec(cmdToRunWithArgs);
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
        ArrayList<String> sa = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sa.add(line);
            }
            p.waitFor();
        } catch (Exception e) {
            return sa;
        }
        return sa;
    }

    public static String getAnswerAt(String cmd2launch, int answerIdx) {
        String[] cmd = cmd2launch.split(" ");
        List<String> sa = runNative(cmd);

        if (answerIdx >= 0 && answerIdx < sa.size()) {
            return sa.get(answerIdx);
        }
        return "";
    }
}

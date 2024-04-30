package io.arex.agent;

import io.arex.agent.bootstrap.AgentInitializer;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.security.CodeSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Premain-Class for the Arex agent.
 *
 * <p>The JVM loads the class onto the application's classloader,
 * and then the agent needs to append jars from its internal directory: bootstrap/ to the bootstrap classloader.
 */
@SuppressWarnings("SystemOut")
public class ArexAgent {
    private static final String AGENT_VERSION = "arex.agent.version";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        init(inst, agentArgs);
    }

    private static void init(Instrumentation inst, String agentArgs) {
        try {
            printAgentInfo();
            /*
             * The jars that need to be added to the bootstrap classloader
             * are packaged in the /bootstrap directory inside the arex-agent.jar package.
             * These jars should be added to the bootstrap classloader in advance.
             */
            installBootstrapJar(inst);
            AgentInitializer.initialize(inst, getJarFile(ArexAgent.class), agentArgs, ArexAgent.class.getClassLoader());
        } catch (Exception ex) {
            System.out.printf("%s [AREX] Agent initialize error, stacktrace: %s%n", getCurrentTime(), ex);
        }
    }

    private static synchronized void installBootstrapJar(Instrumentation inst) throws Exception {
        File agentJarFile = getJarFile(ArexAgent.class);
        List<File> nestedBootStrapJars = JarUtils.extractNestedBootStrapJar(agentJarFile);
        for (File bootStrapFile : nestedBootStrapJars) {
            inst.appendToBootstrapClassLoaderSearch(new JarFile(bootStrapFile, false));
        }
    }

    private static synchronized File getJarFile(Class<?> clazz) throws Exception {
        CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            throw new IllegalStateException("could not get agent jar location");
        }

        return new File(codeSource.getLocation().toURI());
    }

    public static void init(Instrumentation inst, File agent) {
        try {
            printAgentInfo();
            /*
             * The jars that need to be added to the bootstrap classloader
             * are packaged in the /bootstrap directory inside the arex-agent.jar package.
             * These jars should be added to the bootstrap classloader in advance.
             */
            installBootstrapJar(inst);
            AgentInitializer.initialize(inst, agent, "", ArexAgent.class.getClassLoader());
        } catch (Exception ex) {
            System.out.printf("%s [AREX] Agent initialize error, stacktrace: %s%n", getCurrentTime(), ex);
        }
    }

    private static void printAgentInfo() {
        String agentVersion = ArexAgent.class.getPackage().getImplementationVersion();
        System.out.printf("%s [AREX] Agent-v%s starts initialization...%n", getCurrentTime(), agentVersion);
        if (agentVersion != null) {
            System.setProperty(AGENT_VERSION, agentVersion);
        }
    }

    private static String getCurrentTime() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

}

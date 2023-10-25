package io.arex.agent;

import io.arex.agent.bootstrap.AgentInitializer;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.security.CodeSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.jar.JarFile;

@SuppressWarnings("SystemOut")
public class ArexJavaAgent {
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
            installBootstrapJar(inst);
            AgentInitializer.initialize(inst, getJarFile(ArexJavaAgent.class), agentArgs);
        } catch (Exception ex) {
            System.out.printf("%s [AREX] Agent initialize error, stacktrace: %s%n", getCurrentTime(), ex);
        }
    }

    private static synchronized void installBootstrapJar(Instrumentation inst)
            throws Exception {
        JarFile agentJar = new JarFile(getJarFile(AgentInitializer.class), false);
        inst.appendToBootstrapClassLoaderSearch(agentJar);
    }

    private static synchronized File getJarFile(Class<?> clazz) throws Exception {
        CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            throw new IllegalStateException("could not get agent jar location");
        }

        return new File(codeSource.getLocation().toURI());
    }

    public static void init(Instrumentation inst, File agent, File bootstrap) {
        try {
            printAgentInfo();
            JarFile agentJar = new JarFile(bootstrap, false);
            inst.appendToBootstrapClassLoaderSearch(agentJar);
            AgentInitializer.initialize(inst, agent, "");
        } catch (Exception ex) {
            System.out.printf("%s [AREX] Agent initialize error, stacktrace: %s%n", getCurrentTime(), ex);
        }
    }

    private static void printAgentInfo() {
        String agentVersion = ArexJavaAgent.class.getPackage().getImplementationVersion();
        System.out.printf("%s [AREX] Agent-v%s starts initialization...%n", getCurrentTime(), agentVersion);
        if (agentVersion != null) {
            System.setProperty(AGENT_VERSION, agentVersion);
        }
    }

    private static String getCurrentTime() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

}

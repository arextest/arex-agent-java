package io.arex.agent.bootstrap;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class AgentInitializer {

    private static ClassLoader agentClassLoader;

    public static void initialize(Instrumentation inst, File agentJar, String agentArgs)
            throws Exception {
        if (agentClassLoader != null) {
            return;
        }

        System.setProperty("arex-agent-jar-file-path", agentJar.getAbsolutePath());
        agentClassLoader = createAgentClassLoader(agentJar);
        AgentInstaller installer = createAgentInstaller(inst, agentJar, agentArgs);
        installer.install();
    }

    private static AgentClassLoader createAgentClassLoader(File agentFile) throws Exception {
        return new AgentClassLoader(agentFile, getAgentClassLoaderParent(), null);
    }


    private static ClassLoader getAgentClassLoaderParent() {
        try {
            return (ClassLoader) ClassLoader.class.getDeclaredMethod("getPlatformClassLoader").invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static AgentInstaller createAgentInstaller(Instrumentation inst, File file, String agentArgs) throws Exception {
        Class<?> clazz =
                agentClassLoader.loadClass("io.arex.agent.instrumentation.InstrumentationInstaller");
        Constructor<?> constructor = clazz.getDeclaredConstructor(Instrumentation.class, File.class, String.class);
        return (AgentInstaller) constructor.newInstance(inst, file, agentArgs);
    }
}

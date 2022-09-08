package io.arex.agent;

import io.arex.agent.bootstrap.AgentInstaller;
import io.arex.agent.instrumentation.InstrumentationInstaller;
import io.arex.foundation.services.ConfigService;
import io.arex.foundation.services.DataService;
import io.arex.agent.bootstrap.AgentInitializer;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.security.CodeSource;
import java.util.jar.JarFile;

@SuppressWarnings("SystemOut")
public class ArexJavaAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        init(inst, agentArgs);
    }

    private static void init(Instrumentation inst, String agentArgs) {
        try {
            installBootstrapJar(inst);

            AgentInitializer.initialize(inst, getJarFile(ArexJavaAgent.class), agentArgs);
            System.out.println("ArexJavaAgent installed.");
        } catch (Exception ex) {
            System.out.println("ArexJavaAgent start failed.");
            ex.printStackTrace();
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
            JarFile agentJar = new JarFile(bootstrap, false);
            inst.appendToBootstrapClassLoaderSearch(agentJar);
            AgentInitializer.initialize(inst, agent, "");
            System.out.println("ArexJavaAgent installed.");
        } catch (Exception ex) {
            System.out.println("ArexJavaAgent start failed.");
            ex.printStackTrace();
        }
    }
}

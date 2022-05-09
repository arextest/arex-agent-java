package io.arex.agent;

import io.arex.agent.bootstrap.AgentInitializer;
import io.arex.api.config.ConfigService;
import io.arex.foundation.extension.ExtensionLoader;
import io.arex.foundation.services.DataService;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.security.CodeSource;
import java.util.jar.JarFile;

@SuppressWarnings("SystemOut")
public class ArexJavaAgent {

    private static final ArexJavaAgent INSTANCE = new ArexJavaAgent();

    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        INSTANCE.init(inst, agentArgs);
    }

    private void init(Instrumentation inst, String agentArgs) {
        try {
            ExtensionLoader.getExtension(ConfigService.class).initial(agentArgs);
            DataService.INSTANCE.initial();

            installBootstrapJar(inst);
            AgentInitializer.initialize(inst, getJarFile(ArexJavaAgent.class), agentArgs);
            System.out.println("ArexJavaAgent installed.");
        } catch (Exception ex) {
            System.out.println("ArexJavaAgent start failed.");
            ex.printStackTrace();
        }
    }

    private synchronized void installBootstrapJar(Instrumentation inst) throws Exception {
        JarFile agentJar = new JarFile(getJarFile(AgentInitializer.class), false);
        inst.appendToBootstrapClassLoaderSearch(agentJar);
    }

    private synchronized File getJarFile(Class<?> clazz) throws Exception {
        CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            throw new IllegalStateException("could not get agent jar location");
        }

        return new File(codeSource.getLocation().toURI());
    }
}

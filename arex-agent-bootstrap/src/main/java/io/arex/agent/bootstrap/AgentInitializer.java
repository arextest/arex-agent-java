package io.arex.agent.bootstrap;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.util.AdviceClassesCollector;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;

public class AgentInitializer {

    private static ClassLoader classLoader;

    /**
     * @param parentClassLoader Normally, the parentClassLoader should be ClassLoaders.AppClassLoader.
     */
    public static void initialize(Instrumentation inst, File agentFile, String agentArgs, ClassLoader parentClassLoader)
            throws Exception {
        if (classLoader != null) {
            return;
        }
        System.setProperty(ConfigConstants.SHADED_LOGGER_SHOW_DATE_TIME, "true");
        System.setProperty(ConfigConstants.SHADED_LOGGER_DATE_TIME_FORMAT, "yyyy-MM-dd HH:mm:ss:SSS");
        File[] extensionFiles = getExtensionJarFiles(agentFile);
        classLoader = new AgentClassLoader(agentFile, parentClassLoader, extensionFiles);
        InstrumentationHolder.setAgentClassLoader(classLoader);
        InstrumentationHolder.setInstrumentation(inst);
        InstrumentationHolder.setAgentFile(agentFile);
        AgentInstaller installer = createAgentInstaller(inst, agentFile, agentArgs);
        addJarToLoaderSearch(agentFile, extensionFiles);
        installer.install();
    }

    private static void addJarToLoaderSearch(File agentFile, File[] extensionFiles) {
        AdviceClassesCollector.INSTANCE.addJarToLoaderSearch(agentFile);

        if (extensionFiles == null) {
            return;
        }

        for (File file : extensionFiles) {
            AdviceClassesCollector.INSTANCE.addJarToLoaderSearch(file);
        }
    }

    private static File[] getExtensionJarFiles(File jarFile) {
        String extensionDir = jarFile.getParent() + "/extensions/";
        return new File(extensionDir).listFiles(AgentInitializer::isJar);
    }

    private static boolean isJar(File f) {
        return f.isFile() && f.getName().endsWith(".jar");
    }

    private static AgentInstaller createAgentInstaller(Instrumentation inst, File file, String agentArgs) throws Exception {
        Class<?> clazz = classLoader.loadClass("io.arex.agent.instrumentation.InstrumentationInstaller");
        Constructor<?> constructor = clazz.getDeclaredConstructor(Instrumentation.class, File.class, String.class);
        return (AgentInstaller) constructor.newInstance(inst, file, agentArgs);
    }
}

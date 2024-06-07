package io.arex.agent.bootstrap;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.util.AdviceClassesCollector;
import io.arex.agent.bootstrap.util.StringUtil;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AgentInitializer {

    public static final String SIMPLE_DATE_FORMAT_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";

    private static ClassLoader classLoader;

    /**
     * @param parentClassLoader Normally, the parentClassLoader should be ClassLoaders.AppClassLoader.
     */
    public static void initialize(Instrumentation inst, File agentFile, String agentArgs, ClassLoader parentClassLoader)
            throws Exception {
        if (classLoader != null) {
            return;
        }
        initializeSimpleLoggerConfig(agentFile.getParent());
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

    private static void initializeSimpleLoggerConfig(String agentFileParent) {
        System.setProperty(ConfigConstants.SIMPLE_LOGGER_SHOW_DATE_TIME, Boolean.TRUE.toString());
        System.setProperty(ConfigConstants.SIMPLE_LOGGER_DATE_TIME_FORMAT, SIMPLE_DATE_FORMAT_MILLIS);

        String logPath = System.getProperty(ConfigConstants.LOG_PATH);
        if (StringUtil.isEmpty(logPath)) {
            logPath = agentFileParent + "/logs";
            System.setProperty(ConfigConstants.LOG_PATH, logPath);
        }

        Path filePath = Paths.get(logPath);
        if (Files.notExists(filePath)) {
            try {
                Files.createDirectories(filePath);
            } catch (IOException e) {
                System.err.printf("%s [AREX] Failed to create log directory: %s%n",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT_MILLIS)), logPath);
                return;
            }
        }
        String logFilePath = logPath + "/arex." + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".log";
        System.setProperty(ConfigConstants.SIMPLE_LOGGER_FILE, logFilePath);
    }
}

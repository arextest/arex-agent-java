package io.arex.agent.bootstrap.util;

import io.arex.agent.bootstrap.InstrumentationHolder;
import io.arex.agent.bootstrap.cache.AdviceInjectorCache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import net.bytebuddy.dynamic.ClassFileLocator;

public class AdviceClassesCollector {

    public static final AdviceClassesCollector INSTANCE = new AdviceClassesCollector();
    private static final String EXCLUDE_CLASS_PREFIX = "shaded";
    private static final String CLASS_AREX_AGENT_PREFIX = "io/arex/inst";
    private static final String CLASS_SERIALIZER_PREFIX = "io/arex/foundation/serializer";
    private static final String CLASS_SUFFIX = ".class";
    private static final int CLASS_SUFFIX_LENGTH = CLASS_SUFFIX.length();
    private static final String JAR_SUFFIX = ".jar";
    private static final String THIRD_PARTY = "third-party";
    private static final Map<String, List<String>> THIRD_PARTY_NESTED_JARS_PATH_MAP = new ConcurrentHashMap<>();

    private AdviceClassesCollector() {
    }

    public void addJarToLoaderSearch(File file) {
        if (file == null) {
            return;
        }
        boolean isExtensionJar = file.getAbsolutePath().contains("extensions");
        addJarToLoaderSearch(file, isExtensionJar);
    }

    private void addJarToLoaderSearch(File file, boolean isExtensionJar) {
        try (JarInputStream jarInputStream = new JarInputStream(Files.newInputStream(file.toPath()))) {
            JarEntry jarEntry;
            JarFile jarFile = new JarFile(file);
            do {
                jarEntry = jarInputStream.getNextJarEntry();

                if (jarEntry != null && !jarEntry.isDirectory()) {
                    String entryName = jarEntry.getName();
                    if (ServiceLoader.match(entryName)) {
                        ServiceLoader.buildCache(jarFile, jarEntry, entryName);
                        continue;
                    }
                    // ex: entryName : third-party/jackson/jackson-databind-2.13.1.jar -> secondaryDirectory: jackson
                    if (StringUtil.startWith(entryName, THIRD_PARTY) && entryName.endsWith(JAR_SUFFIX)) {
                        Path entryPath = Paths.get(entryName);
                        int pathNameCount = entryPath.getNameCount();
                        String secondaryDirectory = pathNameCount > 1 ? entryPath.getName(pathNameCount - 2).toString() : entryPath.getName(0).toString();
                        List<String> filePathList = THIRD_PARTY_NESTED_JARS_PATH_MAP.computeIfAbsent(secondaryDirectory, k -> new ArrayList<>());
                        filePathList.add(entryName);
                        continue;
                    }
                    // exclude package io.arex.inst.runtime/extension, not class, and shaded class.
                    boolean isFilterEntry = StringUtil.isEmpty(entryName) ||
                            entryName.startsWith(EXCLUDE_CLASS_PREFIX) ||
                            !entryName.endsWith(CLASS_SUFFIX) ||
                            StringUtil.startWithFrom(entryName, "runtime", 13) ||
                            StringUtil.startWithFrom(entryName, "extension", 13);
                    if (isFilterEntry) {
                        continue;
                    }
                    String className = entryName.replace('/', '.');
                    String realClassName = className.substring(0,
                            className.length() - CLASS_SUFFIX_LENGTH);
                    // classes that meet these conditions will be loaded by User classLoader
                    if (isExtensionJar || entryName.startsWith(CLASS_AREX_AGENT_PREFIX) || entryName.startsWith(CLASS_SERIALIZER_PREFIX)) {
                        addClassToInjectorCache(realClassName);
                    }
                }

            } while (jarEntry != null);
        } catch (Throwable ex) {
            System.err.printf("add jar classes to advice failed, file: %s, exception: %s%n", file.getAbsolutePath(), ex);
        }
    }

    public void addClassToLoaderSearch(Class<?> clazz) {
        if (clazz == null) {
            return;
        }

        try {
            Class<?>[] classes = clazz.getDeclaredClasses();
            for (Class<?> innerClass : classes) {
                addClassToInjectorCache(innerClass.getName());
            }
            addClassToInjectorCache(clazz.getName());
        } catch (Throwable ex) {
            System.err.printf("add single class to advice failed, clazz: %s%n", clazz.getName());
        }
    }

    private void addClassToInjectorCache(String adviceClassName) {
        ClassLoader loader = InstrumentationHolder.getAgentClassLoader();

        try {
            if (!AdviceInjectorCache.contains(adviceClassName)) {
                AdviceInjectorCache.registerInjector(adviceClassName,
                        new AdviceInjectorCache.AdviceClassInjector(getBytes(adviceClassName, loader)));
            }
        } catch (Exception ex) {
            System.err.printf("create class %s injector failed.", adviceClassName);
        }
    }

    private byte[] getBytes(String name, ClassLoader loader) throws IOException {
        ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(loader);
        return locator.locate(name).resolve();
    }

    public void appendToClassLoaderSearch(String jarPackageName, ClassLoader loader) {
        List<String> filePathList = THIRD_PARTY_NESTED_JARS_PATH_MAP.get(jarPackageName);
        if (CollectionUtil.isEmpty(filePathList)) {
            return;
        }
        try (JarFile agentJarFile = new JarFile(InstrumentationHolder.getAgentFile())){
            for (String filePath : filePathList) {
                JarEntry jarEntry = agentJarFile.getJarEntry(filePath);
                File extractNestedJar = JarUtils.extractNestedJar(agentJarFile, jarEntry, filePath);
                JarUtils.appendToClassLoaderSearch(loader, extractNestedJar);
            }
        } catch (Exception ex) {
            System.err.printf("appendToClassLoaderSearch failed, jarPackageName: %s%n", jarPackageName);
        }
    }

}

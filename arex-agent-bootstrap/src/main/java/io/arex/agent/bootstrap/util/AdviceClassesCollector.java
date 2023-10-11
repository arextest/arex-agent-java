package io.arex.agent.bootstrap.util;

import io.arex.agent.bootstrap.InstrumentationHolder;
import io.arex.agent.bootstrap.cache.AdviceInjectorCache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import net.bytebuddy.dynamic.ClassFileLocator;

public class AdviceClassesCollector {

    public static final AdviceClassesCollector INSTANCE = new AdviceClassesCollector();
    private static final String EXCLUDE_CLASS_PREFIX = "shaded";
    private static final String CLASS_AREX_AGENT_PREFIX = "io/arex/inst";
    private static final String CLASS_SERIALIZER_PREFIX = "io/arex/foundation/serializer";
    private static final String CLASS_SUFFIX = ".class";
    private static final int CLASS_SUFFIX_LENGTH = CLASS_SUFFIX.length();

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
            do {
                jarEntry = jarInputStream.getNextJarEntry();

                if (jarEntry != null && !jarEntry.isDirectory()) {
                    String entryName = jarEntry.getName();
                    if (ServiceLoader.match(entryName)) {
                        ServiceLoader.buildCache(file, jarEntry, entryName);
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
            System.err.printf("add jar classes to advice failed, file: %s%n", file.getAbsolutePath());
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

}

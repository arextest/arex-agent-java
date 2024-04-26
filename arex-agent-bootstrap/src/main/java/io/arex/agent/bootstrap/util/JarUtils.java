package io.arex.agent.bootstrap.util;

import io.arex.agent.bootstrap.constants.ConfigConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtils {
    private static Method addURL;
    private static final File TMP_FILE = new File(AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty("java.io.tmpdir")));
    private static final String AREX_TEMP_DIR = TMP_FILE.getAbsolutePath() + File.separator + "arex";

    static {
        try {
            addURL = Class.forName("java.net.URLClassLoader").getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
        } catch (Exception e) {
            System.err.println("Failed to get addURL method from URLClassLoader");
        }
    }
    public static File extractNestedJar(JarFile file, JarEntry entry, String entryName) throws IOException {
        File outputFile = createFile(AREX_TEMP_DIR + File.separator + entryName);
        try(InputStream inputStream = file.getInputStream(entry);
            FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
        return outputFile;
    }

    private static File createFile(String path) throws IOException {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        boolean newFile = file.createNewFile();
        if (newFile) {
            System.out.printf("create file: %s%n", file.getAbsolutePath());
        }
        return file;
    }

    /**
     * tomcat jdk <= 8, classLoader is ParallelWebappClassLoader, ClassLoader.getSystemClassLoader() is Launcher$AppClassLoader
     * jdk > 8, classLoader is ParallelWebappClassLoader, ClassLoader.getSystemClassLoader() is ClassLoaders$AppClassLoader
     */
    public static void appendToClassLoaderSearch(ClassLoader classLoader, File jarFile) {
        try {
            if (classLoader instanceof URLClassLoader) {
                addURL.invoke(classLoader, jarFile.toURI().toURL());
            }

            /*
             * Due to Java 8 vs java 9+ incompatibility issues
             * See https://stackoverflow.com/questions/46694600/java-9-compatability-issue-with-classloader-getsystemclassloader/51584718
             */
            ClassLoader urlClassLoader = ClassLoader.getSystemClassLoader();
            if (!(urlClassLoader instanceof URLClassLoader)) {
                try (URLClassLoader tempClassLoader = new URLClassLoader(new URL[] {jarFile.toURI().toURL()}, urlClassLoader)) {
                    addURL.invoke(tempClassLoader, jarFile.toURI().toURL());
                }
            } else {
                addURL.invoke(urlClassLoader, jarFile.toURI().toURL());
            }
            appendToAppClassLoaderSearch(classLoader, jarFile);
        } catch (Exception e) {
            System.err.printf("appendToClassLoaderSearch failed, classLoader: %s, jarFile: %s%n",
                    classLoader.getClass().getName(), jarFile.getAbsolutePath());
        }
    }

    /**
     * append jar jdk.internal.loader.ClassLoaders.AppClassLoader
     * if java version >= 11 need add jvm option:--add-opens=java.base/jdk.internal.loader=ALL-UNNAMED
     */
    private static void appendToAppClassLoaderSearch(ClassLoader classLoader, File jarFile) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<? extends ClassLoader> loaderClass = classLoader.getClass();
        if (JdkUtils.isJdk11OrHigher() && ConfigConstants.APP_CLASSLOADER_NAME.equalsIgnoreCase(loaderClass.getName())) {
            Method classPathMethod = loaderClass.getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
            classPathMethod.setAccessible(true);
            classPathMethod.invoke(classLoader, jarFile.getPath());
        }
    }
}

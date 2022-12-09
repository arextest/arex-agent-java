package io.arex.foundation.util;

import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class SPIUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(SPIUtil.class);

    @SuppressWarnings("ForEachIterable")
    public static <T> List<T> load(Class<T> serviceClass) {
        List<T> result = new ArrayList<>();
        java.util.ServiceLoader<T> services = ServiceLoader.load(serviceClass);
        for (Iterator<T> iter = services.iterator(); iter.hasNext(); ) {
            try {
                result.add(iter.next());
            } catch (Throwable e) {
                LOGGER.warn("Unable to load class: {} from classloader: {}, throwable: {}",
                    serviceClass.getName(), serviceClass.getClassLoader(), e.toString());
            }
        }
        return result;
    }

    public static <T> ServiceLoader<T> load(Class<T> spiType, String moduleName, String jarName) {
        try {
            String jarDir = getJarDir(moduleName, jarName);
            LOGGER.info("Arex spi load jar: {}", jarDir);
            List<URL> urls = getJarUrls(jarDir);
            ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]));
            return ServiceLoader.load(spiType, cl);
        } catch (Throwable e) {
            LOGGER.error("Arex spi load class: {} from classloader: {}, error: {}",
                spiType.getCanonicalName(), spiType.getClassLoader() , e.toString());
        }
        return null;
    }

    public static <T> ServiceLoader<T> load(Class<T> spiType, ClassLoader cl) {
        try {
            return ServiceLoader.load(spiType, cl);
        } catch (Throwable e) {
            LOGGER.error("Arex spi load class: {} error", spiType.getCanonicalName(), e);
        }
        return null;
    }

    private static List<URL> getJarUrls(String jarDir) throws Exception {
        File file = new File(jarDir);
        List<URL> jarPaths = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles(SPIUtil::isJar);
            if (files == null) {
                return jarPaths;
            }
            for (File jarFile : files) {
                if (isJar(jarFile)) {
                    jarPaths.add(jarFile.toURI().toURL());
                }
            }
        } else if (isJar(file)) {
            jarPaths.add(file.toURI().toURL());
        }
        return jarPaths;
    }

    private static boolean isJar(File f) {
        return f.isFile() && f.getName().endsWith(".jar");
    }

    private static String getJarDir(String moduleName, String jarName) throws Exception {
        CodeSource codeSource = SPIUtil.class.getProtectionDomain().getCodeSource();
        File jarPath = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
        String projectDir;
        if (jarPath.getAbsolutePath().endsWith(File.separator + "classes")) {
            projectDir = jarPath.getParentFile().getParentFile().getParentFile().getAbsolutePath();
        } else {
            projectDir = jarPath.getParentFile().getParentFile().getAbsolutePath();
        }
        return projectDir + File.separator + moduleName + File.separator + "target" + File.separator + jarName + ".jar";
    }
}

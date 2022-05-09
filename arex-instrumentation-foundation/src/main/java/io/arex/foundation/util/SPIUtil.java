package io.arex.foundation.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Iterator;
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
                LogUtil.warn(String.format("Unable to load instrumentation class: %s", serviceClass.getName()), e);
            }
        }
        return result;
    }

    public static <T> ServiceLoader<T> load(Class<T> spiType, String moduleName, String jarName) {
        try {
            String jarDir = getJarDir(moduleName, jarName);
            LOGGER.info("Arex spi load jar: {}", jarDir);
            URL[] urls = getJarUrls(jarDir);
            ClassLoader cl = new URLClassLoader(urls);
            return ServiceLoader.load(spiType, cl);
        } catch (Throwable e) {
            LOGGER.error("Arex spi load class: {} error", spiType.getCanonicalName(), e);
        }
        return null;
    }

    private static URL[] getJarUrls(String jarDir) throws Exception {
        File file = new File(jarDir);
        List<URL> jarPaths = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return jarPaths.toArray(new URL[0]);
            }
            for (File jarFile : files) {
                if (jarFile.isFile() && jarFile.getName().endsWith(".jar")) {
                    jarPaths.add(new URL("file:" + jarFile.getPath()));
                }
            }
        } else if (file.isFile() && file.getName().endsWith(".jar")) {
            jarPaths.add(new URL("file:" + file.getPath()));
        }
        return jarPaths.toArray(new URL[0]);
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

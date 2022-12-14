package io.arex.inst.runtime.context;

import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.agent.bootstrap.internal.Pair;
import io.arex.agent.bootstrap.util.StringUtil;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Manifest;

public class ResourceManager {
    private static final Set<ClassLoader> cache = new HashSet<>();
    private static final String BYTEBUDDY_PREFIX = StringUtil.removeShadePrefix("net.bytebuddy.");

    public static void registerResources(ClassLoader loader) {
        if (!checkLoader(loader)) {
            return;
        }

        classLoaderInitResources(loader);
    }

    private static boolean checkLoader(ClassLoader loader) {
        if (loader == null || cache.contains(loader)) {
            return false;
        }
        cache.add(loader);

        String name = loader.getClass().getName();
        if (name.startsWith("sun.reflect.DelegatingClassLoader") || name.startsWith(BYTEBUDDY_PREFIX)) {
            return false;
        }
        return true;
    }

    private static void classLoaderInitResources(ClassLoader classLoader) {
        try {
            Enumeration<URL> files = classLoader.getResources("META-INF/MANIFEST.MF");
            while (files.hasMoreElements()) {
                URL url = files.nextElement();
                try (InputStream stream = url.openStream()) {
                    Manifest mf = new Manifest(stream);
                    String packageName = mf.getMainAttributes().getValue("Bundle-Name");
                    if (StringUtil.isEmpty(packageName)) {
                        packageName = mf.getMainAttributes().getValue("Automatic-Module-Name");
                    }
                    if (StringUtil.isEmpty(packageName)) {
                        continue;
                    }

                    String version = mf.getMainAttributes().getValue("Bundle-Version");
                    if (StringUtil.isEmpty(version)) {
                        version = mf.getMainAttributes().getValue("Implementation-Version");
                    }
                    if (StringUtil.isEmpty(version)) {
                        continue;
                    }
                    LoadedModuleCache.registerProjectModule(packageName, parse(version));
                } catch (Exception ex) {
                    continue;
                }
            }
        } catch (Exception ex) {
            return;
        }

    }

    private static Pair<Integer, Integer> parse(String version) {
        int index = version.indexOf('.');
        if (index < 0) {
            return null;
        }
        int major = Integer.parseInt(version.substring(0, index));
        int next = version.indexOf('.', index + 1);
        if (next < 0) {
            return null;
        }
        int minor = Integer.parseInt(version.substring(index + 1, next));
        return Pair.of(major, minor);
    }
}

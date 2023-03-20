package io.arex.inst.runtime.context;

import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.agent.bootstrap.internal.Pair;
import io.arex.agent.bootstrap.util.ConcurrentHashSet;
import io.arex.agent.bootstrap.util.StringUtil;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.Manifest;

public class ResourceManager {
    private static final Set<ClassLoader> CACHE = new ConcurrentHashSet<>();

    public static void registerResources(ClassLoader loader) {
        if (!checkLoader(loader)) {
            return;
        }

        classLoaderInitResources(loader);
    }

    private static boolean checkLoader(ClassLoader loader) {
        if (loader == null || CACHE.contains(loader)) {
            return false;
        }
        CACHE.add(loader);
        return true;
    }

    /**
     * Register a package that allows instrumentation
     *
     * <p>package name in manifest file, with key: Bundle-Name or Automatic-Module-Name
     * <p>package version in manifest file, with key: Bundle-Version or Implementation-Version
     */
    private static void classLoaderInitResources(ClassLoader classLoader) {
        try {
            Enumeration<URL> files = classLoader.getResources("META-INF/MANIFEST.MF");
            while (files.hasMoreElements()) {
                URL url = files.nextElement();
                try (InputStream stream = url.openStream()) {
                    Manifest mf = new Manifest(stream);
                    String packageName = getManifestAttr(mf, "Bundle-Name", "Automatic-Module-Name");
                    if (StringUtil.isEmpty(packageName)) {
                        continue;
                    }

                    String version = getManifestAttr(mf, "Bundle-Version", "Implementation-Version");
                    if (StringUtil.isEmpty(version)) {
                        continue;
                    }
                    LoadedModuleCache.registerProjectModule(packageName, parse(version));
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    private static String getManifestAttr(Manifest manifest, String... keys) {
        for (String key : keys) {
            String value = manifest.getMainAttributes().getValue(key);
            if (StringUtil.isNotEmpty(value)) {
                return value;
            }
        }
        return null;
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

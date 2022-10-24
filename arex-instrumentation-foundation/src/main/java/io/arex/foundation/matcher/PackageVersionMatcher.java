package io.arex.foundation.matcher;

import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.foundation.api.ModuleDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.Manifest;

public class PackageVersionMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {
    public static ElementMatcher.Junction<ClassLoader> versionMatch(ModuleDescription moduleDescription) {
        return new PackageVersionMatcher(moduleDescription);
    }

    private static final Set<ClassLoader> cache = new HashSet<>(10);

    private final ModuleDescription moduleDescription;

    PackageVersionMatcher(ModuleDescription moduleDescription) {
        this.moduleDescription = moduleDescription;
    }

    @Override
    public boolean matches(ClassLoader cl) {
        if (cl == null || moduleDescription == null) {
            return false;
        }

        if (!cache.contains(cl)) {
            classLoaderInitResources(cl);
        }
        return LoadedModuleCache.hasResource(moduleDescription.getPackages());
    }

    private void classLoaderInitResources(ClassLoader classLoader) {
        try {
            Enumeration<URL> files = classLoader.getResources("META-INF/MANIFEST.MF");
            while (files.hasMoreElements()) {
                URL url = files.nextElement();
                try (InputStream stream = url.openStream()) {
                    Manifest mf = new Manifest(stream);
                    String packageName = mf.getMainAttributes().getValue("Bundle-Name");
                    if (isEmpty(packageName)) {
                        packageName = mf.getMainAttributes().getValue("Automatic-Module-Name");
                    }
                    if (isEmpty(packageName)) {
                        continue;
                    }

                    String version = mf.getMainAttributes().getValue("Bundle-Version");
                    if (isEmpty(version)) {
                        version = mf.getMainAttributes().getValue("Implementation-Version");
                    }
                    if (isEmpty(version)) {
                        continue;
                    }
                    LoadedModuleCache.registerResource(packageName, version);
                } catch (Exception ex) {
                    continue;
                }
            }
        } catch (Exception ex) {
            return;
        }
        cache.add(classLoader);
    }

    private boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }
}

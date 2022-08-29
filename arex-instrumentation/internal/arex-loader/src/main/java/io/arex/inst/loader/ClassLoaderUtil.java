package io.arex.inst.loader;

import io.arex.agent.bootstrap.DecorateControl;
import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.foundation.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

public class ClassLoaderUtil {

    public static void registerResource(ClassLoader classLoader) {
        DecorateControl call = DecorateControl.forClass(ClassLoaderUtil.class);
        if (call.hasDecorated()) {
            return;
        }
        call.setDecorated();

        Enumeration<URL> files;
        try {
            files = classLoader.getResources("META-INF/MANIFEST.MF");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

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
                LoadedModuleCache.registerResource(packageName, version);
            } catch (Exception ex) {
                continue;
            }
        }
    }
}

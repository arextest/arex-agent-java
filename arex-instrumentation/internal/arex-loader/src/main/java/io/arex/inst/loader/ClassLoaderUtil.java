package io.arex.inst.loader;

import io.arex.agent.bootstrap.DecorateOnlyOnce;
import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import io.arex.foundation.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

public class ClassLoaderUtil {

    public static URL[] addUcp(String agentJarPath, URL[] urls) {
        if (urls == null || urls.length == 0) {
            return urls;
        }

        DecorateOnlyOnce call = DecorateOnlyOnce.forClass(ClassLoaderUtil.class);
        if (call.hasDecorated()) {
            // just decorate the top level class loader and only decorate once for all class loader
            return urls;
        }
        call.setDecorated();

        URL url;
        try {
            url = new File(agentJarPath).toURI().toURL();
        } catch (IOException e) {
            return urls;
        }

        URL[] newURLs = new URL[urls.length + 1];
        for (int i = 0; i < urls.length; i++) {
            newURLs[i] = urls[i];
        }
        newURLs[urls.length] = url;
        return newURLs;
    }

    public static void registerResource(ClassLoader classLoader) {
        DecorateOnlyOnce call = DecorateOnlyOnce.forClass(ClassLoaderUtil.class);
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

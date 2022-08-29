package io.arex.agent.bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * simple class loader
 * todo: add plugin jar support
 */
public class AgentClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private String filePath;
    private JarFile agentJarFile;

    private CodeSource codeSource;

    public AgentClassLoader(File jarFile, ClassLoader parent, String[] extensionJars) {
        super(new URL[] {}, parent);

        try {
            filePath = jarFile.getAbsolutePath();
            this.agentJarFile = new JarFile(jarFile, false);
            codeSource = new CodeSource(jarFile.toURI().toURL(), (Certificate[]) null);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open agent jar", e);
        }
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = findLoadedClass(name);
            if (clazz == null) {
                clazz = findClass(name);
            }

            if (clazz == null) {
                clazz = super.loadClass(name, false);
            }
            if (resolve) {
                resolveClass(clazz);
            }

            return clazz;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        JarEntry jarEntry = findJarEntry(name.replace('.', '/') + ".class");
        if (jarEntry != null) {
            byte[] bytes;
            try {
                bytes = getJarEntryBytes(jarEntry);
            } catch (IOException exception) {
                throw new ClassNotFoundException(name, exception);
            }

            definePackageIfNeeded(name);
            return defineClass(name, bytes);
        }
        return null;
    }

    public Class<?> defineClass(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }

    private void definePackageIfNeeded(String className) {
        String packageName = getPackageName(className);
        if (packageName == null) {
            return;
        }
        if (getPackage(packageName) == null) {
            try {
                definePackage(packageName, agentJarFile.getManifest(), codeSource.getLocation());
            } catch (Exception exception) {
                if (getPackage(packageName) == null) {
                    throw new IllegalStateException("Failed to define package", exception);
                }
            }
        }
    }

    private static String getPackageName(String className) {
        int index = className.lastIndexOf('.');
        return index == -1 ? null : className.substring(0, index);
    }

    private byte[] getJarEntryBytes(JarEntry jarEntry) throws IOException {
        int size = (int)jarEntry.getSize();
        byte[] buffer = new byte[size];
        try (InputStream is = agentJarFile.getInputStream(jarEntry)) {
            int offset = 0;
            int read;

            while (offset < size && (read = is.read(buffer, offset, size - offset)) != -1) {
                offset += read;
            }
        }

        return buffer;
    }

    private JarEntry findJarEntry(String name) {
        return agentJarFile.getJarEntry(name);
    }

    private URL getJarEntryUrl(JarEntry jarEntry) {
        if (jarEntry != null) {
            try {
                return new URL("jar:file:" + filePath + "!/" + jarEntry.getName());
            } catch (MalformedURLException e) {
                throw new IllegalStateException(jarEntry.getName(), e);
            }
        }
        return null;
    }

    @Override
    public URL findResource(String name) {
        URL url = getJarEntryUrl(findJarEntry(name));
        if (url != null) {
            return url;
        }
        return super.findResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        URL url = getJarEntryUrl(findJarEntry(name));
        if (url == null) {
            return super.findResources(name);
        }

        List<URL> resources = new LinkedList<>();
        resources.add(url);
        while (super.findResources(name).hasMoreElements()) {
            resources.add(super.findResources(name).nextElement());
        }

        final Iterator<URL> iterator = resources.iterator();
        return new Enumeration<URL>() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public URL nextElement() {
                return iterator.next();
            }
        };
    }
}

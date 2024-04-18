package io.arex.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtils {
    private static final String JAR_SUFFIX = ".jar";
    private static final String DELIMITER = ";";
    private static final String NESTED_BOOTSTRAP_JARS_PATH = "Nested-BootStrap-Jars-Path";

    private static final File TMP_FILE = new File(AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty("java.io.tmpdir")));
    private static final String TEMP_DIR_BOOTSTRAP = TMP_FILE.getAbsolutePath() + File.separator + "arex" + File.separator + "bootstrap";

    /**
     * ex:
     * arex-agent.jar
     * │
     * └───bootstrap
     * │   │   arex-agent-bootstrap.jar
     * │
     * └───jackson
     *     │   jackson.jar
     */
    public static List<File> extractNestedBootStrapJar(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            String nestedBootStrapJarsPath = jarFile.getManifest().getMainAttributes().getValue(NESTED_BOOTSTRAP_JARS_PATH);
            if (nestedBootStrapJarsPath == null || nestedBootStrapJarsPath.isEmpty()) {
                return Collections.emptyList();
            }
            String[] jarPaths = nestedBootStrapJarsPath.split(DELIMITER);
            List<File> jarFiles = new ArrayList<>(jarPaths.length);
            for (String jarPath : jarPaths) {
                JarEntry jarEntry = jarFile.getJarEntry(jarPath);
                if (jarEntry.getName().endsWith(JAR_SUFFIX)) {
                    jarFiles.add(extractNestedBootStrapJar(jarFile, jarEntry, jarEntry.getName()));
                }
            }
            return jarFiles;
        } catch (IOException e) {
            System.err.printf("extract nested bootstrap jar failed, file: %s%n", file.getAbsolutePath());
            return Collections.emptyList();
        }
    }

    private static File extractNestedBootStrapJar(JarFile file, JarEntry entry, String entryName) throws IOException {
        String fileName = new File(entryName).getName();
        File outputFile = createFile(TEMP_DIR_BOOTSTRAP + File.separator + fileName);
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
}

package io.arex.agent;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.*;

class JarUtilsTest {
    private static File jarFile = null;
    private static String outputJarPath = null;

    @BeforeAll
    static void setUp() throws IOException {
        outputJarPath = Files.createTempDirectory("arex").toAbsolutePath().toString();
        File outputJarFile = createFile(outputJarPath ,"/output.jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(new Attributes.Name("Nested-BootStrap-Jars-Path"), "bootstrap" + File.separator + "bootstrap-test.jar");

        try (FileOutputStream fos = new FileOutputStream(outputJarFile);
             JarOutputStream jos = new JarOutputStream(fos, manifest)) {
            File internalJarFile = createFile(outputJarPath ,"/third-party/internal-test.jar");
            File internalBootstrapJarFile = createFile(outputJarPath ,"/bootstrap/bootstrap-test.jar");
            addFileToJar(internalBootstrapJarFile, jos);
            addFileToJar(internalJarFile, jos);
            internalJarFile.delete();
            internalBootstrapJarFile.delete();
            jarFile = outputJarFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void tearDown() {
        jarFile.deleteOnExit();
    }

    private static void addFileToJar(File file, JarOutputStream jos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            JarEntry jarEntry = new JarEntry(file.getAbsolutePath().substring(outputJarPath.length() + 1));
            jos.putNextEntry(jarEntry);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                jos.write(buffer, 0, bytesRead);
            }
        }
    }

    private static File createFile(String path, String name) {
        try {
            File file = new File(path + name);
            if (!file.getParentFile().exists()) {
                boolean mkdirs = file.getParentFile().mkdirs();
            }
            boolean newFile = file.createNewFile();
            return file;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Test
    void extractInnerJar() {
        List<File> fileList = JarUtils.extractNestedBootStrapJar(jarFile);
        assertEquals(1, fileList.size());
        assertTrue(fileList.get(0).getName().endsWith("bootstrap-test.jar"));
    }
}

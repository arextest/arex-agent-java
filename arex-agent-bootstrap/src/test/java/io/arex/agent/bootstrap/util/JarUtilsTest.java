package io.arex.agent.bootstrap.util;

import io.arex.agent.bootstrap.CreateFileCommon;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class JarUtilsTest {
    @AfterAll
    static void tearDown() {
        CreateFileCommon.clear();
    }

    @Test
    void extractNestedJar() throws IOException {
        JarFile tempJarFile = new JarFile(CreateFileCommon.getJarInJarFile());
        JarEntry jarEntry = tempJarFile.getJarEntry("internal-test.jar");
        File extractNestedJar = JarUtils.extractNestedJar(tempJarFile, jarEntry, jarEntry.getName());
        assertNotNull(extractNestedJar);
        assertTrue(extractNestedJar.getName().endsWith("internal-test.jar"));
    }

    @Test
    void appendToClassLoaderSearch() {
        assertDoesNotThrow(() -> JarUtils.appendToClassLoaderSearch(Thread.currentThread().getContextClassLoader(), CreateFileCommon.getJarInJarFile()));
    }
}

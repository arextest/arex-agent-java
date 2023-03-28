package io.arex.agent.bootstrap.util;

import static org.junit.jupiter.api.Assertions.*;
import io.arex.agent.bootstrap.InstrumentationHolder;
import io.arex.agent.bootstrap.cache.AdviceInjectorCache;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static io.arex.agent.bootstrap.CreateFileCommon.*;

class AdviceClassesCollectorTest {
    private static File zipFile = null;

    @BeforeAll
    static void setUp() throws Exception {
        zipFile = getZipFile();

        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);

        ClassLoader urlClassLoader = AdviceClassesCollectorTest.class.getClassLoader();

        if (!(urlClassLoader instanceof URLClassLoader)) {
            urlClassLoader = new URLClassLoader(new URL[] {zipFile.toURI().toURL()}, urlClassLoader);
        }

        Object invoke = addURL.invoke(urlClassLoader, zipFile.toURI().toURL());
        InstrumentationHolder.setAgentClassLoader(urlClassLoader);

    }

    @AfterAll
    static void tearDown() {
        zipFile.deleteOnExit();
        zipFile = null;
    }

    @Test
    void testAddJarToLoaderSearch() {
        AdviceClassesCollector.INSTANCE.addJarToLoaderSearch(zipFile);
        assertTrue(AdviceInjectorCache.contains("io.arex.inst.ArexTest"));
    }

    @Test
    void testAddJarToLoaderSearchThrowable() {
        assertDoesNotThrow(() -> AdviceClassesCollector.INSTANCE.addJarToLoaderSearch(new File("name")));
    }

    @Test
    void testAddClassToLoaderSearch() {
        AdviceClassesCollector.INSTANCE.addClassToLoaderSearch(AdviceInjectorCache.class);
        assertTrue(AdviceInjectorCache.contains(AdviceInjectorCache.class.getName()));
        assertTrue(AdviceInjectorCache.contains(AdviceInjectorCache.AdviceClassInjector.class.getName()));
    }

    @Test
    void testAddClassToLoaderSearchThrowable() {
        try (MockedStatic<InstrumentationHolder> mockStatic = Mockito.mockStatic(InstrumentationHolder.class)) {
            Mockito.when(InstrumentationHolder.getAgentClassLoader()).thenThrow(new RuntimeException());
            assertDoesNotThrow(() -> AdviceClassesCollector.INSTANCE.addClassToLoaderSearch(AdviceClassesCollectorTest.class));
        }
    }

    @Test
    void testNull() {
        assertDoesNotThrow(() -> AdviceClassesCollector.INSTANCE.addJarToLoaderSearch(null));
        assertDoesNotThrow(() -> AdviceClassesCollector.INSTANCE.addClassToLoaderSearch(null));
    }
}
package io.arex.agent.bootstrap.util;

import io.arex.agent.bootstrap.CreateFileCommon;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


class ServiceLoaderTest {
    private static File zipFile = null;
    private static File zipExtensionFile = null;
    @BeforeAll
    static void setUp() {
        zipFile = CreateFileCommon.getZipFile();
        zipExtensionFile = CreateFileCommon.getZipExtensionFile();
    }

    @AfterAll
    static void tearDown() {
        CreateFileCommon.clear();
        Mockito.clearAllCaches();
        zipFile = null;
        zipExtensionFile = null;
        ServiceLoader.SERVICE_CACHE.clear();
    }

    @Test
    void buildCache() {
        AdviceClassesCollector.INSTANCE.addJarToLoaderSearch(zipFile);
        AdviceClassesCollector.INSTANCE.addJarToLoaderSearch(zipExtensionFile);
        Assertions.assertNotNull(ServiceLoader.SERVICE_CACHE);

        // error
        Assertions.assertDoesNotThrow(() -> ServiceLoader.buildCache(null, null, null));
    }

    @Test
    void load() {
        // no service class
        final List<TestStringSerializable> serializableList = ServiceLoader.load(TestStringSerializable.class);
        Assertions.assertEquals(0, serializableList.size());

        // a class
        AdviceClassesCollector.INSTANCE.addJarToLoaderSearch(zipFile);
        final List<SpiTestInterface> serializableList2 = ServiceLoader.load(SpiTestInterface.class);
        Assertions.assertNotNull(serializableList2);
        Assertions.assertTrue(serializableList2.get(0) instanceof TestStringSerializable);

        // throw error
        ServiceLoader.SERVICE_CACHE.get(SpiTestInterface.class.getName()).add("errorTest");
        Assertions.assertDoesNotThrow(() -> ServiceLoader.load(SpiTestInterface.class));
    }

    public static class TestStringSerializable implements SpiTestInterface {
    }

    interface SpiTestInterface {
    }

}
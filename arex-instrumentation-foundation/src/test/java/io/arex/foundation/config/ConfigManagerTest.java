package io.arex.foundation.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigManagerTest {
   static ConfigManager configManager = null;

    @BeforeAll
    static void setUp() throws URISyntaxException {
        configManager = ConfigManager.INSTANCE;
    }

    @AfterAll
    static void tearDown() {
        configManager = null;
    }

    @Test
    void initFromSystemPropertyTest() {
        System.setProperty("arex.service.name", "test-your-service");
        System.setProperty("arex.storage.service.host", "test-storage-service.host ");

        configManager.init();

        assertEquals("test-your-service", configManager.getServiceName());
        assertEquals("test-storage-service.host", configManager.getStorageServiceHost());
    }

    @Test
    void readConfigFromFile() throws URISyntaxException {
        URL configPathResource = ConfigManagerTest.class.getClassLoader().getResource("arex.agent.conf");

        configManager.readConfigFromFile(new File(configPathResource.toURI()).getAbsolutePath());

        assertEquals("test-your-service-config-path", configManager.getServiceName());
        assertEquals("test-storage-service.host-config-path", configManager.getStorageServiceHost());
    }

    @Test
    void setDisabledInstrumentationModules() {
        configManager.setDisabledInstrumentationModules(null);
        assertTrue(configManager.getDisabledInstrumentationModules().isEmpty());
        configManager.setDisabledInstrumentationModules("mock");
        assertTrue(!configManager.getDisabledInstrumentationModules().isEmpty());
    }

    @Test
    void setExcludeServiceOperations() {
        configManager.setExcludeServiceOperations("");
        assertTrue(configManager.getExcludeServiceOperations().isEmpty());
        configManager.setExcludeServiceOperations("mock");
        assertTrue(!configManager.getExcludeServiceOperations().isEmpty());
        Set<String> excludeOperations = new HashSet<>();
        configManager.setExcludeServiceOperations(excludeOperations);
        excludeOperations.add("mock");
        configManager.setExcludeServiceOperations(excludeOperations);
        assertTrue(!configManager.getExcludeServiceOperations().isEmpty());
    }
}
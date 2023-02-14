package io.arex.foundation.config;

import io.arex.foundation.services.ConfigService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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
        System.setProperty("arex.disable.replay", "true");
        configManager.init();

        assertEquals("test-your-service", configManager.getServiceName());
        assertEquals("test-storage-service.host", configManager.getStorageServiceHost());
        assertTrue(configManager.disableReplay());
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

    @ParameterizedTest
    @MethodSource("invalidCase")
    void invalid(Runnable mocker, Predicate<Boolean> predicate) {
        mocker.run();
        assertTrue(predicate.test(configManager.invalid()));
    }

    static Stream<Arguments> invalidCase() {
        Runnable mocker1 = () -> {
            configManager.setStorageServiceMode(ConfigConstants.STORAGE_MODE);
        };
        Runnable mocker2 = () -> {
            configManager.setStorageServiceMode("");
            configManager.setAllowDayOfWeeks(0);
        };
        Runnable mocker3 = () -> {
            configManager.setAllowDayOfWeeks(127);
            configManager.setAllowTimeOfDayFrom("00:00");
            configManager.setAllowTimeOfDayTo("00:00");
        };
        Runnable mocker4 = () -> {
            configManager.setAllowTimeOfDayFrom("00:01");
            configManager.setAllowTimeOfDayTo("23:59");
            configManager.setTargetAddress("mock");
        };
        Runnable mocker5 = () -> {
            configManager.setTargetAddress(null);
        };

        Predicate<Boolean> predicate1 = result -> !result;
        Predicate<Boolean> predicate2 = result -> result;
        return Stream.of(
                arguments(mocker1, predicate1),
                arguments(mocker2, predicate2),
                arguments(mocker3, predicate2),
                arguments(mocker4, predicate2),
                arguments(mocker5, predicate1)
        );
    }

    @Test
    void parseServiceConfig() {
        ConfigService.ResponseBody serviceConfig = new ConfigService.ResponseBody();
        ConfigService.ServiceCollectConfig serviceCollect = new ConfigService.ServiceCollectConfig();
        serviceConfig.setServiceCollectConfiguration(serviceCollect);
        configManager.parseServiceConfig(serviceConfig);
        assertNull(serviceConfig.getDynamicClassConfigurationList());
    }
}
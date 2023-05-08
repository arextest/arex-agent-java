package io.arex.foundation.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    @MethodSource("validCase")
    void valid(Runnable mocker, Predicate<Boolean> predicate) {
        mocker.run();
        assertTrue(predicate.test(configManager.valid()));
    }

    static Stream<Arguments> validCase() {
        Runnable mocker1 = () -> {
            configManager.setStorageServiceMode(ConfigConstants.STORAGE_MODE);
        };
        Runnable mocker2 = () -> {
            configManager.setStorageServiceMode("xxx");
        };
        Runnable mocker3 = () -> {
            configManager.setTargetAddress("mock");
        };

        Predicate<Boolean> predicate1 = result -> result;
        Predicate<Boolean> predicate2 = result -> !result;
        return Stream.of(
                arguments(mocker1, predicate1),
                arguments(mocker2, predicate2),
                arguments(mocker3, predicate2)
        );
    }

    @Test
    void parseServiceConfig() {
        ConfigQueryResponse.ResponseBody serviceConfig = new ConfigQueryResponse.ResponseBody();
        ConfigQueryResponse.ServiceCollectConfig serviceCollect = new ConfigQueryResponse.ServiceCollectConfig();
        serviceConfig.setServiceCollectConfiguration(serviceCollect);
        Map<String, String> extendField = new HashMap<>();
        extendField.put("key", "val");
        serviceConfig.setExtendField(extendField);
        configManager.parseServiceConfig(serviceConfig);
        assertNull(serviceConfig.getDynamicClassConfigurationList());
    }

    @Test
    void parseAgentConfig() {
        configManager.parseAgentConfig("arex.storage.mode=xxx;arex.enable.debug=true");
        assertTrue(configManager.isEnableDebug());
    }

    @Test
    void inWorkingTime() {
        configManager.setAllowDayOfWeeks(0);
        boolean actualResult = configManager.inWorkingTime();
        assertFalse(actualResult);

        char[] weeks = new char[] {'0','0','1','1','1','1','0'};
        int week = LocalDate.now().getDayOfWeek().getValue();
        weeks[week - 1] = '0';
        configManager.setAllowDayOfWeeks(Integer.parseInt(String.valueOf(weeks), 2));
        actualResult = configManager.inWorkingTime();
        assertFalse(actualResult);

        LocalTime localTime = LocalTime.now();
        if (localTime.isAfter(LocalTime.of(0, 1)) && localTime.isBefore(LocalTime.of(23, 57))) {
            configManager.setAllowDayOfWeeks(127);
            configManager.setAllowTimeOfDayFrom(LocalTime.now().minusMinutes(2).format(DateTimeFormatter.ofPattern("HH:mm")));
            configManager.setAllowTimeOfDayTo("23:59");
            actualResult = configManager.inWorkingTime();
            assertTrue(actualResult);

            configManager.setAllowTimeOfDayFrom(LocalTime.now().plusMinutes(2).format(DateTimeFormatter.ofPattern("HH:mm")));
            actualResult = configManager.inWorkingTime();
            assertFalse(actualResult);

            configManager.setAllowTimeOfDayFrom(LocalTime.now().minusMinutes(3).format(DateTimeFormatter.ofPattern("HH:mm")));
            configManager.setAllowTimeOfDayTo(LocalTime.now().minusMinutes(2).format(DateTimeFormatter.ofPattern("HH:mm")));
            actualResult = configManager.inWorkingTime();
            assertFalse(actualResult);
        }
    }
}

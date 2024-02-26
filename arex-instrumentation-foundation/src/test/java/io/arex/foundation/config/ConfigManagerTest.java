package io.arex.foundation.config;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.model.ConfigQueryResponse;
import io.arex.foundation.model.ConfigQueryResponse.DynamicClassConfiguration;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.runtime.model.DynamicClassStatusEnum;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {
   static ConfigManager configManager = null;

    @BeforeAll
    static void setUp() {
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
        System.setProperty(ConfigConstants.DISABLE_REPLAY, "true");
        configManager.init();

        assertEquals("test-your-service", configManager.getServiceName());
        assertEquals("test-storage-service.host", configManager.getStorageServiceHost());
        assertEquals("test-storage-service.host", configManager.getConfigServiceHost());

        System.setProperty("arex.config.service.host", "test-config-service.host");
        configManager.init();
        assertEquals("test-config-service.host", configManager.getConfigServiceHost());
        assertEquals(1024, configManager.getBufferSize());
    }

    @Test
    void readConfigFromFile() throws URISyntaxException {
        URL configPathResource = ConfigManagerTest.class.getClassLoader().getResource("arex.agent.conf");

        configManager.readConfigFromFile(new File(configPathResource.toURI()).getAbsolutePath());
        configManager.setRecordRate(-1);

        assertEquals("test-your-service-config-path", configManager.getServiceName());
        assertEquals("test-storage-service.host-config-path", configManager.getStorageServiceHost());
    }

    @Test
    void setDisabledInstrumentationModules() {
        configManager.setDisabledModules(null);
        assertTrue(configManager.getDisabledModules().isEmpty());
        configManager.setDisabledModules("mock");
        assertFalse(configManager.getDisabledModules().isEmpty());
    }

    @Test
    void setExcludeServiceOperations() {
        configManager.setExcludeServiceOperations("");
        assertTrue(configManager.getExcludeServiceOperations().isEmpty());
        configManager.setExcludeServiceOperations("mock");
        assertFalse(configManager.getExcludeServiceOperations().isEmpty());
        Set<String> excludeOperations = new HashSet<>();
        configManager.setExcludeServiceOperations(excludeOperations);
        excludeOperations.add("mock");
        configManager.setExcludeServiceOperations(excludeOperations);
        assertFalse(configManager.getExcludeServiceOperations().isEmpty());
    }

    @Test
    void replaceConfigFromService() {
        ConfigQueryResponse.ResponseBody serviceConfig = new ConfigQueryResponse.ResponseBody();
        ConfigQueryResponse.ServiceCollectConfig serviceCollect = new ConfigQueryResponse.ServiceCollectConfig();
        serviceConfig.setServiceCollectConfiguration(serviceCollect);
        Map<String, String> extendField = new HashMap<>();
        extendField.put("key", "val");
        serviceConfig.setExtendField(extendField);
        configManager.updateConfigFromService(serviceConfig);
        assertNull(serviceConfig.getDynamicClassConfigurationList());
        assertTrue(StringUtil.isEmpty(configManager.getMessage()));
        assertFalse(configManager.isAgentEnabled());

        // set agent enabled and message
        serviceConfig.setAgentEnabled(true);
        serviceConfig.setMessage("test message");
        configManager.updateConfigFromService(serviceConfig);
        assertTrue(configManager.isAgentEnabled());
        assertEquals("test message", configManager.getMessage());
    }

    @Test
    void parseAgentConfig() {
        // empty
        configManager.parseAgentConfig("");
        // normal
        configManager.parseAgentConfig("arex.storage.mode=xxx;arex.enable.debug=true;arex.disable.instrumentation.module=dynamic;arex.buffer.size=1024;arex.serializer.config=");
        assertTrue(configManager.isEnableDebug());
    }

    @Test
    void inWorkingTime() {
        configManager.setAllowDayOfWeeks(0);
        boolean actualResult = configManager.inWorkingTime();
        assertFalse(actualResult);

        char[] weeks = new char[] {'1','1','1','1','1','1','1'};
        int week = LocalDate.now().getDayOfWeek().getValue();
        // incoming in reverse order ex: today is Monday, so the week is 1, and the index of array is 6
        weeks[weeks.length - week] = '0';
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

    @Test
    void setDynamicClassList() {
        // new list is null
        configManager.setDynamicClassList(null);
        assertEquals(0, configManager.getDynamicClassList().size());

        // add new list
        List<DynamicClassConfiguration> newList = new ArrayList<>();
        DynamicClassConfiguration classAMethodA = new DynamicClassConfiguration();
        classAMethodA.setFullClassName("ClassA");
        classAMethodA.setMethodName("MethodA");
        newList.add(classAMethodA);
        updateDynamicClassAndPrint(newList);
        assertEquals(0, configManager.getResetClassSet().size());
        assertEquals(1, configManager.getDynamicClassList().size());
        assertEquals(DynamicClassStatusEnum.RETRANSFORM, configManager.getDynamicClassList().get(0).getStatus());

        // not change list
        updateDynamicClassAndPrint(newList);
        assertEquals(0, configManager.getResetClassSet().size());
        assertEquals(1, configManager.getDynamicClassList().size());
        assertEquals(DynamicClassStatusEnum.UNCHANGED, configManager.getDynamicClassList().get(0).getStatus());

        // add new dynamic and class is new
        DynamicClassConfiguration classBMethodA = new DynamicClassConfiguration();
        classBMethodA.setFullClassName("ClassB");
        classBMethodA.setMethodName("MethodA");
        newList.add(classBMethodA);
        updateDynamicClassAndPrint(newList);
        assertEquals(0, configManager.getResetClassSet().size());
        assertEquals(2, configManager.getDynamicClassList().size());
        assertEquals(DynamicClassStatusEnum.UNCHANGED, configManager.getDynamicClassList().get(0).getStatus());
        assertEquals(DynamicClassStatusEnum.RETRANSFORM, configManager.getDynamicClassList().get(1).getStatus());

        // add new dynamic and class is exist in old dynamic class list
        DynamicClassConfiguration classAMethodB = new DynamicClassConfiguration();
        classAMethodB.setFullClassName("ClassA");
        classAMethodB.setMethodName("MethodB");
        newList.add(classAMethodB);
        updateDynamicClassAndPrint(newList);
        assertEquals(0, configManager.getResetClassSet().size());
        assertEquals(3, configManager.getDynamicClassList().size());
        assertEquals(DynamicClassStatusEnum.RETRANSFORM, configManager.getDynamicClassList().get(0).getStatus());
        assertEquals(DynamicClassStatusEnum.UNCHANGED, configManager.getDynamicClassList().get(1).getStatus());
        assertEquals(DynamicClassStatusEnum.RETRANSFORM, configManager.getDynamicClassList().get(2).getStatus());

        // remove dynamic and class is exist in new dynamic class list
        newList.remove(classAMethodB);
        updateDynamicClassAndPrint(newList);
        assertEquals(0, configManager.getResetClassSet().size());
        assertEquals(2, configManager.getDynamicClassList().size());
        assertEquals(DynamicClassStatusEnum.RETRANSFORM, configManager.getDynamicClassList().get(0).getStatus());
        assertEquals(DynamicClassStatusEnum.UNCHANGED, configManager.getDynamicClassList().get(1).getStatus());

        // remove dynamic and class is not exist in new dynamic class list
        newList.remove(classAMethodA);
        updateDynamicClassAndPrint(newList);
        assertEquals(1, configManager.getResetClassSet().size());
        assertEquals(1, configManager.getDynamicClassList().size());
        assertEquals(DynamicClassStatusEnum.UNCHANGED, configManager.getDynamicClassList().get(0).getStatus());

        // remove all dynamic
        newList.clear();
        updateDynamicClassAndPrint(newList);
        assertEquals(1, configManager.getResetClassSet().size());
        assertEquals(0, configManager.getDynamicClassList().size());
    }

    private void updateDynamicClassAndPrint(List<DynamicClassConfiguration> newList) {
        System.out.println("current dynamic class as follows: ");
        for (DynamicClassEntity entity : configManager.getDynamicClassList()) {
            System.out.printf("clazzName: %s, methodName: %s\n", entity.getClazzName(), entity.getOperation());
        }

        System.out.println("new dynamic class as follows: ");
        for (DynamicClassConfiguration entity : newList) {
            System.out.printf("clazzName: %s, methodName: %s\n", entity.getFullClassName(), entity.getMethodName());
        }
        configManager.setDynamicClassList(newList);

        System.out.println("after set current dynamic class status as follows: ");
        for (DynamicClassEntity entity : configManager.getDynamicClassList()) {
            System.out.printf("clazzName: %s, methodName: %s, status: %s\n", entity.getClazzName(), entity.getOperation(), entity.getStatus());
        }
        System.out.println();
    }

    @Test
    void setDynamicClassListWithKeyFormula() throws Exception {
        DynamicClassConfiguration dynamicClassConfiguration1 = new DynamicClassConfiguration();
        dynamicClassConfiguration1.setFullClassName("class1");
        dynamicClassConfiguration1.setMethodName("method1");
        dynamicClassConfiguration1.setKeyFormula("$1");

        DynamicClassConfiguration dynamicClassConfiguration2 = new DynamicClassConfiguration();
        dynamicClassConfiguration2.setFullClassName("class2");
        dynamicClassConfiguration2.setMethodName("method2");

        DynamicClassConfiguration dynamicClassConfiguration3 = new DynamicClassConfiguration();
        dynamicClassConfiguration3.setFullClassName("class3");
        dynamicClassConfiguration3.setMethodName("method3");
        dynamicClassConfiguration3.setKeyFormula("$1 + $2");

        DynamicClassConfiguration dynamicClassConfiguration4 = new DynamicClassConfiguration();
        dynamicClassConfiguration4.setFullClassName("class4");
        dynamicClassConfiguration4.setMethodName("method4");
        dynamicClassConfiguration4.setKeyFormula(ArexConstants.UUID_SIGNATURE);

        DynamicClassConfiguration dynamicClassConfiguration5 = new DynamicClassConfiguration();
        dynamicClassConfiguration5.setFullClassName("class5");
        dynamicClassConfiguration5.setMethodName("method5");
        dynamicClassConfiguration5.setKeyFormula(ArexConstants.UUID_SIGNATURE + "," + ArexConstants.CURRENT_TIME_MILLIS_SIGNATURE);

        final Constructor<ConfigManager> constructor = ConfigManager.class.getDeclaredConstructor(null);
        constructor.setAccessible(true);
        ConfigManager newConfigManager = constructor.newInstance(null);
        newConfigManager.setDynamicClassList(
                Arrays.asList(dynamicClassConfiguration1, dynamicClassConfiguration2, dynamicClassConfiguration3, dynamicClassConfiguration4, dynamicClassConfiguration5));
        assertEquals(6, newConfigManager.getDynamicClassList().size());
        assertEquals("class1", newConfigManager.getDynamicClassList().get(0).getClazzName());
        assertEquals("method1", newConfigManager.getDynamicClassList().get(0).getOperation());
        assertEquals("$1", newConfigManager.getDynamicClassList().get(0).getAdditionalSignature());

        assertEquals("class2", newConfigManager.getDynamicClassList().get(1).getClazzName());
        assertEquals("method2", newConfigManager.getDynamicClassList().get(1).getOperation());
        assertNull(newConfigManager.getDynamicClassList().get(1).getAdditionalSignature());

        assertEquals("class3", newConfigManager.getDynamicClassList().get(2).getClazzName());
        assertEquals("method3", newConfigManager.getDynamicClassList().get(2).getOperation());
        assertEquals("$1 + $2", newConfigManager.getDynamicClassList().get(2).getAdditionalSignature());

        assertEquals("class4", newConfigManager.getDynamicClassList().get(3).getClazzName());
        assertEquals("method4", newConfigManager.getDynamicClassList().get(3).getOperation());
        assertEquals(ArexConstants.UUID_SIGNATURE, newConfigManager.getDynamicClassList().get(3).getAdditionalSignature());

        assertEquals("class5", newConfigManager.getDynamicClassList().get(4).getClazzName());
        assertEquals("method5", newConfigManager.getDynamicClassList().get(4).getOperation());
        assertEquals(ArexConstants.UUID_SIGNATURE, newConfigManager.getDynamicClassList().get(4).getAdditionalSignature());

        assertEquals("class5", newConfigManager.getDynamicClassList().get(5).getClazzName());
        assertEquals("method5", newConfigManager.getDynamicClassList().get(5).getOperation());
        assertEquals(ArexConstants.CURRENT_TIME_MILLIS_SIGNATURE, newConfigManager.getDynamicClassList().get(5).getAdditionalSignature());
    }

    @Test
    void appendCoveragePackages() throws Exception {
        // collectCoveragePackages is empty
        Method appendCoveragePackages = ConfigManager.class.getDeclaredMethod("appendCoveragePackages", String.class);
        appendCoveragePackages.setAccessible(true);
        appendCoveragePackages.invoke(configManager, "");
        assertNull(System.getProperty(ConfigConstants.COVERAGE_PACKAGES));

        // defaultPackages is empty
        appendCoveragePackages.invoke(configManager, "com.a.b");
        assertEquals("com.a.b", System.getProperty(ConfigConstants.COVERAGE_PACKAGES));

        // defaultPackage is com.a.b
        System.setProperty(ConfigConstants.COVERAGE_PACKAGES, "com.a.c");
        appendCoveragePackages.invoke(configManager, "com.a.b");
        assertEquals("com.a.c,com.a.b", System.getProperty(ConfigConstants.COVERAGE_PACKAGES));
    }
}

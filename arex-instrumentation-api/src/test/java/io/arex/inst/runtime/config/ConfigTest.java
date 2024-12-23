package io.arex.inst.runtime.config;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.util.ConcurrentHashSet;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.EventProcessor;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.DynamicClassEntity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ConfigTest {
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(EventProcessor.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("invalidCase")
    void invalidRecord(Runnable mocker, Predicate<Boolean> predicate) {
        mocker.run();
        assertTrue(predicate.test(Config.get().invalidRecord("mock")));
    }

    static Stream<Arguments> invalidCase() {
        ConfigBuilder config = ConfigBuilder.create("mock");
        Runnable mocker1 = () -> {
            Mockito.when(EventProcessor.dependencyInitComplete()).thenReturn(true);
            config.addProperty(ConfigConstants.STORAGE_SERVICE_MODE, "local").build();;
        };
        Runnable mocker2 = () -> {
            Mockito.when(EventProcessor.dependencyInitComplete()).thenReturn(true);
            config.addProperty(ConfigConstants.STORAGE_SERVICE_MODE, "").build();;
            config.recordRate(0).build();
        };
        Runnable mocker3 = () -> {
            Mockito.when(EventProcessor.dependencyInitComplete()).thenReturn(true);
            config.recordRate(1).build();
        };
        Runnable mocker4 = () -> {
            Mockito.when(EventProcessor.dependencyInitComplete()).thenReturn(true);
            config.addProperty(ConfigConstants.DURING_WORK, "true").build();;
        };
        Runnable mocker5 = () -> {
            Mockito.when(EventProcessor.dependencyInitComplete()).thenReturn(true);
            config.addProperty(ConfigConstants.AGENT_ENABLED, "true").build();
            RecordLimiter.init(mock -> true);
        };
        Runnable disableRecord = () -> {
            Mockito.when(EventProcessor.dependencyInitComplete()).thenReturn(true);
            config.addProperty(ConfigConstants.DISABLE_RECORD, "true").build();;
            RecordLimiter.init(mock -> true);
        };

        Runnable dependencyNotInitComplete = () -> {
            Mockito.when(EventProcessor.dependencyInitComplete()).thenReturn(false);
            config.build();;
        };

        Predicate<Boolean> assertFalse = result -> !result;
        Predicate<Boolean> assertTrue = result -> result;
        return Stream.of(
            arguments(mocker1, assertFalse),
            arguments(mocker2, assertTrue),
            arguments(mocker3, assertTrue),
            arguments(mocker4, assertTrue),
            arguments(mocker5, assertFalse),
            arguments(disableRecord, assertTrue),
            arguments(dependencyNotInitComplete, assertTrue)
        );
    }

    @Test
    void testDynamicClassList() {
        ConfigBuilder.create("mock").dynamicClassList(null).build();
        assertNull(Config.get().getDynamicClassList());

        ConfigBuilder config = ConfigBuilder.create("mock").addProperty("arex.agent.version", "0.3.4");
        config.addProperty("includeServiceOperations", "operation1,operation2,operation3");
        String genericObject = "innerTest";
        String genericTypeName = genericObject.getClass().getName();
        DynamicClassEntity genericEntity = new DynamicClassEntity("classA", "methodA", null, "T:" + genericTypeName);
        DynamicClassEntity uuidEntity = new DynamicClassEntity(null, null, null, ArexConstants.UUID_SIGNATURE);
        DynamicClassEntity systemEntity = new DynamicClassEntity(null, null, null, ArexConstants.CURRENT_TIME_MILLIS_SIGNATURE);
        DynamicClassEntity abstractEntity = new DynamicClassEntity("ac:classB", "methodB", null, "T:" + genericTypeName);
        List<DynamicClassEntity> dynamicClassEntities = Arrays.asList(systemEntity, genericEntity, uuidEntity, abstractEntity);
        config.dynamicClassList(dynamicClassEntities).build();
        Assertions.assertEquals(4, Config.get().getDynamicClassList().size());
        Assertions.assertEquals(genericTypeName, Config.get().getDynamicEntity(genericEntity.getSignature()).getActualType());

        assertEquals("mock", Config.get().getServiceName());
        assertEquals("0.3.4", Config.get().getRecordVersion());
        assertEquals("0.3.4", Config.get().getString("arex.agent.version"));
        assertEquals(3, Config.get().getIncludeServiceOperations().size());
        assertTrue(Config.get().getIncludeServiceOperations().contains("operation1"));
        assertTrue(Config.get().getIncludeServiceOperations().contains("operation2"));
        assertTrue(Config.get().getIncludeServiceOperations().contains("operation3"));
        assertEquals(0, Config.get().excludeServiceOperations().size());
        assertEquals(0, Config.get().getDubboStreamReplayThreshold());
        assertEquals(3, Config.get().getDynamicClassSignatureMap().size());
        assertEquals(1, Config.get().getDynamicAbstractClassList().length);
        assertEquals("classB", Config.get().getDynamicAbstractClassList()[0]);
    }

    @Test
    void testBuildExcludeOperation() {
        final ConfigBuilder configBuilder = ConfigBuilder.create("test");
        configBuilder.excludeServiceOperations(new HashSet<>(Arrays.asList("operation1", "operation2")));
        configBuilder.build();
        assertEquals(2, Config.get().excludeServiceOperations().size());
        assertTrue(Config.get().excludeServiceOperations() instanceof ConcurrentHashSet);
    }

    @Test
    void buildCoveragePackages() {
        ConfigBuilder configBuilder = ConfigBuilder.create("mock").addProperty(ConfigConstants.COVERAGE_PACKAGES, "io.arex.inst");
        configBuilder.build();
        assertEquals(1, Config.get().getCoveragePackages().size());
        assertEquals("io.arex.inst", Config.get().getCoveragePackages().iterator().next());
        // no config coverage package
        System.setProperty(ArexConstants.SPRING_SCAN_PACKAGES, "io.arex.inst.spring");
        ConfigBuilder.create("mock").build();
        assertEquals(1, Config.get().getCoveragePackages().size());
        assertEquals("io.arex.inst.spring", Config.get().getCoveragePackages().iterator().next());
        System.clearProperty(ArexConstants.SPRING_SCAN_PACKAGES);

    }

    @Test
    void arexMockerTags() {
        ConfigBuilder configBuilder = ConfigBuilder.create("mock");
        configBuilder.build();
        assertTrue(MapUtils.isEmpty(Config.get().getMockerTags()));
        Map<String, String> tags = new HashMap<>();
        tags.put("key1", "value1");
        tags.put("key2", "value2");
        System.setProperty(ConfigConstants.MOCKER_TAGS, StringUtil.mapToString(tags));
        configBuilder.build();
        assertEquals("value1", Config.get().getMockerTags().get("key1"));
        assertEquals("value2", Config.get().getMockerTags().get("key2"));
    }
}

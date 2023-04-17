package io.arex.inst.runtime.config;

import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.DynamicClassEntity;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ConfigTest {

    @ParameterizedTest
    @MethodSource("invalidCase")
    void invalidRecord(Runnable mocker, Predicate<Boolean> predicate) {
        mocker.run();
        assertTrue(predicate.test(Config.get().invalidRecord("mock")));
    }

    static Stream<Arguments> invalidCase() {
        ConfigBuilder config = ConfigBuilder.create("mock");
        Runnable mocker1 = () -> {
            config.enableDebug(true).build();
        };
        Runnable mocker2 = () -> {
            config.enableDebug(false).build();
        };
        Runnable mocker3 = () -> {
            config.recordRate(1).build();
        };
        Runnable mocker4 = () -> {
            config.addProperty("arex.during.work", "true").build();;
        };
        Runnable mocker5 = () -> {
            config.addProperty("arex.ip.validate", "true").build();;
            RecordLimiter.init(mock -> true);
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
    void testDynamicClassList() {
        ConfigBuilder.create("mock").dynamicClassList(null).build();
        assertNull(Config.get().getDynamicClassList());

        ConfigBuilder config = ConfigBuilder.create("mock").addProperty("arex.agent.version", "0.3.4");
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
        assertNull(Config.get().excludeServiceOperations());
        assertEquals(0, Config.get().getDubboStreamReplayThreshold());
        assertEquals(3, Config.get().getDynamicClassSignatureMap().size());
        assertEquals(1, Config.get().getDynamicAbstractClassList().length);
        assertEquals("classB", Config.get().getDynamicAbstractClassList()[0]);
    }
}

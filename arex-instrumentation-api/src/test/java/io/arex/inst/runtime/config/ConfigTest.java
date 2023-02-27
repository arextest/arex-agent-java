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
            RecordLimiter.init(mock -> true);
        };

        Predicate<Boolean> predicate1 = result -> !result;
        Predicate<Boolean> predicate2 = result -> result;
        return Stream.of(
                arguments(mocker1, predicate1),
                arguments(mocker2, predicate2),
                arguments(mocker3, predicate2),
                arguments(mocker4, predicate1)
        );
    }

    @Test
    void testDynamicClassEntities() {
        ConfigBuilder config = ConfigBuilder.create("mock");
        String genericObject = "innerTest";
        String genericTypeName = genericObject.getClass().getName();
        DynamicClassEntity genericEntity = new DynamicClassEntity(null, null, null, "T:" + genericTypeName);
        DynamicClassEntity uuidEntity = new DynamicClassEntity(null, null, null, ArexConstants.UUID_SIGNATURE);
        DynamicClassEntity systemEntity = new DynamicClassEntity(null, null, null, ArexConstants.CURRENT_TIME_MILLIS_SIGNATURE);
        List<DynamicClassEntity> dynamicClassEntities = Arrays.asList(systemEntity, genericEntity, uuidEntity);
        config.dynamicClassList(dynamicClassEntities).build();
        Assertions.assertEquals(3, Config.get().dynamicClassEntities().size());
        Assertions.assertEquals(1, Config.get().getGenericReturnTypeMapSize());
        Assertions.assertEquals(genericTypeName, Config.get().getGenericReturnType(genericEntity.getSignature()));
    }

    @Test
    void testDynamicClassEntitiesNull() {
        ConfigBuilder config = ConfigBuilder.create("mock");
        config.dynamicClassList(null).build();
        Assertions.assertEquals(0, Config.get().getGenericReturnTypeMapSize());
    }
}
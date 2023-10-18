package io.arex.inst.runtime.util.fastreflect;

import io.arex.agent.bootstrap.util.ReflectUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Executable;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MethodHolderTest {
    static FastReflectTest caller;

    @BeforeAll
    static void setUp() {
        caller = new FastReflectTest();
    }

    @AfterAll
    static void tearDown() {
        caller = null;
    }

    @ParameterizedTest
    @MethodSource("buildCase")
    void build(Executable executable, Object[] args) {
        MethodHolder<?> mh1 = MethodHolder.build(executable);
        assertNotNull(mh1.invoke(args));
    }

    static Stream<Arguments> buildCase() {
        return Stream.of(
                arguments(ReflectUtil.getMethod(FastReflectTest.class, "test1", String.class), new Object[]{caller, "mock"}),
                arguments(ReflectUtil.getMethod(FastReflectTest.class, "test2", String.class), new Object[]{caller, "mock"}),
                arguments(ReflectUtil.getMethod(FastReflectTest.class, "testStatic", String.class), new Object[]{"mock"}),
                arguments(ReflectUtil.getConstructor(FastReflectTest.class), new Object[]{})
        );
    }
}
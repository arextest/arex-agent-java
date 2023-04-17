package io.arex.agent.instrumentation;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.util.SPIUtil;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.runtime.model.DynamicClassStatusEnum;
import java.lang.instrument.Instrumentation;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class InstrumentationInstallerTest {
    static InstrumentationInstaller target;
    static ConfigManager config = ConfigManager.INSTANCE;
    static ModuleInstrumentation module;
    static TypeInstrumentation inst;
    static File agentFile;

    static Instrumentation instrumentation = ByteBuddyAgent.install();

    @BeforeAll
    static void setUp() {
        agentFile = Mockito.mock(File.class);
        target = new InstrumentationInstaller(instrumentation, agentFile, null);
        module = Mockito.mock(ModuleInstrumentation.class);
        inst = Mockito.mock(TypeInstrumentation.class);
        Mockito.when(module.matcher()).thenReturn(ElementMatchers.none());
        Mockito.when(inst.matcher()).thenReturn(ElementMatchers.none());
        Mockito.mockStatic(SPIUtil.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        config = null;
        module = null;
        inst = null;
        agentFile = null;
        instrumentation = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("transformCase")
    void transform(Runnable mocker, Predicate<ResettableClassFileTransformer> predicate) {
        mocker.run();
        assertTrue(predicate.test(target.transform()));
    }

    static Stream<Arguments> transformCase() {
        Runnable firstTransform = () -> {
            config.setEnableDebug("true");
            config.setStorageServiceMode("local");
            config.setDisabledModules("mock");
            Mockito.when(module.name()).thenReturn("mock");
            Mockito.when(SPIUtil.load(any())).thenReturn(Collections.singletonList(module));
        };
        Runnable resetAndRetransformClassEmpty = () -> {
            config.setDisabledModules("mock1");
        };
        Runnable resetAndRetransformClassNotEmpty = () -> {
            DynamicClassEntity resetEntity = new DynamicClassEntity("reset-entity", null, null, null);
            resetEntity.setStatus(DynamicClassStatusEnum.RESET);
            config.getDynamicClassList().add(resetEntity);

            DynamicClassEntity retransformEntity = new DynamicClassEntity("retransform-resetEntity", null, null, null);
            retransformEntity.setStatus(DynamicClassStatusEnum.RETRANSFORM);
            config.getDynamicClassList().add(retransformEntity);

            config.setRetransformModules("mock");

            AgentBuilder.Transformer transformer = Mockito.mock(AgentBuilder.Transformer.class);
            Mockito.when(inst.transformer()).thenReturn(transformer);
        };

        Runnable instrumentationTypesNotEmpty = () -> {
            DynamicClassEntity retransformEntity = new DynamicClassEntity("retransform-resetEntity2", null, null, null);
            retransformEntity.setStatus(DynamicClassStatusEnum.RETRANSFORM);
            config.getDynamicClassList().add(retransformEntity);

            Mockito.when(module.instrumentationTypes()).thenReturn(Collections.singletonList(inst));
        };

        Runnable methodAdvicesNotEmpty = () -> {
            DynamicClassEntity retransformEntity = new DynamicClassEntity("retransform-resetEntity3", null, null, null);
            retransformEntity.setStatus(DynamicClassStatusEnum.RETRANSFORM);
            config.getDynamicClassList().add(retransformEntity);

            Mockito.when(module.instrumentationTypes()).thenReturn(Collections.singletonList(inst));

            List<MethodInstrumentation> methodInstList = new ArrayList<>();
            MethodInstrumentation methodInst1 = Mockito.mock(MethodInstrumentation.class);
            methodInstList.add(methodInst1);
            MethodInstrumentation methodInst2 = Mockito.mock(MethodInstrumentation.class);
            methodInstList.add(methodInst2);
            Mockito.when(inst.methodAdvices()).thenReturn(methodInstList);
        };

        Predicate<ResettableClassFileTransformer> predicate_nonNull = Objects::nonNull;
        return Stream.of(
                arguments(firstTransform, predicate_nonNull),
                arguments(resetAndRetransformClassEmpty, predicate_nonNull),
                arguments(resetAndRetransformClassNotEmpty, predicate_nonNull),
                arguments(instrumentationTypesNotEmpty, predicate_nonNull),
                arguments(methodAdvicesNotEmpty, predicate_nonNull)
        );
    }

    @Test
    void onTransformation() {
        config.setEnableDebug("true");
        TransformListener listener = new TransformListener();
        TypeDescription typeDescription = Mockito.mock(TypeDescription.class);
        DynamicType dynamicType = Mockito.mock(DynamicType.class);
        listener.onTransformation(typeDescription, null, null, false, dynamicType);
        assertDoesNotThrow(() -> listener.onError(null, null,null, false, new RuntimeException()));
        config.setEnableDebug("false");
        assertDoesNotThrow(() -> listener.onError(null, null,null, false, new RuntimeException()));
    }
}

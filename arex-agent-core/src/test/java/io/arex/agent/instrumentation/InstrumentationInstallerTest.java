package io.arex.agent.instrumentation;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.util.SPIUtil;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstrumentationInstallerTest {
    static InstrumentationInstaller target;
    static ConfigManager config = ConfigManager.INSTANCE;
    static ModuleInstrumentation module;
    static TypeInstrumentation inst;
    static File agentFile;

    @BeforeAll
    static void setUp() {
        agentFile = Mockito.mock(File.class);
        target = new InstrumentationInstaller(ByteBuddyAgent.install(), agentFile, null);
        module = Mockito.mock(ModuleInstrumentation.class);
        inst = Mockito.mock(TypeInstrumentation.class);
        Mockito.mockStatic(SPIUtil.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        config = null;
        module = null;
        inst = null;
        agentFile = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("transformCase")
    void transform(Runnable mocker, Predicate<ResettableClassFileTransformer> predicate) {
        mocker.run();
        assertTrue(predicate.test(target.transform()));
    }

    static Stream<Arguments> transformCase() {
        Runnable mocker1 = () -> {
            config.setTargetAddress("mock");
        };
        Runnable mocker2 = () -> {
            config.setStorageServiceMode("local");
            config.setDisabledInstrumentationModules("mock");
            Mockito.when(module.name()).thenReturn("mock");
            Mockito.when(SPIUtil.load(any())).thenReturn(Collections.singletonList(module));
        };
        Runnable mocker3 = () -> {
            config.setDisabledInstrumentationModules("mock1");
        };
        Runnable mocker4 = () -> {
            Mockito.when(module.instrumentationTypes()).thenReturn(Collections.singletonList(inst));
            Mockito.when(inst.adviceClassNames()).thenReturn(Collections.singletonList("mock"));
            AgentBuilder.Transformer transformer = Mockito.mock(AgentBuilder.Transformer.class);
            Mockito.when(inst.transformer()).thenReturn(transformer);
        };
        Runnable mocker5 = () -> {
            List<MethodInstrumentation> methodInstList = new ArrayList<>();
            MethodInstrumentation methodInst1 = Mockito.mock(MethodInstrumentation.class);
            methodInstList.add(methodInst1);
            MethodInstrumentation methodInst2 = Mockito.mock(MethodInstrumentation.class);
            methodInstList.add(methodInst2);
            Mockito.when(inst.methodAdvices()).thenReturn(methodInstList);
        };
        Predicate<ResettableClassFileTransformer> predicate1 = Objects::isNull;
        Predicate<ResettableClassFileTransformer> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(mocker1, predicate1),
                arguments(mocker2, predicate2),
                arguments(mocker3, predicate2),
                arguments(mocker4, predicate2),
                arguments(mocker5, predicate2)
        );
    }

    @Test
    void disabledModule() {
        assertFalse(target.disabledModule("mock"));
    }

    @Test
    void onTransformation() throws IOException {
        InstrumentationInstaller.TransformListener listener = new InstrumentationInstaller.TransformListener(agentFile);
        config.setEnableDebug("true");
        listener = new InstrumentationInstaller.TransformListener(agentFile);
        TypeDescription typeDescription = Mockito.mock(TypeDescription.class);
        DynamicType dynamicType = Mockito.mock(DynamicType.class);
        listener.onTransformation(typeDescription, null, null, false, dynamicType);
        verify(dynamicType).saveIn(any());
        listener.onError(null, null,null, false, null);
    }
}
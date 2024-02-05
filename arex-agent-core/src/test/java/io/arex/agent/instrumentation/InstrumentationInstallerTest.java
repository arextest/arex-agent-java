package io.arex.agent.instrumentation;

import io.arex.foundation.config.ConfigManager;
import io.arex.inst.extension.ExtensionTransformer;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.runtime.model.DynamicClassStatusEnum;
import io.arex.agent.bootstrap.util.ServiceLoader;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class InstrumentationInstallerTest {
    static InstrumentationInstaller target;
    static ModuleInstrumentation module;
    static TypeInstrumentation inst;
    static File agentFile;

    static Instrumentation instrumentation;

    @BeforeAll
    static void setUp() {
        instrumentation = Mockito.mock(Instrumentation.class);
        Mockito.when(instrumentation.isRetransformClassesSupported()).thenReturn(true);
        Mockito.when(instrumentation.getAllLoadedClasses()).thenReturn(new Class<?>[0]);
        agentFile = Mockito.mock(File.class);
        target = new InstrumentationInstaller(instrumentation, agentFile, null);
        module = Mockito.mock(ModuleInstrumentation.class);
        inst = Mockito.mock(TypeInstrumentation.class);
        Mockito.when(module.matcher()).thenReturn(ElementMatchers.none());
        Mockito.when(inst.matcher()).thenReturn(ElementMatchers.none());
        Mockito.mockStatic(ServiceLoader.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        module = null;
        inst = null;
        agentFile = null;
        instrumentation = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("retransformCase")
    void retransform(Runnable mocker) {
        mocker.run();
        assertDoesNotThrow(() -> target.retransform());
    }

    @ParameterizedTest
    @MethodSource("transformCase")
    void transform(Runnable mocker) {
        mocker.run();
        assertDoesNotThrow(() -> target.transform());
    }

    static Stream<Arguments> retransformCase() {
        Runnable emptyClass = () -> {

        };

        Runnable resetAndRetransformClassNotEmpty = () -> {
            DynamicClassEntity resetEntity = new DynamicClassEntity("reset-entity", null, null, null);
            resetEntity.setStatus(DynamicClassStatusEnum.RESET);
            ConfigManager.INSTANCE.getResetClassSet().add(resetEntity.getClazzName());

            DynamicClassEntity retransformEntity = new DynamicClassEntity("retransform-resetEntity", null, null, null);
            retransformEntity.setStatus(DynamicClassStatusEnum.RETRANSFORM);
            ConfigManager.INSTANCE.getDynamicClassList().add(retransformEntity);

            ConfigManager.INSTANCE.setRetransformModules("mock");
            Mockito.when(module.getName()).thenReturn("mock");
            Mockito.when(ServiceLoader.load(any())).thenReturn(Collections.singletonList(module));
        };

        Runnable instrumentationTypesNotEmpty = () -> {
            DynamicClassEntity retransformEntity = new DynamicClassEntity("retransform-resetEntity2", null, null, null);
            retransformEntity.setStatus(DynamicClassStatusEnum.RETRANSFORM);
            ConfigManager.INSTANCE.getDynamicClassList().add(retransformEntity);

            Mockito.when(module.instrumentationTypes()).thenReturn(Collections.singletonList(inst));
        };

        Runnable methodAdvicesNotEmpty = () -> {
            DynamicClassEntity retransformEntity = new DynamicClassEntity("retransform-resetEntity3", null, null, null);
            retransformEntity.setStatus(DynamicClassStatusEnum.RETRANSFORM);
            ConfigManager.INSTANCE.getDynamicClassList().add(retransformEntity);

            Mockito.when(module.instrumentationTypes()).thenReturn(Collections.singletonList(inst));

            List<MethodInstrumentation> methodInstList = new ArrayList<>();
            MethodInstrumentation methodInst1 = Mockito.mock(MethodInstrumentation.class);
            methodInstList.add(methodInst1);
            MethodInstrumentation methodInst2 = Mockito.mock(MethodInstrumentation.class);
            methodInstList.add(methodInst2);
            Mockito.when(inst.methodAdvices()).thenReturn(methodInstList);
        };

        return Stream.of(
            arguments(emptyClass),
            arguments(resetAndRetransformClassNotEmpty),
            arguments(instrumentationTypesNotEmpty),
            arguments(methodAdvicesNotEmpty)
        );
    }

    static Stream<Arguments> transformCase() {
        // filtered disabled module
        Runnable disabledModule = () -> {
            ExtensionTransformer invalidExtensionTransformer = new ExtensionTransformer("mock") {
                @Override
                public boolean validate() {
                    return false;
                }

                @Override
                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                    return classfileBuffer;
                }
            };

            ConfigManager.INSTANCE.setEnableDebug("true");
            ConfigManager.INSTANCE.setStorageServiceMode("local");
            ConfigManager.INSTANCE.setDisabledModules("mock");
            Mockito.when(module.getName()).thenReturn("mock");
            Mockito.when(ServiceLoader.load(any())).thenReturn(Collections.singletonList(module));
            Mockito.when(ServiceLoader.load(any(), any())).thenReturn(Collections.singletonList(invalidExtensionTransformer));
        };

        // filtered empty instrumentation module
        Runnable emptyInstrumentations = () -> {
            ConfigManager.INSTANCE.setDisabledModules("mock1");
        };

        // normal instrumentation module
        Runnable transform = () -> {
            ExtensionTransformer extensionTransformer = new ExtensionTransformer("mock") {
                @Override
                public boolean validate() {
                    return true;
                }

                @Override
                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                    return classfileBuffer;
                }
            };
            Mockito.when(module.instrumentationTypes()).thenReturn(Collections.singletonList(inst));
            Mockito.when(ServiceLoader.load(any(), any())).thenReturn(Collections.singletonList(extensionTransformer));
        };

        return Stream.of(
            arguments(disabledModule),
            arguments(emptyInstrumentations),
            arguments(transform));
    }

    @Test
    void onTransformation() {
        ConfigManager.INSTANCE.setEnableDebug("true");
        TransformListener listener = new TransformListener();
        TypeDescription typeDescription = Mockito.mock(TypeDescription.class);
        DynamicType dynamicType = Mockito.mock(DynamicType.class);
        listener.onTransformation(typeDescription, null, null, false, dynamicType);
        assertDoesNotThrow(() -> listener.onError(null, null,null, false, new RuntimeException()));
        ConfigManager.INSTANCE.setEnableDebug("false");
        assertDoesNotThrow(() -> listener.onError(null, null,null, false, new RuntimeException()));
    }
}

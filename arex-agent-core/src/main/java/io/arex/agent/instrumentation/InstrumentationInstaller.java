package io.arex.agent.instrumentation;

import io.arex.foundation.logger.AgentLoggerFactory;
import io.arex.foundation.logger.AgentLogger;
import io.arex.inst.extension.ExtensionTransformer;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.agent.bootstrap.InstrumentationHolder;
import io.arex.foundation.config.ConfigManager;
import io.arex.agent.bootstrap.util.CollectionUtil;

import io.arex.inst.extension.matcher.IgnoredRawMatcher;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.runtime.model.DynamicClassStatusEnum;
import io.arex.agent.bootstrap.util.ServiceLoader;
import java.util.stream.Collectors;

import io.arex.inst.runtime.util.IgnoreUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.matcher.ElementMatcher;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.*;

@SuppressWarnings("unused")
public class InstrumentationInstaller extends BaseAgentInstaller {
    private static final AgentLogger LOGGER = AgentLoggerFactory.getAgentLogger(InstrumentationInstaller.class);
    private ModuleInstrumentation dynamicModule;
    private ResettableClassFileTransformer resettableClassFileTransformer;

    public InstrumentationInstaller(Instrumentation inst, File agentFile, String agentArgs) {
        super(inst, agentFile, agentArgs);
    }

    @Override
    protected void transform() {
        resettableClassFileTransformer = install(getAgentBuilder(), false);
        extensionTransform();
        LOGGER.info("[AREX] Agent first transform class successfully.");
    }

    @Override
    protected void retransform() {
        resetClass();

        List<DynamicClassEntity> retransformList = ConfigManager.INSTANCE.getDynamicClassList().stream()
            .filter(item -> DynamicClassStatusEnum.RETRANSFORM == item.getStatus()).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(retransformList)) {
            LOGGER.info("[AREX] No dynamic class need to retransform.");
            return;
        }
        IgnoreUtils.clearInvalidOperation();
        if (resettableClassFileTransformer != null) {
            instrumentation.removeTransformer(resettableClassFileTransformer);
        }
        resettableClassFileTransformer = install(getAgentBuilder(), true);
        LOGGER.info("[AREX] Agent retransform class successfully.");
    }

    private void resetClass() {
        Set<String> resetClassSet = ConfigManager.INSTANCE.getResetClassSet();
        if (CollectionUtil.isEmpty(resetClassSet)) {
            return;
        }
        IgnoreUtils.clearInvalidOperation();
        if (resettableClassFileTransformer != null) {
            // The transformer must be removed before reset will take effect.
            instrumentation.removeTransformer(resettableClassFileTransformer);
        }
        // TODO: optimize reset abstract class
        for (Class<?> clazz : this.instrumentation.getAllLoadedClasses()) {
            if (resetClassSet.contains(clazz.getName())) {
                try {
                    ClassReloadingStrategy.of(this.instrumentation).reset(clazz);
                    LOGGER.info("[arex] retransform reset class successfully, name: {}", clazz.getName());
                } catch (Exception e) {
                    LOGGER.warn("[arex] retransform reset class failed, name: {}", clazz.getName(), e);
                }
            }
        }
    }

    private void extensionTransform() {
        List<ExtensionTransformer> transformers = ServiceLoader.load(ExtensionTransformer.class, getClassLoader());
        for (ExtensionTransformer transformer : transformers) {
            if (disabledModule(transformer.getName())) {
                LOGGER.warn("[arex] filtered disabled instrumentation module: {}", transformer.getName());
                continue;
            }

            if (!transformer.validate()) {
                LOGGER.warn("[arex] filtered invalid instrumentation module: {}", transformer.getName());
                continue;
            }

            LOGGER.info("[arex] first transform instrumentation module: {}", transformer.getName());
            instrumentation.addTransformer(transformer, true);
        }
    }

    private ResettableClassFileTransformer install(AgentBuilder builder, boolean retransform) {
        List<ModuleInstrumentation> list = ServiceLoader.load(ModuleInstrumentation.class);

        for (ModuleInstrumentation module : list) {
            builder = installModule(builder, module, retransform);
        }

        return builder.installOn(this.instrumentation);
    }

    private AgentBuilder installModule(AgentBuilder builder, ModuleInstrumentation module, boolean retransform) {
        String moduleName = module.getName();
        if (disabledModule(moduleName)) {
            LOGGER.warn("[arex] filtered disabled instrumentation module: {}", moduleName);
            return builder;
        }

        if (CollectionUtil.isEmpty(module.instrumentationTypes())) {
            LOGGER.warn("[arex] filtered empty instrumentation module: {}", moduleName);
            return builder;
        }

        if (!retransform) {
            LOGGER.info("[arex] first transform instrumentation module: {}", moduleName);
            return installTypes(builder, module, module.instrumentationTypes());
        }

        if (retranformModule(moduleName)) {
            LOGGER.info("[arex] retransform instrumentation module: {}", moduleName);
            return installTypes(builder, module, module.instrumentationTypes());
        }
        return builder;
    }

    private AgentBuilder installTypes(AgentBuilder builder, ModuleInstrumentation module, List<TypeInstrumentation> types) {
        for (TypeInstrumentation inst : types) {
            builder = installType(builder, module.matcher(), inst);
        }

        return builder;
    }

    private AgentBuilder installType(AgentBuilder builder, ElementMatcher<ClassLoader> moduleMatcher,
        TypeInstrumentation type) {
        AgentBuilder.Identified identified = builder.type(type.matcher(), moduleMatcher);
        AgentBuilder.Transformer transformer = type.transformer();
        if (transformer != null) {
            identified = identified.transform(transformer);
        }

        List<MethodInstrumentation> methodAdvices = type.methodAdvices();
        if (CollectionUtil.isEmpty(methodAdvices)) {
            return (AgentBuilder) identified;
        }

        AgentBuilder.Identified.Extendable extBuilder = installMethod(identified, methodAdvices.get(0));
        for (int i = 1; i < methodAdvices.size(); i++) {
            extBuilder = installMethod(extBuilder, methodAdvices.get(i));
        }

        return extBuilder;
    }

    private AgentBuilder.Identified.Extendable installMethod(AgentBuilder.Identified builder,
        MethodInstrumentation method) {
        return builder.transform(new AgentBuilder.Transformer.ForAdvice()
                        .include(InstrumentationHolder.getAgentClassLoader())
                        .advice(method.getMethodMatcher(), method.getAdviceClassName())
                        .withExceptionHandler(Advice.ExceptionHandler.Default.PRINTING));
    }


    private AgentBuilder getAgentBuilder() {
        // config may use to add some classes to be ignored in future
        long buildBegin = System.currentTimeMillis();

        return new AgentBuilder.Default(
                new ByteBuddy().with(MethodGraph.Compiler.ForDeclaredMethods.INSTANCE))
            .enableNativeMethodPrefix("arex_")
            .disableClassFormatChanges()
            .ignore(new IgnoredRawMatcher(ConfigManager.INSTANCE.getIgnoreTypePrefixes(),
                ConfigManager.INSTANCE.getIgnoreClassLoaderPrefixes()))
            .with(new TransformListener())
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
            .with(AgentBuilder.TypeStrategy.Default.REBASE)
             // https://github.com/raphw/byte-buddy/issues/1441
            .with(AgentBuilder.DescriptionStrategy.Default.POOL_FIRST)
            .with(AgentBuilder.LocationStrategy.ForClassLoader.STRONG
                .withFallbackTo(ClassFileLocator.ForClassLoader.ofSystemLoader()));
    }

    private boolean disabledModule(String moduleName) {
        return ConfigManager.INSTANCE.getDisabledModules().contains(moduleName);
    }

    private boolean retranformModule(String moduleName) {
        return ConfigManager.INSTANCE.getRetransformModules().contains(moduleName);
    }
}

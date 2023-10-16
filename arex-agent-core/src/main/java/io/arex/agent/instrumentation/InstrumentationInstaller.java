package io.arex.agent.instrumentation;

import io.arex.agent.bootstrap.util.FileUtils;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.agent.bootstrap.InstrumentationHolder;
import io.arex.foundation.config.ConfigManager;
import io.arex.agent.bootstrap.util.CollectionUtil;

import io.arex.inst.extension.matcher.IgnoredTypesMatcher;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.runtime.model.DynamicClassStatusEnum;
import io.arex.agent.bootstrap.util.ServiceLoader;
import java.util.stream.Collectors;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.dynamic.scaffold.TypeWriter;
import net.bytebuddy.matcher.ElementMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.*;

@SuppressWarnings("unused")
public class InstrumentationInstaller extends BaseAgentInstaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentationInstaller.class);
    private static final String BYTECODE_DUMP_DIR = "/bytecode-dump";
    private ModuleInstrumentation dynamicModule;
    private ResettableClassFileTransformer resettableClassFileTransformer;

    public InstrumentationInstaller(Instrumentation inst, File agentFile, String agentArgs) {
        super(inst, agentFile, agentArgs);
    }

    @Override
    protected ResettableClassFileTransformer transform() {
        if (ConfigManager.FIRST_TRANSFORM.compareAndSet(false, true)) {
            createDumpDirectory();
            resettableClassFileTransformer = install(getAgentBuilder(), false);
            LOGGER.info("[AREX] Agent first install successfully.");
            return resettableClassFileTransformer;
        }

        resetClass();

        return retransform();
    }

    private ResettableClassFileTransformer retransform() {
        List<DynamicClassEntity> retransformList = ConfigManager.INSTANCE.getDynamicClassList().stream()
            .filter(item -> DynamicClassStatusEnum.RETRANSFORM == item.getStatus()).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(retransformList)) {
            LOGGER.info("[AREX] No Change in dynamic class config, no need to retransform.");
            return resettableClassFileTransformer;
        }

        instrumentation.removeTransformer(resettableClassFileTransformer);
        resettableClassFileTransformer = install(getAgentBuilder(), true);
        LOGGER.info("[AREX] Agent retransform successfully.");
        return resettableClassFileTransformer;
    }

    private void resetClass() {
        Set<String> resetClassSet = ConfigManager.INSTANCE.getResetClassSet();
        if (CollectionUtil.isEmpty(resetClassSet)) {
            return;
        }

        instrumentation.removeTransformer(resettableClassFileTransformer);
        // TODO: optimize reset abstract class
        for (Class<?> clazz : this.instrumentation.getAllLoadedClasses()) {
            if (resetClassSet.contains(clazz.getName())) {
                try {
                    ClassReloadingStrategy.of(this.instrumentation).reset(clazz);
                    LOGGER.info("[arex] reset class successfully, name: {}", clazz.getName());
                } catch (Exception e) {
                    LOGGER.warn("[arex] reset class failed, name: {}", clazz.getName(), e);
                }
            }
        }
    }

    private ResettableClassFileTransformer install(AgentBuilder builder, boolean retransform) {
        List<ModuleInstrumentation> list = loadInstrumentationModules();

        for (ModuleInstrumentation module : list) {
            builder = installModule(builder, module, retransform);
        }
        return builder.installOn(this.instrumentation);
    }

    private List<ModuleInstrumentation> loadInstrumentationModules() {
        return ServiceLoader.load(ModuleInstrumentation.class);
    }

    private AgentBuilder installModule(AgentBuilder builder, ModuleInstrumentation module, boolean retransform) {
        if (disabledModule(module.name())) {
            LOGGER.warn("[arex] disabled instrumentation module: {}", module.name());
            return builder;
        }

        if (retransform) {
            if (retranformModule(module.name())) {
                LOGGER.info("[arex] retransform instrumentation module: {}", module.name());
                return installTypes(builder, module, module.instrumentationTypes());
            }
            return builder;
        }

        LOGGER.info("[arex] installed instrumentation module: {}", module.name());
        return installTypes(builder, module, module.instrumentationTypes());
    }

    private AgentBuilder installTypes(AgentBuilder builder, ModuleInstrumentation module, List<TypeInstrumentation> types) {
        if (CollectionUtil.isEmpty(types)) {
            LOGGER.warn("[arex] invalid instrumentation module: {}", module.name());
            return builder;
        }

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
        AgentBuilder builder = new AgentBuilder.Default(
                new ByteBuddy().with(MethodGraph.Compiler.ForDeclaredMethods.INSTANCE))
            .enableNativeMethodPrefix("arex_")
            .disableClassFormatChanges()
            .ignore(new IgnoredTypesMatcher())
            .with(new TransformListener())
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
            .with(AgentBuilder.TypeStrategy.Default.REBASE)
             // https://github.com/raphw/byte-buddy/issues/1441
            .with(AgentBuilder.DescriptionStrategy.Default.POOL_FIRST)
            .with(AgentBuilder.LocationStrategy.ForClassLoader.STRONG
                .withFallbackTo(ClassFileLocator.ForClassLoader.ofSystemLoader()));

        return builder;
    }

    private boolean disabledModule(String moduleName) {
        return ConfigManager.INSTANCE.getDisabledModules().contains(moduleName);
    }

    private boolean retranformModule(String moduleName) {
        return ConfigManager.INSTANCE.getRetransformModules().contains(moduleName);
    }

    private void createDumpDirectory() {
        if (!ConfigManager.INSTANCE.isEnableDebug()) {
            return;
        }

        try {
            File bytecodeDumpPath = new File(agentFile.getParent(), BYTECODE_DUMP_DIR);
            if (!bytecodeDumpPath.exists()) {
                bytecodeDumpPath.mkdir();
            } else {
                FileUtils.cleanDirectory(bytecodeDumpPath);
            }
            System.setProperty(TypeWriter.DUMP_PROPERTY, bytecodeDumpPath.getPath());
        } catch (Exception e) {
            LOGGER.info("[arex] Failed to create directory to instrumented bytecode: {}", e.getMessage());
        }
    }
}

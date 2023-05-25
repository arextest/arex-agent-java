package io.arex.agent.instrumentation;

import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.agent.bootstrap.InstrumentationHolder;
import io.arex.foundation.config.ConfigManager;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.foundation.util.SPIUtil;

import io.arex.inst.extension.matcher.IgnoredTypesMatcher;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.dynamic.scaffold.TypeWriter;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.*;

@SuppressWarnings("unused")
public class InstrumentationInstaller extends BaseAgentInstaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentationInstaller.class);
    private static final String BYTECODE_DUMP_DIR = "/bytecode-dump";

    public InstrumentationInstaller(Instrumentation inst, File agentFile, String agentArgs) {
        super(inst, agentFile, agentArgs);
    }

    @Override
    protected ResettableClassFileTransformer transform() {
        createDumpDirectory();
        return install(getAgentBuilder());
    }

    private ResettableClassFileTransformer install(AgentBuilder builder) {
        List<ModuleInstrumentation> list = loadInstrumentationModules();

        for (ModuleInstrumentation module : list) {
            builder = installModule(builder, module);
        }

        ResettableClassFileTransformer transformer = builder.installOn(this.instrumentation);
        return transformer;
    }

    private List<ModuleInstrumentation> loadInstrumentationModules() {
        return SPIUtil.load(ModuleInstrumentation.class);
    }

    private AgentBuilder installModule(AgentBuilder builder, ModuleInstrumentation module) {
        if (disabledModule(module.name())) {
            LOGGER.warn("[arex] disabled instrumentation module: {}", module.name());
            return builder;
        }


        List<TypeInstrumentation> types = module.instrumentationTypes();
        if (CollectionUtil.isEmpty(types)) {
            LOGGER.warn("[arex] invalid instrumentation module: {}", module.name());
            return builder;
        }

        for (TypeInstrumentation inst : types) {
            builder = installType(builder, module.matcher(), inst);
        }
        LOGGER.info("[arex] installed instrumentation module: {}", module.name());
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
            .with(AgentBuilder.LocationStrategy.ForClassLoader.STRONG
                .withFallbackTo(ClassFileLocator.ForClassLoader.ofSystemLoader()));

        return builder;
    }

    static class TransformListener extends AgentBuilder.Listener.Adapter {
        @Override
        public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
            boolean loaded, DynamicType dynamicType) {
            LOGGER.info("[arex] onTransformation: {} loaded: {} from classLoader {}", typeDescription.getName(), loaded, classLoader);
        }

        @Override
        public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
            Throwable throwable) {
            String message = null;
            if (throwable != null) {
                if (ConfigManager.INSTANCE.isEnableDebug()) {
                    message = throwable.toString();
                } else {
                    message = throwable.getMessage();
                }
            }
            LOGGER.error("[arex] onError: {} loaded: {} from classLoader {}, throwable: {}", typeName, loaded, classLoader, message);
        }
    }

    private boolean disabledModule(String moduleName) {
        return ConfigManager.INSTANCE.getDisabledInstrumentationModules().contains(moduleName);
    }

    private void createDumpDirectory() {
        if (!ConfigManager.INSTANCE.isEnableDebug()) {
            return;
        }

        try {
            File bytecodeDumpPath = new File(agentFile.getParent(), BYTECODE_DUMP_DIR);
            if (!bytecodeDumpPath.exists()) {
                bytecodeDumpPath.mkdir();
            }
            System.setProperty(TypeWriter.DUMP_PROPERTY, bytecodeDumpPath.getPath());
        } catch (Exception e) {
            LOGGER.info("[arex] Failed to create directory to instrumented bytecode: {}", e.getMessage());
        }
    }
}

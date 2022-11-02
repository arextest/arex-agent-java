package io.arex.agent.instrumentation;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.bytebuddy.AdviceInjector;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.services.ConfigService;
import io.arex.foundation.util.CollectionUtil;
import io.arex.foundation.util.LogUtil;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.*;

import static net.bytebuddy.matcher.ElementMatchers.*;

@SuppressWarnings("unused")
public class InstrumentationInstaller extends BaseAgentInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentationInstaller.class);

    public InstrumentationInstaller(Instrumentation inst, File agentFile, String agentArgs) {
        super(inst, agentFile, agentArgs);
    }

    @Override
    protected ResettableClassFileTransformer invoke() {
        ConfigService.INSTANCE.loadAgentConfig(agentArgs);
        if (ConfigManager.INSTANCE.invalid()) {
            LogUtil.warn("config is invalid and stop instrument");
            return null;
        }
        return install(getAgentBuilder());
    }

    private ResettableClassFileTransformer install(AgentBuilder builder) {
        Thread.currentThread().setContextClassLoader(InstrumentationInstaller.class.getClassLoader());
        for (ModuleInstrumentation module : loadInstrumentationModules()) {
            builder = installModule(builder, module);
        }

        return builder.installOn(this.instrumentation);
    }

    private List<ModuleInstrumentation> loadInstrumentationModules() {
        return load(ModuleInstrumentation.class);
    }

    private AgentBuilder installModule(AgentBuilder builder, ModuleInstrumentation instrumentation) {
        if (!instrumentation.validate()) {
            LogUtil.warn("invalid instrumentation: ".concat(instrumentation.name()));
            return builder;
        }

        ElementMatcher<ClassLoader> moduleMatcher = instrumentation.matcher();
        for (TypeInstrumentation inst : instrumentation.instrumentationTypes()) {
            builder = installType(builder, moduleMatcher, inst);
        }
        LOGGER.info("[arex] module installed:{}", instrumentation.name());
        return builder;
    }

    private AgentBuilder installType(AgentBuilder builder, ElementMatcher<ClassLoader> moduleMatcher, TypeInstrumentation inst) {
        List<MethodInstrumentation> methods = inst.methodAdvices();
        if (methods == null || methods.size() == 0) {
            return builder;
        }

        AgentBuilder.Identified identified = builder.type(inst.matcher(), moduleMatcher);
        List<String> advicesClassNames = inst.adviceClassNames();
        if (!CollectionUtil.isEmpty(advicesClassNames)) {
            identified = identified.transform(new AdviceInjector(advicesClassNames));
        }

        AgentBuilder.Identified.Extendable extBuilder = installMethod(identified, methods.get(0));
        for (int i = 1; i < methods.size(); i++) {
            extBuilder = installMethod(extBuilder, methods.get(i));
        }

        return extBuilder;
    }

    private AgentBuilder.Identified.Extendable installMethod(AgentBuilder.Identified builder, MethodInstrumentation inst) {
        AgentBuilder.Identified.Extendable extendable;
        if (inst.isInterceptor()) {
            extendable = builder.transform((b, t, c, m)
                    -> b.method(inst.getMethodMatcher()).intercept(Advice.to(inst.getInterceptor())));
        } else {
            extendable = builder.transform(new AgentBuilder.Transformer.ForAdvice()
                    .include(InstrumentationInstaller.class.getClassLoader())
                    .advice(inst.getMethodMatcher(), inst.getAdviceClassName()));
        }
        LOGGER.info("[arex] type installed:{}", inst.getAdviceClassName());
        return extendable;
    }

    private AgentBuilder getAgentBuilder() {
        // config may use to add some classes to be ignored in future
        long buildBegin = System.currentTimeMillis();
        AgentBuilder builder = new AgentBuilder.Default(
                new ByteBuddy().with(MethodGraph.Compiler.ForDeclaredMethods.INSTANCE))
            .enableNativeMethodPrefix("arex_")
            .ignore(none())
            .ignore(nameStartsWith("net.bytebuddy."))
            .with(new TransformListener())
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
            .with(AgentBuilder.TypeStrategy.Default.REBASE)
            .with(AgentBuilder.LocationStrategy.ForClassLoader.STRONG
                .withFallbackTo(ClassFileLocator.ForClassLoader.ofSystemLoader()));

        // config used here to avoid warning of unused
        LOGGER.info("AgentBuilder use time: {}", (System.currentTimeMillis() - buildBegin));
        return builder;
    }

    @SuppressWarnings("ForEachIterable")
    private static <T> List<T> load(Class<T> serviceClass) {
        List<T> result = new ArrayList<>();
        java.util.ServiceLoader<T> services = ServiceLoader.load(serviceClass, InstrumentationInstaller.class.getClassLoader());
        for (Iterator<T> iter = services.iterator(); iter.hasNext(); ) {
            try {
                result.add(iter.next());
            } catch (Throwable e) {
                LOGGER.warn(String.format("Unable to load instrumentation class: %s", serviceClass.getName()), e);
            }
        }
        return result;
    }

    static class TransformListener extends AgentBuilder.Listener.Adapter {

        @Override
        public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
                                     boolean loaded, DynamicType dynamicType) {
            LOGGER.info("onTransformation: {} loaded: {} from classLoader {}", typeDescription.getName(), loaded, classLoader);
        }

        @Override
        public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
                            Throwable throwable) {
            LOGGER.error("onError: {} loaded: {} from classLoader {}, throwable: {}", typeName, loaded, classLoader, throwable);
        }
    }
}

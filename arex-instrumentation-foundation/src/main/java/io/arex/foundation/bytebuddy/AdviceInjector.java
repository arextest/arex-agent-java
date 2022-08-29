package io.arex.foundation.bytebuddy;

import io.arex.agent.bootstrap.cache.AdviceInjectorCache;
import io.arex.agent.bootstrap.InstrumentationHolder;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class AdviceInjector implements AgentBuilder.Transformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdviceInjector.class);

    private final List<String> adviceClassNames;

    public AdviceInjector(List<String> names) {
        adviceClassNames = names;
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                            ClassLoader classLoader, JavaModule module) {
        ClassLoader loader = InstrumentationHolder.getAgentClassLoader();
        for (String name : adviceClassNames) {
            try {
                if (!AdviceInjectorCache.contains(name)) {
                    AdviceInjectorCache.registerInjector(name,
                            new AdviceInjectorCache.AdviceClassInjector(getBytes(name, loader)));
                }
            } catch (Exception ex) {
                LOGGER.warn("create class {} injector failed.", name, ex);
            }
        }

        return builder;
    }

    private byte[] getBytes(String name, ClassLoader loader) throws IOException {
        ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(loader);
        return locator.locate(name).resolve();
    }

}

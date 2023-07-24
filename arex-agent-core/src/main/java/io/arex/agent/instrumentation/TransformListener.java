package io.arex.agent.instrumentation;

import io.arex.foundation.config.ConfigManager;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformListener  extends AgentBuilder.Listener.Adapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformListener.class);

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

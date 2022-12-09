package io.arex.agent.instrumentation;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.serializer.JacksonSerializer;
import io.arex.foundation.services.ConfigService;
import io.arex.foundation.services.DataCollectorService;
import io.arex.foundation.util.NetUtils;
import io.arex.foundation.util.SPIUtil;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.StringSerializable;

import java.util.ServiceLoader;

public class ServiceInitializer {

    public static void start(String agentArgs) {
        TraceContextManager.init(NetUtils.getIpAddress());
        installSerializer();
        RecordLimiter.init(HealthManager::acquire);
        ConfigManager.INSTANCE.getServiceName();
        ConfigService.INSTANCE.loadAgentConfig(agentArgs);
        DataCollectorService.INSTANCE.start();
    }

    private static void installSerializer() {
        Serializer.Builder builder = Serializer.builder(JacksonSerializer.INSTANCE);
        ServiceLoader<StringSerializable> serializableList = SPIUtil.load(StringSerializable.class,
                "arex-instrumentation-api", "arex-instrumentation-api");
        for (StringSerializable serializable : serializableList) {
            builder.addSerializer(serializable.name(), serializable);
        }
        builder.build();
    }
}

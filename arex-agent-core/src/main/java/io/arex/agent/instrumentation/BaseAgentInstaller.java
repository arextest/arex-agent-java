package io.arex.agent.instrumentation;

import io.arex.agent.bootstrap.AgentInstaller;
import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.AdviceClassesCollector;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;
//import io.arex.foundation.serializer.JacksonSerializer;
import io.arex.foundation.services.ConfigService;
import io.arex.foundation.services.DataCollectorService;
import io.arex.foundation.util.NetUtils;
//import io.arex.foundation.util.SPIUtil;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.service.DataCollector;
import io.arex.inst.runtime.service.DataService;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.SPIUtil;

import java.util.List;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;

import java.io.File;
import java.lang.instrument.Instrumentation;

public abstract class BaseAgentInstaller implements AgentInstaller {

    protected final Instrumentation instrumentation;
    protected final File agentFile;
    protected final String agentArgs;

    private ResettableClassFileTransformer transformer;

    public BaseAgentInstaller(Instrumentation inst, File agentFile, String agentArgs) {
        this.instrumentation = inst;
        this.agentFile = agentFile;
        this.agentArgs = agentArgs;
    }

    @Override
    public void install() {
        ClassLoader savedContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            init(agentArgs);
            transformer = transform();
        } finally {
            Thread.currentThread().setContextClassLoader(savedContextClassLoader);
        }
        LogUtil.info("ArexJavaAgent AgentInstaller initialized.");
    }

    private void init(String agentArgs) {
        TraceContextManager.init(NetUtils.getIpAddress());
//        installSerializer();
        RecordLimiter.init(HealthManager::acquire);
        ConfigService.INSTANCE.loadAgentConfig(agentArgs);
        initDataCollector();
    }

//    private void installSerializer() {
//        AdviceClassesCollector.INSTANCE.addClassToLoaderSearch(JacksonSerializer.class);
//        Serializer.Builder builder = Serializer.builder(JacksonSerializer.INSTANCE);
//        List<StringSerializable> serializableList =
//                SPIUtil.load(StringSerializable.class, getClassLoader());
//        for (StringSerializable serializable : serializableList) {
//            builder.addSerializer(serializable.name(), serializable);
//        }
//        builder.build();
//    }
    private void initDataCollector() {
        DataCollector collector = DataCollectorService.INSTANCE;
        if (ConfigManager.INSTANCE.isLocalStorage()) {
            List<DataCollector> extendCollectorList =
                    SPIUtil.load(DataCollector.class, getClassLoader());
            if (CollectionUtil.isNotEmpty(extendCollectorList)) {
                collector = extendCollectorList.get(0);
            }
        }
        collector.start();
        DataService.builder().setDataCollector(collector).build();
    }

    protected abstract ResettableClassFileTransformer transform();

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
}

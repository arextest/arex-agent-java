package io.arex.agent.instrumentation;

import io.arex.agent.bootstrap.AgentInstaller;
import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.AdviceClassesCollector;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.serializer.JacksonSerializer;
import io.arex.foundation.services.ConfigService;
import io.arex.foundation.services.DataCollectorService;
import io.arex.foundation.util.NetUtils;
import io.arex.foundation.util.SPIUtil;
import io.arex.foundation.util.async.ThreadFactoryImpl;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.service.DataCollector;
import io.arex.inst.runtime.service.DataService;

import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;

import java.io.File;
import java.lang.instrument.Instrumentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseAgentInstaller implements AgentInstaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseAgentInstaller.class);
    protected final Instrumentation instrumentation;
    protected final File agentFile;
    protected final String agentArgs;
    private ResettableClassFileTransformer transformer;
    private final AtomicBoolean initDependentComponents = new AtomicBoolean(false);
    private final AtomicBoolean onlyTransformOnce = new AtomicBoolean(false);
    private ScheduledThreadPoolExecutor scheduler = null;

    public BaseAgentInstaller(Instrumentation inst, File agentFile, String agentArgs) {
        this.instrumentation = inst;
        this.agentFile = agentFile;
        this.agentArgs = agentArgs;
    }

    @Override
    public void install() {
        ClassLoader savedContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClassLoader());
            // Timed load config for agent delay start and dynamic retransform
            int delaySeconds = ConfigService.INSTANCE.loadAgentConfig(agentArgs);
            if (delaySeconds > 0) {
                if (scheduler == null) {
                    scheduler = new ScheduledThreadPoolExecutor(1, new ThreadFactoryImpl("arex-install-thread"));
                }
                scheduler.schedule(this::install, delaySeconds, TimeUnit.SECONDS);
            }
            if (!ConfigManager.INSTANCE.valid()) {
                if (!onlyTransformOnce.get()) {
                    LOGGER.warn("[AREX] Agent install will not install due to invalid config.");
                }
                return;
            }
            initDependentComponents();
            if (onlyTransformOnce.compareAndSet(false, true)) {
                transformer = transform();
                LOGGER.info("[AREX] Agent install success.");
            }
        } finally {
            Thread.currentThread().setContextClassLoader(savedContextClassLoader);
        }
    }

    private void initDependentComponents() {
        if (initDependentComponents.compareAndSet(false, true)) {
            TraceContextManager.init(NetUtils.getIpAddress());
            RecordLimiter.init(HealthManager::acquire);
            initSerializer();
            initDataCollector();
            loadForkJoinTask();
        }
    }

    /**
     * Load the ForkJoinTask inner class in advance for transform
     * ex: java.util.concurrent.ForkJoinTask$AdaptedCallable
     */
    private void loadForkJoinTask() {
        ForkJoinTask.class.getDeclaredClasses();
    }

    private void initSerializer() {
        AdviceClassesCollector.INSTANCE.addClassToLoaderSearch(JacksonSerializer.class);
        Serializer.Builder builder = Serializer.builder(JacksonSerializer.INSTANCE);
        List<StringSerializable> serializableList = SPIUtil.load(StringSerializable.class, getClassLoader());
        for (StringSerializable serializable : serializableList) {
            builder.addSerializer(serializable.name(), serializable);
        }
        builder.build();
    }
    private void initDataCollector() {
        DataCollector collector = DataCollectorService.INSTANCE;
        if (ConfigManager.INSTANCE.isLocalStorage()) {
            List<DataCollector> extendCollectorList = SPIUtil.load(DataCollector.class, getClassLoader());
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

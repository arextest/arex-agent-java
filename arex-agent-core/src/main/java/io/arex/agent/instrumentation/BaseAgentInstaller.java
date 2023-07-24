package io.arex.agent.instrumentation;

import io.arex.agent.bootstrap.AgentInstaller;
import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.AdviceClassesCollector;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.serializer.GsonSerializer;
import io.arex.foundation.serializer.JacksonSerializer;
import io.arex.foundation.services.ConfigService;
import io.arex.foundation.services.DataCollectorService;
import io.arex.foundation.services.TimerService;
import io.arex.foundation.util.NetUtils;
import io.arex.foundation.util.NumberTypeAdaptor;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.service.DataCollector;
import io.arex.inst.runtime.service.DataService;

import io.arex.agent.bootstrap.util.ServiceLoader;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ScheduledFuture;
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
    private final AtomicBoolean initDependentComponents = new AtomicBoolean(false);
    private ScheduledFuture<?> reportStatusTask;

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
            long delayMinutes = ConfigService.INSTANCE.loadAgentConfig(agentArgs);
            ConfigService.INSTANCE.reportStatus();
            if (delayMinutes > 0) {
                TimerService.schedule(this::install, delayMinutes, TimeUnit.MINUTES);
                timedReportStatus();
            }
            if (!ConfigManager.INSTANCE.valid()) {
                if (!ConfigManager.FIRST_TRANSFORM.get()) {
                    LOGGER.warn("[AREX] Agent would not install due to invalid config.");
                }
                return;
            }
            initDependentComponents();
            transform();
        } finally {
            Thread.currentThread().setContextClassLoader(savedContextClassLoader);
        }
    }

    private void timedReportStatus() {
        if (reportStatusTask != null) {
            return;
        }
        if (!ConfigManager.INSTANCE.isEnableReportStatus()) {
            return;
        }
        reportStatusTask = TimerService.scheduleAtFixedRate(() -> {
            ConfigService.INSTANCE.reportStatus();
            // Load agent config according to last modified time
            if (ConfigService.INSTANCE.reloadConfig()) {
                install();
            }
        }, 1, 1, TimeUnit.MINUTES);
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
     * Load the ForkJoinTask inner class in advance for transform ex: java.util.concurrent.ForkJoinTask$AdaptedCallable
     */
    private void loadForkJoinTask() {
        ForkJoinTask.class.getDeclaredClasses();
    }

    /**
     * add class to user loader search. ex: ParallelWebappClassLoader
     */
    private void initSerializer() {
        AdviceClassesCollector.INSTANCE.addClassToLoaderSearch(JacksonSerializer.class);
        AdviceClassesCollector.INSTANCE.addClassToLoaderSearch(GsonSerializer.class);
        AdviceClassesCollector.INSTANCE.addClassToLoaderSearch(NumberTypeAdaptor.class);
        Serializer.builder(JacksonSerializer.INSTANCE).build();
    }
    private void initDataCollector() {
        DataCollector collector = DataCollectorService.INSTANCE;
        if (ConfigManager.INSTANCE.isLocalStorage()) {
            List<DataCollector> extendCollectorList = ServiceLoader.load(DataCollector.class, getClassLoader());
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

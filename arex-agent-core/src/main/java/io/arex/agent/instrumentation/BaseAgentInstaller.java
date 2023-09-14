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
import io.arex.inst.extension.ExtensionTransformer;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.service.DataCollector;
import io.arex.inst.runtime.service.DataService;

import io.arex.agent.bootstrap.util.ServiceLoader;
import java.util.List;
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
            Runtime.getRuntime().addShutdownHook(new Thread(ConfigService.INSTANCE::shutdown, "arex-agent-shutdown-hook"));
            // Timed load config for dynamic retransform
            long delayMinutes = ConfigService.INSTANCE.loadAgentConfig(agentArgs);
            if (!allowStartAgent()) {
                ConfigService.INSTANCE.reportStatus();
                if (!ConfigManager.FIRST_TRANSFORM.get()) {
                    LOGGER.warn("[AREX] Agent would not install due to {}.", getInvalidReason());
                }
                return;
            }
            if (delayMinutes > 0) {
                TimerService.schedule(this::install, delayMinutes, TimeUnit.MINUTES);
                timedReportStatus();
            }
            initDependentComponents();
            transform();

            for (ExtensionTransformer transformer : loadTransformers()) {
                if (transformer.validate()) {
                    instrumentation.addTransformer(transformer, true);
                }
            }

            ConfigService.INSTANCE.reportStatus();
        } finally {
            Thread.currentThread().setContextClassLoader(savedContextClassLoader);
        }
    }

    private List<ExtensionTransformer> loadTransformers() {
        return ServiceLoader.load(ExtensionTransformer.class, getClassLoader());
    }

    boolean allowStartAgent() {
        if (ConfigManager.INSTANCE.isLocalStorage()) {
            return true;
        }
        return ConfigManager.INSTANCE.checkTargetAddress();
    }

    String getInvalidReason() {
        if (!ConfigManager.INSTANCE.checkTargetAddress()) {
            return "response [targetAddress] is not match";
        }

        return "invalid config";
    }

    private void timedReportStatus() {
        if (reportStatusTask != null) {
            return;
        }
        reportStatusTask = TimerService.scheduleAtFixedRate(() -> {
            try {
                ConfigService.INSTANCE.reportStatus();
                // Load agent config according to last modified time
                if (ConfigService.INSTANCE.reloadConfig()) {
                    install();
                }
            } catch (Exception e) {
                LOGGER.error("[AREX] Report status error.", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private void initDependentComponents() {
        if (initDependentComponents.compareAndSet(false, true)) {
            TraceContextManager.init(NetUtils.getIpAddress());
            RecordLimiter.init(HealthManager::acquire);
            initSerializer();
            initDataCollector();
        }
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

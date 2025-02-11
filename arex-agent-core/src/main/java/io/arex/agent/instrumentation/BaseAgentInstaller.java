package io.arex.agent.instrumentation;

import io.arex.agent.bootstrap.AgentInstaller;
import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.util.FileUtils;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.logger.AgentLoggerFactory;
import io.arex.foundation.logger.AgentLogger;
import io.arex.foundation.services.ConfigService;
import io.arex.foundation.services.TimerService;
import io.arex.foundation.util.NetUtils;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.service.DataCollector;
import io.arex.inst.runtime.service.DataService;

import io.arex.agent.bootstrap.util.ServiceLoader;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import java.io.File;
import java.lang.instrument.Instrumentation;

import net.bytebuddy.dynamic.scaffold.TypeWriter;

public abstract class BaseAgentInstaller implements AgentInstaller {
    private static final AgentLogger LOGGER = AgentLoggerFactory.getAgentLogger(BaseAgentInstaller.class);
    private static final String BYTECODE_DUMP_DIR = "/bytecode-dump";
    protected final Instrumentation instrumentation;
    protected final File agentFile;
    protected final String agentArgs;
    private ScheduledFuture<?> reportStatusTask;
    private ScheduledFuture<?> loadConfigTask;

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

            if (delayMinutes > 0 && loadConfigTask == null) {
                loadConfigTask = TimerService.scheduleAtFixedRate(this::install, delayMinutes, delayMinutes, TimeUnit.MINUTES);
                timedReportStatus();
            }

            if (ConfigManager.FIRST_TRANSFORM.compareAndSet(false, true)) {
                initDependentComponents();
                createDumpDirectory();
                transform();
            } else {
                retransform();
            }

            ConfigService.INSTANCE.reportStatus();
        } finally {
            Thread.currentThread().setContextClassLoader(savedContextClassLoader);
        }
    }

    boolean allowStartAgent() {
        if (ConfigManager.INSTANCE.isLocalStorage()) {
            return true;
        }
        return ConfigManager.INSTANCE.isAgentEnabled();
    }

    String getInvalidReason() {
        if (!ConfigManager.INSTANCE.isAgentEnabled()) {
            return ConfigManager.INSTANCE.getMessage();
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
        TraceContextManager.init(NetUtils.getIpAddress());
        RecordLimiter.init(HealthManager::acquire);
        initDataCollector();
    }
    private void initDataCollector() {
        List<DataCollector> collectorList = ServiceLoader.load(DataCollector.class, getClassLoader());
        DataService.setDataCollector(collectorList);
    }

    /**
     * First transform class
     */
    protected abstract void transform();

    /**
     * Retransform class after dynamic class changed
     */
    protected abstract void retransform();

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    private void createDumpDirectory() {
        if (!ConfigManager.INSTANCE.isEnableDebug()) {
            return;
        }

        try {
            File bytecodeDumpPath = new File(agentFile.getParent(), BYTECODE_DUMP_DIR);
            boolean exists = bytecodeDumpPath.exists();
            boolean mkdir = false;
            if (exists) {
                FileUtils.cleanDirectory(bytecodeDumpPath);
            } else {
                mkdir = bytecodeDumpPath.mkdirs();
            }
            if (exists || mkdir) {
                System.setProperty(TypeWriter.DUMP_PROPERTY, bytecodeDumpPath.getPath());
            }
            LOGGER.info("[arex] bytecode dump path exists: {}, mkdir: {}, path: {}", exists, mkdir, bytecodeDumpPath.getPath());
        } catch (Exception e) {
            LOGGER.warn("[arex] Failed to create directory to instrumented bytecode: ", e);
        }
    }
}

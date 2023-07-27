package io.arex.inst.config.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.internals.*;
import com.ctrip.framework.apollo.util.ConfigUtil;
import io.arex.agent.bootstrap.util.ReflectUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * {@link com.ctrip.framework.apollo.spi.DefaultConfigFactory#create} </br>
 * Config config = new DefaultConfig(new LocalFileConfigRepository(new RemoteConfigRepository(namespace)))
 * <pre>
 * process:
 *
 *             --------------------------------
 *             |       DefaultConfig          |
 *             |    ----------------------    |
 *             |    | onRepositoryChange |    |
 *             |    ----------------------    |
 *             --------------------------------
 *                        ¦          ↑
 *                        1          4
 *                        ↓          ¦
 *             --------------------------------
 *             |       LocalFileConfig        |
 *             |    ----------------------    |
 *             |    | onRepositoryChange |    |
 *             |    ----------------------    |
 *             --------------------------------
 *                        ¦          ↑
 *                        2          3
 *                        ↓          ¦
 *          ---------------------------------------
 *          |  RemoteConfig(trySync,longPolling)  |
 *          |    ------------------------         |
 *          |    | fireRepositoryChange |         |
 *          |    ------------------------         |
 *          ---------------------------------------
 *
 * </pre>
 */
public class ApolloConfigHelper {
    private static Field configInstancesField;

    public static void initAndRecord(Supplier<String> recordIdSpl, Supplier<String> versionSpl) {
        String recordId = recordIdSpl.get();
        if (StringUtil.isEmpty(recordId)) {
            return;
        }
        String configVersion = versionSpl.get();
        initReplayState(recordId, configVersion);

        if (StringUtil.isEmpty(configVersion)) {
            return;
        }
        /*
        Does not include increment config, as Apollo has not yet created an instance of this configuration
        it will be replay in io.arex.inst.config.apollo.ApolloConfigHelper.getReplayConfig
         */
        replayAllConfigs();
    }

    /**
     * 1. first record init config(full & incremental) {@link ApolloServletV3RequestHandler#postHandle}
     * 2. then record changed config within running {@link ApolloDefaultConfigInstrumentation}
     */
    public static void recordAllConfigs() {
        if (!ApolloConfigExtractor.needRecord()) {
            return;
        }
        ApolloConfigExtractor extractor = ApolloConfigExtractor.tryCreateExtractor();
        if (extractor == null) {
            return;
        }
        Map<String, Config> configMap = getAllConfigInstance();
        for (Map.Entry<String, Config> entry : configMap.entrySet()) {
            try {
                Properties properties = getConfigProperties(entry.getValue());
                if (properties != null) {
                    extractor.record(entry.getKey(), properties);
                }
            } catch (Throwable e) {
                LogManager.warn("record apollo config error", e);
            }
        }
    }

    /**
     * ConfigService -> s_instance -> m_manager -> m_configs -> m_configProperties
     */
    private static Map<String, Config> getAllConfigInstance() {
        try {
            if (configInstancesField == null) {
                configInstancesField = ConfigService.class.getDeclaredField("s_instance");
                configInstancesField.setAccessible(true);
            }
            Object configService = configInstancesField.get(null);
            Object managerInstance = ReflectUtil.getFieldOrInvokeMethod(() ->
                    ConfigService.class.getDeclaredField("m_configManager"), configService);
            Object configs = ReflectUtil.getFieldOrInvokeMethod(() ->
                    DefaultConfigManager.class.getDeclaredField("m_configs"), managerInstance);
            if (configs instanceof Map) {
                return (Map<String, Config>) configs;
            }
        } catch (Throwable e) {
            LogManager.warn("get apollo all config instance error", e);
        }
        return Collections.emptyMap();
    }

    private static Properties getConfigProperties(Config config) throws Exception {
        Object configProperties = ReflectUtil.getFieldOrInvokeMethod(() ->
                config.getClass().getDeclaredField("m_configProperties"), config);
        if (configProperties instanceof AtomicReference) {
            AtomicReference<Properties> properties = (AtomicReference<Properties>) configProperties;
            return properties.get();
        }
        return null;
    }

    public static void initReplayState(String recordId, String configVersion) {
        ApolloConfigExtractor.updateReplayState(recordId, configVersion);
    }

    /**
     * you can also modify m_configs in {@link DefaultConfigManager} just like the recorded logic,
     * But there are the following points to consider: <pre>
     * 1. the case where configuration polling triggers a change during replay, it may overwrite the values replay
     * 2. how can trigger ConfigChangeListener on business side
     * 3. how to recover the original configuration after replay
     * </pre>
     * so the final entry point is sync() in the {@link RemoteConfigRepository}
     */
    public static void replayAllConfigs() {
        Map<String, Config> configMap = getAllConfigInstance();
        for (Map.Entry<String, Config> entry : configMap.entrySet()) {
            try {
                triggerReplay((DefaultConfig) entry.getValue());
            } catch (Exception e) {
                LogManager.warn("replay apollo config error", e);
            }
        }
    }

    private static void triggerReplay(DefaultConfig config) throws Exception {
        Object repositoryObj = ReflectUtil.getFieldOrInvokeMethod(() ->
                config.getClass().getDeclaredField("m_configRepository"), config);
        if (repositoryObj instanceof LocalFileConfigRepository) {
            LocalFileConfigRepository localRepository = (LocalFileConfigRepository) repositoryObj;
            Object remoteRepositoryObj = ReflectUtil.getFieldOrInvokeMethod(() ->
                    localRepository.getClass().getDeclaredField("m_upstream"), localRepository);
            if (remoteRepositoryObj instanceof RemoteConfigRepository) {
                RemoteConfigRepository remoteRepository = (RemoteConfigRepository) remoteRepositoryObj;
                // sync -> loadApolloConfig(by arex transformed)
                ReflectUtil.getFieldOrInvokeMethod(() ->
                        remoteRepository.getClass().getDeclaredMethod("sync"), remoteRepository);
            }
        }
    }

    public static ApolloConfig getReplayConfig(ApolloConfig previous, String namespace, ConfigUtil configUtil) {
        if (!ApolloConfigExtractor.duringReplay() || previous == null) {
            // execute original business code, not replay
            return null;
        }
        /*
        if releaseKey is same, then there is no need to replay it again,
        because this configuration has already been replayed, during the first full replay(replayAllConfigs)
         */
        if (getReleaseKey().equals(previous.getReleaseKey())) {
            return previous;
        }
        /*
        if releaseKey is different during replaying, then it needs to be replayed again, because:
        1. switch to a new configuration(different configBatchNo) for next config version replay
        2. this configuration belongs to a new configuration,
           does not exist during the first full replay (replayAllConfigs)
           and no instances were created, which belongs to a new configuration.
           in other words, the configuration instance and content are only created when the business code is executed.
         */
        Properties properties = ApolloConfigExtractor.replay(namespace);
        if (properties == null) {
            return null;
        }

        ApolloConfig config = new ApolloConfig(
                configUtil.getAppId(),
                configUtil.getCluster(),
                namespace,
                getReleaseKey());
        config.setConfigurations((Map) properties);
        return config;
    }

    /**
     * format: arex-fff55f0a-b457-4cbe-9183-ca1adcf0d851
     *
     * during long polling pull configuration,
     * the Apollo-Config-Server will determine whether it is the latest configuration based on the request param: releaseKey,
     * if it is different between local and server, the server will return the latest configuration.
     * the purpose of doing this is to use the real configuration after replay finished,
     * that is, restore the real configuration.
     */
    private static String getReleaseKey() {
        return ArexConstants.PREFIX + ApolloConfigExtractor.currentReplayConfigBatchNo();
    }
}

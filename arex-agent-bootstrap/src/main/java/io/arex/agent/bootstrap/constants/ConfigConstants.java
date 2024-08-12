package io.arex.agent.bootstrap.constants;

public class ConfigConstants {
    private ConfigConstants() {
    }
    public static final String ENABLE_DEBUG = "arex.enable.debug";
    public static final String SERVICE_NAME = "arex.service.name";
    public static final String STORAGE_SERVICE_HOST = "arex.storage.service.host";
    public static final String CONFIG_SERVICE_HOST = "arex.config.service.host";
    public static final String CONFIG_PATH = "arex.config.path";
    public static final String STORAGE_MODE = "local";
    public static final String RECORD_RATE = "arex.rate.limit";
    public static final String DYNAMIC_RESULT_SIZE_LIMIT = "arex.dynamic.result.size.limit";
    public static final String STORAGE_SERVICE_MODE = "arex.storage.mode";
    public static final String ALLOW_DAY_WEEKS = "arex.allow.day.weeks";
    public static final String ALLOW_TIME_FROM = "arex.allow.time.from";
    public static final String ALLOW_TIME_TO = "arex.allow.time.to";

    /**
     * Assign values via -Darex.ignore.type.prefixes=xxx.type, separate multiple values with commas.
     */
    public static final String IGNORED_TYPE_PREFIXES = "arex.ignore.type.prefixes";
    /**
     * Assign values via -Darex.ignore.classloader.prefixes=xxx.classloader, separate multiple values with commas.
     */
    public static final String IGNORED_CLASS_LOADER_PREFIXES = "arex.ignore.classloader.prefixes";
    public static final String DISABLE_MODULE = "arex.disable.instrumentation.module";
    public static final String RETRANSFORM_MODULE = "arex.retransform.instrumentation.module";

    public static final String EXCLUDE_SERVICE_OPERATION = "arex.exclude.service.operation";
    public static final String DUBBO_STREAM_REPLAY_THRESHOLD = "arex.dubbo.replay.threshold";
    public static final String DISABLE_REPLAY = "arex.disable.replay";
    public static final String DISABLE_RECORD = "arex.disable.record";
    public static final String DURING_WORK = "arex.during.work";
    public static final String AGENT_VERSION = "arex.agent.version";
    public static final String AGENT_ENABLED = "arex.agent.enabled";
    public static final String CURRENT_RATE = "arex.current.rate";
    public static final String DECELERATE_CODE = "arex.decelerate.code";
    public static final String SERIALIZER_CONFIG = "arex.serializer.config";
    public static final String BUFFER_SIZE = "arex.buffer.size";
    public static final String SIMPLE_LOGGER_SHOW_DATE_TIME = "shaded.org.slf4j.simpleLogger.showDateTime";
    public static final String SIMPLE_LOGGER_DATE_TIME_FORMAT = "shaded.org.slf4j.simpleLogger.dateTimeFormat";
    public static final String SIMPLE_LOGGER_FILE = "shaded.org.slf4j.simpleLogger.logFile";
    public static final String COVERAGE_PACKAGES = "arex.coverage.packages";
    public static final String APP_CLASSLOADER_NAME = "jdk.internal.loader.ClassLoaders$AppClassLoader";
    public static final String API_TOKEN = "arex.api.token";
    public static final String MOCKER_TAGS = "arex.mocker.tags";
    public static final String LOG_PATH = "arex.log.path";
}

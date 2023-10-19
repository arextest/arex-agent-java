package io.arex.inst.runtime.model;

public class ArexConstants {
    private ArexConstants() {}

    public static final String RECORD_ID = "arex-record-id";
    public static final String REPLAY_ID = "arex-replay-id";
    public static final String REPLAY_WARM_UP = "arex-replay-warm-up";
    public static final String FORCE_RECORD = "arex-force-record";
    public static final String REDIRECT_REQUEST_METHOD = "arex-redirect-request-method";
    public static final String REDIRECT_REFERER = "arex-redirect-referer";
    public static final String REDIRECT_PATTERN = "arex-redirect-pattern";
    /**
     * mock template
     */
    public static final String HEADER_EXCLUDE_MOCK = "X-AREX-Exclusion-Operations";
    /**
     * dubbo stream protocol:triple
     */
    public static final String DUBBO_STREAM_PROTOCOL = ":tri";
    /**
     * dubbo stream protocol:streaming
     */
    public static final String DUBBO_STREAM_NAME = "streaming";

    public static final String UUID_SIGNATURE = "java.util.UUID.randomUUID";
    public static final String CURRENT_TIME_MILLIS_SIGNATURE = "java.lang.System.currentTimeMillis";
    public static final String NEXT_INT_SIGNATURE = "java.util.Random.nextInt";
    public static final String SERIALIZE_SKIP_INFO_CONFIG_KEY = "serializeSkipInfoList";
    public static final String SCHEDULE_REPLAY_FLAG = "arex-schedule-replay";
    public static final String REPLAY_ORIGINAL_MOCKER = "arex-replay-original-mocker";
    public static final String AREX_EXTENSION_ATTRIBUTE = "arex-extension-attribute";
    public static final String GSON_SERIALIZER = "gson";
    public static final String CONFIG_DEPENDENCY = "arex_replay_prepare_dependency";
    public static final String PREFIX = "arex-";
    public static final String CONFIG_VERSION = "configBatchNo";
    public static final String SKIP_FLAG = "arex-skip-flag";
    public static final String ORIGINAL_REQUEST = "arex-original-request";
}

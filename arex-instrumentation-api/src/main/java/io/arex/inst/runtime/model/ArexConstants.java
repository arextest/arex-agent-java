package io.arex.inst.runtime.model;

public class ArexConstants {
    public static final String HTTP_METHOD_HEAD = "HEAD";
    public static final String HTTP_METHOD_OPTIONS = "OPTIONS";
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";

    private ArexConstants() {}

    /**
     * The prefix of the header passed through the gateway.
     */
    public static final String HEADER_X_PREFIX = "X-";

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
    public static final String SCHEDULE_REPLAY = "arex-schedule-replay";
    public static final String REPLAY_ORIGINAL_MOCKER = "arex-replay-original-mocker";
    public static final String AREX_EXTENSION_ATTRIBUTE = "arex-extension-attribute";
    public static final String GSON_SERIALIZER = "gson";
    public static final String GSON_REQUEST_SERIALIZER = "gson-request";
    public static final String JACKSON_SERIALIZER = "jackson";
    public static final String JACKSON_SERIALIZER_WITH_TYPE = "jackson-with-type";
    public static final String JACKSON_REQUEST_SERIALIZER = "jackson-request";
    public static final String AREX_SERIALIZER = "arex-serializer";
    public static final String CONFIG_DEPENDENCY = "arex_replay_prepare_dependency";
    public static final String PREFIX = "arex-";
    public static final String CONFIG_VERSION = "configBatchNo";
    public static final String SKIP_FLAG = "arex-skip-flag";
    public static final String ORIGINAL_REQUEST = "arex-original-request";
    public static final String MERGE_RECORD_NAME = "arex.mergeRecord";
    public static final String MERGE_RECORD_THRESHOLD = "arex.merge.record.threshold";
    public static final String DISABLE_MERGE_RECORD = "arex.disable.merge.record";
    public static final int MERGE_RECORD_THRESHOLD_DEFAULT = 10;
    public static final String MERGE_TYPE = "java.util.ArrayList-io.arex.inst.runtime.model.MergeDTO";
    public static final String MERGE_SPLIT_COUNT = "arex.merge.split.count";
    public static final long MEMORY_SIZE_1MB = 1024L * 1024L;
    public static final long MEMORY_SIZE_5MB = 5 * 1024L * 1024L;
    public static final String EXCEED_MAX_SIZE_TITLE = "exceed.max.size";
    public static final String EXCEED_MAX_SIZE_FLAG = "isExceedMaxSize";
    public static final String RECORD_SIZE_LIMIT = "arex.record.size.limit";
    public static final String SERVLET_V3 = "ServletV3";
    public static final String SERVLET_V5 = "ServletV5";
    public static final String DATABASE = "database";
    public static final String DB_NAME = "dbName";
    public static final String DB_PARAMETERS = "parameters";
    public static final String DB_SQL = "sql";
    public static final int DB_SQL_MAX_LEN = 5000;
    public static final String MOCKER_TYPE = "java.util.ArrayList-io.arex.agent.bootstrap.model.ArexMocker";
    public static final String REPLAY_COMPARE_TYPE = "java.util.ArrayList-io.arex.inst.runtime.model.ReplayCompareResultDTO";
    public static final String HTTP_QUERY_STRING = "QueryString";
    public static final String HTTP_METHOD = "HttpMethod";
    public static final String HTTP_BODY = "body";
    public static final String HTTP_CONTENT_TYPE = "ContentType";
    public static final String DISABLE_SQL_PARSE = "arex.disable.sql.parse";
    public static final String MATCH_LOG_TITLE = "replay.match";
    public static final String MOCKER_TARGET_TYPE = "io.arex.agent.bootstrap.model.Mocker$Target";
    public static final String SPRING_SCAN_PACKAGES = "arex.spring.scan.packages";
    public static final String REPLAY_END_FLAG = "arex-replay-end";
}

package io.arex.inst.runtime.model;

public class ArexConstants {
    private ArexConstants() {}

    public static final String RECORD_ID = "arex-record-id";
    public static final String REPLAY_ID = "arex-replay-id";
    public static final String REPLAY_WARM_UP = "arex-replay-warm-up";
    public static final String FORCE_RECORD = "arex-force-record";
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
}

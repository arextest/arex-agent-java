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
}

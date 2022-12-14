package io.arex.inst.runtime.model;

public class ArexConstants {
    private ArexConstants() {}
    /**
     * command line prompt
     */
    public static final String CLI_PROMPT = "arex> ";

    /**
     * command line separator
     */
    public static final String CLI_SEPARATOR = "[arex@]";

    public static final String TYPE_LIST_DIFFMOCKER = "java.util.ArrayList-io.arex.foundation.model.DiffMocker";

    public static final String RECORD_ID = "arex-record-id";
    public static final String REPLAY_ID = "arex-replay-id";
    public static final String REPLAY_WARM_UP = "arex-replay-warm-up";
    public static final String FORCE_RECORD = "arex-force-record";
    /**
     * mock template
     */
    public static final String HEADER_EXCLUDE_MOCK = "X-AREX-Exclusion-Operations";
}

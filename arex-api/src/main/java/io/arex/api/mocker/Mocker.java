package io.arex.api.mocker;

/**
 * Mocker
 */
public interface Mocker {
    String getCaseId();
    String getReplayId();
    long getQueueTime();
    int getCategoryType();
    String getCategoryName();

    /**
     * Record data
     */
    default void record() {

    };

    /**
     * Replay data
     * @return Mock object
     */
    default Object replay() {
        return null;
    }

    Object parseMockResponse();
}

package io.arex.inst.runtime.extension;

/**
 * extended interface are used to handle service entrance request
 */
public interface RequestHandler {
    String name();

    /**
     * add or get request information
     */
    void preProcess(Object request);
    void postProcess(Object request, Object response);
}

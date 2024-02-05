package io.arex.inst.runtime.request;

/**
 * extended interface are used to handle service entrance request
 */
public interface RequestHandler<Request, Response> {
    String name();

    /**
     * add or get request information
     */
    void preHandle(Request request);
    void handleAfterCreateContext(Request request);
    void postHandle(Request request, Response response);
}

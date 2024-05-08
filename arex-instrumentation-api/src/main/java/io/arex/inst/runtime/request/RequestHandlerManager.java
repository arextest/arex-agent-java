package io.arex.inst.runtime.request;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.ServiceLoader;
import io.arex.inst.runtime.log.LogManager;

public class RequestHandlerManager {
    private static final Map<String, List<RequestHandler>> REQUEST_HANDLER_CACHE = new ConcurrentHashMap<>();

    public static void init(ClassLoader contextClassLoader) {
        final List<RequestHandler> requestHandlers = ServiceLoader.load(RequestHandler.class, contextClassLoader);
        final Map<String, List<RequestHandler>> requestHandlerMap = requestHandlers.stream().collect(Collectors.groupingBy(RequestHandler::name));
        REQUEST_HANDLER_CACHE.putAll(requestHandlerMap);
    }

    public static void preHandle(Object request, String name) {
        final List<RequestHandler> requestHandlers = REQUEST_HANDLER_CACHE.get(name);
        if (CollectionUtil.isEmpty(requestHandlers)) {
            return;
        }
        for (RequestHandler requestHandler : requestHandlers) {
            try {
                requestHandler.preHandle(request);
            } catch (Throwable ex) {
                // avoid affecting the remaining handlers when one handler fails
                LogManager.warn("preHandler", ex);
            }
        }
    }

    public static void handleAfterCreateContext(Object request, String name) {
        final List<RequestHandler> requestHandlers = REQUEST_HANDLER_CACHE.get(name);
        if (CollectionUtil.isEmpty(requestHandlers)) {
            return;
        }
        for (RequestHandler requestHandler : requestHandlers) {
            try {
                requestHandler.handleAfterCreateContext(request);
            } catch (Throwable ex) {
                // avoid affecting the remaining handlers when one handler fails
                LogManager.warn("handleAfterCreateContext", ex);
            }
        }
    }

    public static void postHandle(Object request, Object response, String name) {
        final List<RequestHandler> requestHandlers = REQUEST_HANDLER_CACHE.get(name);
        if (CollectionUtil.isEmpty(requestHandlers)) {
            return;
        }
        for (RequestHandler requestHandler : requestHandlers) {
            try {
                requestHandler.postHandle(request, response);
            } catch (Throwable ex) {
                LogManager.warn("postHandle", ex);
            }
        }
    }
}

package io.arex.inst.runtime.extension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.arex.agent.bootstrap.util.ServiceLoader;

public class RequestProcessor {
    private static final Map<String, List<RequestHandler>> REQUEST_HANDLER_CACHE = new ConcurrentHashMap<>();

    public static void init() {
        final List<RequestHandler> requestHandlers = ServiceLoader.load(RequestHandler.class);
        final Map<String, List<RequestHandler>> requestHandlerMap = requestHandlers.stream().collect(Collectors.groupingBy(RequestHandler::name));
        REQUEST_HANDLER_CACHE.putAll(requestHandlerMap);
    }

    public static void preProcess(Object request, String name) {
        final List<RequestHandler> requestHandlers = REQUEST_HANDLER_CACHE.get(name);
        if (requestHandlers == null) {
            return;
        }
        for (RequestHandler requestHandler : requestHandlers) {
            requestHandler.preProcess(request);
        }
    }

    public static void postProcess(Object request, Object response, String name) {
        final List<RequestHandler> requestHandlers = REQUEST_HANDLER_CACHE.get(name);
        if (requestHandlers == null) {
            return;
        }
        for (RequestHandler requestHandler : requestHandlers) {
            requestHandler.postProcess(request, response);
        }
    }
}

package io.arex.inst.runtime.extension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ServiceLoader;

public class MockerProcessor {
    private static final Map<String, List<MockerHandler>> MOCKER_HANDLER_CACHE = new ConcurrentHashMap<>();

    public static void init() {
        final List<MockerHandler> mockers = ServiceLoader.load(MockerHandler.class);
        final Map<String, List<MockerHandler>> mockerHandlerMap = mockers.stream().collect(Collectors.groupingBy(MockerHandler::name));
        MOCKER_HANDLER_CACHE.putAll(mockerHandlerMap);
    }

    /**
     * add, update or delete mocker information
     */
    public static void preProcess(Mocker mocker) {
        final List<MockerHandler> mockerHandlers = MOCKER_HANDLER_CACHE.get(mocker.getCategoryType().getName());
        if (mockerHandlers == null) {
            return;
        }
        for (MockerHandler mockerHandler : mockerHandlers) {
            mockerHandler.preProcess(mocker);
        }
    }
}

package io.arex.inst.runtime.listener;

import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.model.InitializeEnum;
import io.arex.inst.runtime.request.RequestHandlerManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.Logger;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.agent.bootstrap.util.ServiceLoader;

import java.util.List;

import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.LoggerFactory;

public class EventProcessor {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EventProcessor.class);
    private static final String CLOCK_CLASS = "java.lang.System";
    private static final String CLOCK_METHOD = "currentTimeMillis";
    public static final String EXCLUDE_MOCK_TYPE = "java.util.HashMap-java.lang.String,java.util.HashSet";
    private static final AtomicReference<InitializeEnum> INIT_DEPENDENCY = new AtomicReference<>(InitializeEnum.START);

    /**
     * the onRequest method must be called before calling the onCreate method
     */
    public static void onCreate(EventSource source){
        if (!InitializeEnum.COMPLETE.equals(INIT_DEPENDENCY.get())) {
            return;
        }
        initContext(source);
        initClock();
        addEnterLog();
    }

    private static void addEnterLog() {
        final ArexContext context = ContextManager.currentContext();
        if (context == null) {
            return;
        }
        final String recordId = context.getCaseId();
        final String replayId = context.getReplayId();
        if (StringUtil.isNotEmpty(replayId)) {
            LogManager.info("enter", StringUtil.format("arex-record-id: %s, arex-replay-id: %s", recordId, replayId));
            return;
        }
        LogManager.info("enter", StringUtil.format("arex-record-id: %s", recordId));
    }

    /**
     * user loader to load serializer, ex: ParallelWebappClassLoader
     */
    private static void initSerializer() {
        final List<StringSerializable> serializableList = ServiceLoader.load(StringSerializable.class, Thread.currentThread().getContextClassLoader());
        Serializer.builder(serializableList).build();
    }

    public static void initContext(EventSource source){
        ArexContext context = ContextManager.currentContext(true, source.getCaseId());
        if (context != null) {
            context.setExcludeMockTemplate(Serializer.deserialize(source.getExcludeMockTemplate(), EXCLUDE_MOCK_TYPE));
        }
    }

    private static void initClock(){
        try {
            if (ContextManager.needReplay() && Config.get().getBoolean("arex.time.machine", false)) {
                Mocker mocker = MockUtils.createDynamicClass(CLOCK_CLASS, CLOCK_METHOD);
                long millis = parseLong(MockUtils.replayBody(mocker));
                if (millis > 0) {
                    TimeCache.put(millis);
                }
            } else if (ContextManager.needRecord()) {
                Mocker mocker = MockUtils.createDynamicClass(CLOCK_CLASS, CLOCK_METHOD);
                mocker.getTargetResponse().setBody(String.valueOf(System.currentTimeMillis()));
                mocker.getTargetResponse().setType(Long.class.getName());
                MockUtils.recordMocker(mocker);
            }
        } catch (Throwable e) {
            LOGGER.warn(LogManager.buildTitle("time.machine.init"), e);
        }
    }

    public static void onExit(){
    }

    /**
     * Processing at the beginning of entry, for example:Servlet、Netty
     * init dependency only once, the context is only allowed to be created after the initialization is complete
     */
    public static void onRequest(){
        if (INIT_DEPENDENCY.compareAndSet(InitializeEnum.START, InitializeEnum.RUNNING)) {
            initSerializer();
            initLog();
            RequestHandlerManager.init();
            INIT_DEPENDENCY.set(InitializeEnum.COMPLETE);
        }
        TimeCache.remove();
        ContextManager.remove();
    }

    private static void initLog() {
        List<Logger> extensionLoggerList = ServiceLoader.load(Logger.class, Thread.currentThread().getContextClassLoader());
        LogManager.build(extensionLoggerList);
    }

    private static long parseLong(Object value) {
        if (value == null) {
            return 0;
        }

        String valueStr = String.valueOf(value);
        if (StringUtil.isEmpty(valueStr)) {
            return 0;
        }

        try {
            return Long.parseLong(valueStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

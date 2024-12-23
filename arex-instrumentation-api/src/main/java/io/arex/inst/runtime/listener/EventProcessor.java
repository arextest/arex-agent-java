package io.arex.inst.runtime.listener;

import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.AdviceClassesCollector;
import io.arex.agent.bootstrap.util.NumberUtil;
import io.arex.agent.bootstrap.util.ReflectUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.bootstrap.util.ServiceLoader;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.model.InitializeEnum;
import io.arex.inst.runtime.request.RequestHandlerManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.Logger;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.util.MockUtils;

import java.lang.reflect.Method;
import java.util.List;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import io.arex.inst.runtime.util.ReplayUtil;
import org.slf4j.LoggerFactory;

public class EventProcessor {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EventProcessor.class);
    private static final String CLOCK_CLASS = "java.lang.System";
    private static final String CLOCK_METHOD = "currentTimeMillis";
    public static final String EXCLUDE_MOCK_TYPE = "java.util.HashMap-java.lang.String,java.util.HashSet";
    private static final AtomicReference<InitializeEnum> INIT_DEPENDENCY = new AtomicReference<>(InitializeEnum.START);
    private static final Method FIND_LOADED_METHOD = ReflectUtil.getMethod(ClassLoader.class, "findLoadedClass", String.class);
    private static boolean existJacksonDependency = true;
    static {
        try {
            Class.forName("com.fasterxml.jackson.databind.ObjectMapper",true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            existJacksonDependency = false;
        }
    }

    /**
     * the onRequest method must be called before calling the onCreate method
     */
    public static void onCreate(EventSource source){
        if (!dependencyInitComplete()) {
            return;
        }
        initContext(source);
        loadReplayData();
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
    private static void initSerializer(ClassLoader contextClassLoader) {
        if (!existJacksonDependency) {
            AdviceClassesCollector.INSTANCE.appendToClassLoaderSearch("jackson",
                    contextClassLoader);
        }
        final List<StringSerializable> serializableList = ServiceLoader.load(StringSerializable.class, contextClassLoader);
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
            if (ContextManager.needReplay()) {
                Mocker mocker = MockUtils.createDynamicClass(CLOCK_CLASS, CLOCK_METHOD);
                long millis = NumberUtil.parseLong(MockUtils.replayBody(mocker));
                if (millis > 0) {
                    TimeCache.put(millis);
                }
            } else if (ContextManager.needRecord()) {
                Mocker mocker = MockUtils.createDynamicClass(CLOCK_CLASS, CLOCK_METHOD);
                mocker.getTargetResponse().setBody(String.valueOf(System.currentTimeMillis()));
                mocker.getTargetResponse().setType(Long.class.getName());
                MockUtils.recordMocker(mocker);
            }
        } catch (Exception e) {
            LOGGER.warn(LogManager.buildTitle("time.machine.init"), e);
        }
    }

    public static void onExit(){
        ContextManager.remove();
    }

    /**
     * Processing at the beginning of entry, for example:Servlet、Netty
     * init dependency only once, the context is only allowed to be created after the initialization is complete.
     * The initialization process should not block the main thread and should be done asynchronously.
     * Recording should start after INIT_DEPENDENCY is set to complete.
     */
    public static void onRequest(){
        if (INIT_DEPENDENCY.compareAndSet(InitializeEnum.START, InitializeEnum.RUNNING)) {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            // https://bugs.openjdk.org/browse/JDK-8172726
            CompletableFuture.runAsync(() -> {
                initClass(contextClassLoader);
                initSerializer(contextClassLoader);
                initLog(contextClassLoader);
                RequestHandlerManager.init(contextClassLoader);
                INIT_DEPENDENCY.set(InitializeEnum.COMPLETE);
            });
        }
        TimeCache.remove();
        ContextManager.remove();
    }

    /**
     * To prevent class initialization failure during request replay,
     * it is advisable to perform early initialization of classes that rely on external dependencies.
     * This helps to avoid triggering the class initialization logic when replaying requests.
     */
    private static void initClass(ClassLoader contextClassLoader) {
        boolean disableReplay = Config.get().getBoolean(ConfigConstants.DISABLE_REPLAY, false);
        if (disableReplay) {
            return;
        }
        String arexStaticClassInit = Config.get().getString(ConfigConstants.AREX_STATIC_CLASS_INIT);
        if (StringUtil.isBlank(arexStaticClassInit)) {
            return;
        }
        Set<String> initClassNameSet = StringUtil.splitToSet(arexStaticClassInit, ',');
        for (String initClass : initClassNameSet) {
            try {
                if (isClassLoaded(initClass, contextClassLoader)) {
                    continue;
                }
                Class.forName(initClass, true, contextClassLoader);
            } catch (Throwable e) {
                // if the class init fails, the next class init will continue
                LOGGER.warn(LogManager.buildTitle("init.class"), e);
            }
        }
    }

    private static boolean isClassLoaded(String className, ClassLoader classLoader) {
        return FIND_LOADED_METHOD == null || ReflectUtil.invoke(FIND_LOADED_METHOD, classLoader, className) != null;
    }

    private static void initLog(ClassLoader contextClassLoader) {
        List<Logger> extensionLoggerList = ServiceLoader.load(Logger.class, contextClassLoader);
        LogManager.build(extensionLoggerList);
    }

    public static boolean dependencyInitComplete() {
        return InitializeEnum.COMPLETE.equals(INIT_DEPENDENCY.get());
    }

    private static void loadReplayData() {
        ReplayUtil.queryMockers();
    }
}

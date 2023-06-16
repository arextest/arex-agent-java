package io.arex.inst.runtime.listener;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.agent.bootstrap.util.ServiceLoader;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessor.class);
    private static final String CLOCK_CLASS = "java.lang.System";
    private static final String CLOCK_METHOD = "currentTimeMillis";
    public static final String EXCLUDE_MOCK_TYPE = "java.util.HashMap-java.lang.String,java.util.HashSet";
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    public static void onCreate(EventSource source){
        initContext(source);
        initClock();
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
                String result = String.valueOf(MockUtils.replayBody(mocker));
                long millis = parseLong(result);
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
            LOGGER.warn(LogUtil.buildTitle("time.machine.init"), e);
        }
    }

    public static void onExit(){
    }

    /**
     * Processing at the beginning of entry, for example:Servlet、Netty
     */
    public static void onRequest(){
        if (INITIALIZED.compareAndSet(false, true)) {
            initSerializer();
        }
        TimeCache.remove();
        TraceContextManager.remove();
        ContextManager.overdueCleanUp();
    }

    private static long parseLong(String value) {
        if (StringUtil.isEmpty(value) || "null".equals(value)) {
            return 0;
        }

        long result;
        try {
            result = Long.parseLong(value);
        } catch (NumberFormatException e) {
            result = 0;
        }

        return result;
    }
}

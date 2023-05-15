package io.arex.inst.runtime.listener;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.Serializer.Builder;
import io.arex.inst.runtime.serializer.StringSerializable;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.SPIUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
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
        if (INITIALIZED.compareAndSet(false, true)) {
            initSerializer();
        }
        // not init serializer, not record
        if (Serializer.getINSTANCE().getSerializer() == null) {
            return;
        }
        initContext(source);
        initClock();
    }

    private static void initSerializer() {
        List<StringSerializable> serializers = SPIUtil.load(StringSerializable.class);
        Builder builder = null;
        for (StringSerializable serializable : serializers) {
            if (isJacksonSerialize(serializable)) {
                builder = Serializer.builder(serializable);
            }
        }
        if (builder == null) {
            return;
        }
        for (StringSerializable serializable : serializers) {
            if (isJacksonSerialize(serializable)) {
                continue;
            }
            builder.addSerializer(serializable.name(), serializable);
        }
        builder.build();
    }

    private static boolean isJacksonSerialize(StringSerializable serializable) {
        return "jackson".equals(serializable.name());
    }

    public static <T> List<T> load(Class<T> service) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return load(service, cl);
    }

    @SuppressWarnings("ForEachIterable")
    public static <T> List<T> load(Class<T> service, ClassLoader loader) {
        List<T> result = new ArrayList<>();
        java.util.ServiceLoader<T> services = ServiceLoader.load(service, loader);
        for (Iterator<T> iter = services.iterator(); iter.hasNext(); ) {
            try {
                result.add(iter.next());
            } catch (Throwable e) {
                LOGGER.warn("Unable to load class: {} from classloader: {}, throwable: {}",
                        service.getName(), service.getClassLoader(), e.toString());
            }
        }
        return result;
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

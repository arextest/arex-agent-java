package io.arex.inst.runtime.listener;

import io.arex.agent.bootstrap.ctx.ArexThreadLocal;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Consumer;

public class EventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);

    private static final ArexThreadLocal<Consumer<Mocker>> CONSUME_EVENT_TL = new ArexThreadLocal<>();

    public static void register(Consumer<Mocker> action) {
        CONSUME_EVENT_TL.set(action);
    }

    public static void trigger(Mocker mocker) {
        try {
            Consumer<Mocker> consumer = CONSUME_EVENT_TL.get();
            if (consumer != null) {
                consumer.accept(mocker);
            }
        } catch (Throwable e) {
            LOGGER.warn(LogManager.buildTitle("event.trigger"), e);
        }
    }
}

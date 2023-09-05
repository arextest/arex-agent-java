package io.arex.inst.runtime.listener;

import static org.mockito.Mockito.times;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class CaseEventDispatcherTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void onEvent() {
        try (MockedStatic<EventProcessor> mockedStatic = Mockito.mockStatic(EventProcessor.class)) {
            // test enter event
            CaseEventDispatcher.onEvent(CaseEvent.ofEnterEvent());
            mockedStatic.verify(EventProcessor::onRequest, times(1));

            // test create event
            CaseEventDispatcher.onEvent(CaseEvent.ofCreateEvent(EventSource.empty()));
            mockedStatic.verify(() -> EventProcessor.onCreate(EventSource.empty()), times(1));

            // test exit event
            CaseEventDispatcher.onEvent(CaseEvent.ofExitEvent());
            mockedStatic.verify(EventProcessor::onExit, times(1));
        }
    }
}

package io.arex.inst.runtime.listener;

public class CaseEventDispatcher {

    public static void onEvent(CaseEvent event) {
        switch (event.getAction()) {
            case ENTER:
                EventProcessor.onRequest();
                 break;
            case CREATE:
                EventProcessor.onCreate((EventSource) event.getSource());
                break;
            case EXIT:
                EventProcessor.onExit();
                break;
            default:
        }
    }
}

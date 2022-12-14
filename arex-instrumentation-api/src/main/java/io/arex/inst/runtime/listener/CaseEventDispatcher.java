package io.arex.inst.runtime.listener;

public class CaseEventDispatcher {

    public static void onEvent(CaseEvent e) {
        switch (e.action) {
            case ENTER:
                EventProcessor.onRequest();
                 break;
            case CREATE:
                EventProcessor.onCreate((EventSource) e.getSource());
                break;
            case DESTROY:
                EventProcessor.onExit();
                break;
            default:
        }
    }
}

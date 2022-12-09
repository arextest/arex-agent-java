package io.arex.inst.runtime.listener;

public class CaseEventDispatcher {

    public static void onEvent(CaseEvent e) {
        switch (e.action) {
            case ENTER:
                EventProcessor.onRequest();
            case CREATE:
                EventProcessor.onCreate();
                break;
            case DESTROY:
                EventProcessor.onExit();
                break;
        }
    }
}

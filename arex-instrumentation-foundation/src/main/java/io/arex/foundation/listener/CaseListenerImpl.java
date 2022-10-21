package io.arex.foundation.listener;

public class CaseListenerImpl implements CaseListener {
    public static final CaseListener INSTANCE = new CaseListenerImpl();
    @Override
    public void onEvent(CaseEvent event) {
        switch (event.action) {
            case CREATE:
                CaseInitializer.initialize(String.valueOf(event.getSource()));
                break;
            case DESTROY:
                CaseInitializer.release();
                break;
            default:
                break;
        }
    }
}

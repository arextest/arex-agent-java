package io.arex.foundation.listener;

public class CaseListenerImpl implements CaseListener {
    public static final CaseListener INSTANCE = new CaseListenerImpl();
    @Override
    public void onEvent(CaseEvent e) {
        switch (e.action) {
            case CREATE:
                CaseInitializer.initialize(String.valueOf(e.getSource()));
                break;
            case DESTROY:
                CaseInitializer.release();
                break;
        }
    }
}

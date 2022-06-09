package io.arex.foundation.listener;

import java.util.EventListener;

public interface CaseListener extends EventListener {
    public void onEvent(CaseEvent e);
}

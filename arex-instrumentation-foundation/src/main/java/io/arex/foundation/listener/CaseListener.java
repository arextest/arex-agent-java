package io.arex.foundation.listener;

import java.util.EventListener;

public interface CaseListener extends EventListener {
    void onEvent(CaseEvent e);
}

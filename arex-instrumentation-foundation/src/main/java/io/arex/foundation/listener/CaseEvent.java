package io.arex.foundation.listener;

import java.util.EventObject;

public class CaseEvent extends EventObject {
    public enum Action {CREATE, DESTROY}
    CaseEvent.Action action;
    public CaseEvent(Object source, CaseEvent.Action action) {
        super(source);
        this.action = action;
    }
}

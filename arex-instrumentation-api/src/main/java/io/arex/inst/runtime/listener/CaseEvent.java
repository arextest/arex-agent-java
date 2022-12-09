package io.arex.inst.runtime.listener;

import java.util.EventObject;

public class CaseEvent extends EventObject {
    public enum Action { ENTER, CREATE, DESTROY }
    CaseEvent.Action action;

    private CaseEvent(EventSource source, CaseEvent.Action action) {
        super(source);
        this.action = action;
    }

    public static CaseEvent ofEnterEvent() {
        return new CaseEvent(EventSource.empty(), CaseEvent.Action.ENTER);
    }

    public static CaseEvent ofCreateEvent(EventSource source) {
        return new CaseEvent(source, Action.CREATE);
    }

    public static CaseEvent ofDestroyEvent() {
        return new CaseEvent(EventSource.empty(), CaseEvent.Action.DESTROY);
    }
}

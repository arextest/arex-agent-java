package io.arex.inst.runtime.listener;

import java.util.EventObject;

public class CaseEvent extends EventObject {
    public enum Action {
        /*
         * Mainly used to clean up the previous case on service entrance
         */
        ENTER,
        /*
         * Mainly used to create a new trace for the current case
         */
        CREATE,
        /*
         * record a case
         */
        RECORD,
        /*
         * replay a case
         */
        REPLAY,
        /*
         * Mainly used to clean up the trace of the current case on service exit
         */
        EXIT
    }

    private final CaseEvent.Action action;

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

    public static CaseEvent ofExitEvent() {
        return new CaseEvent(EventSource.empty(), CaseEvent.Action.EXIT);
    }

    public CaseEvent.Action getAction() {
        return action;
    }

    public static CaseEvent ofRecordEvent(EventSource source) {
        return new CaseEvent(source, Action.RECORD);
    }

    public static CaseEvent ofReplayEvent(EventSource source) {
        return new CaseEvent(source, Action.REPLAY);
    }
}

package com.redtop.engaze.common.enums;


public class EventState{
    public static final String TRACKING_ON = "1";
    public static final String TRACKING_OFF = "2";
    public static final String EVENT_END = "3";
    public static final String EVENT_OPEN = "4";
}

/*public enum EventState {
    TRACKING_ON(1), TRACKING_OFF(2), EVENT_END(3), EVENT_OPEN(4), EVENT_UNDEFINED(0);

    private final int state;

    private EventState(int state) {
        this.state = state;
    }

    public int getEventState() {
        return state;
    }

    public static EventState getStatus(int stateID) {
        switch (stateID) {
            case 1:
                return EventState.TRACKING_ON;

            case 2:
                return EventState.TRACKING_OFF;

            case 3:
                return EventState.EVENT_END;

            case 4:
                return EventState.EVENT_OPEN;
            default:
                return EventState.EVENT_UNDEFINED;
        }
    }
}*/

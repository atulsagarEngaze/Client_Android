package com.redtop.engaze.common.enums;

public enum EventState {
    TRACKING_ON(1), TRACKING_OFF(2), EVENT_END(3), EVENT_OPEN(4), EVENT_UNDEFINED(0);

    private final int stateId;

    private EventState(int stateId) {
        this.stateId = stateId;
    }

    public int GetStateId() {
        return this.stateId;
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
}

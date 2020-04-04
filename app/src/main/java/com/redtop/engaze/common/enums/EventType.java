package com.redtop.engaze.common.enums;

public enum EventType {
    SHAREMYLOACTION(100), TRACKBUDDY(200), GENERAL(300);
    private final int eventTypeId;

    private EventType(int eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public int GetEventTypeId() {
        return this.eventTypeId;
    }

    public static EventType getEventType(int eventTypeId) {
        switch (eventTypeId) {
            case 100:
                return EventType.SHAREMYLOACTION;
            case 200:
                return EventType.TRACKBUDDY;
            case 300:
                return EventType.GENERAL;
            default:
                return EventType.GENERAL;
        }
    }
}
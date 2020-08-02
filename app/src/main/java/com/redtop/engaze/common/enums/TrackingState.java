package com.redtop.engaze.common.enums;

public enum TrackingState {
     TRACKING_OFF(1), TRACKING_ON(2),COMPLETED(3);

    private final int stateId;

    private TrackingState(int stateId) {
        this.stateId = stateId;
    }

    public int GetStateId() {
        return this.stateId;
    }


    public static TrackingState getStatus(int stateID) {
        switch (stateID) {
            case 1:
                return TrackingState.TRACKING_OFF;

            case 2:
                return TrackingState.TRACKING_ON;

            case 3:
                return TrackingState.COMPLETED;

            default:
                return TrackingState.TRACKING_OFF;
        }
    }
}

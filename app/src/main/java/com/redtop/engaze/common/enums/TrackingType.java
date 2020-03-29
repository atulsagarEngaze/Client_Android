package com.redtop.engaze.common.enums;

public enum TrackingType {
    SELF(1),BUDDY(0);
    private final int trackingType;
    private TrackingType(int trackingType) {
        this.trackingType = trackingType;
    }
    public int getTrackingType() {
        return this.trackingType;
    }
    public static TrackingType getTrackingType(int trackingTypeId)
    {
        switch(trackingTypeId)
        {
            case 0 :
                return TrackingType.BUDDY;
            case 1:
                return TrackingType.SELF;
            default :
                return TrackingType.SELF;
        }
    }
}
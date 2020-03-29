package com.redtop.engaze.common.enums;

public enum AcceptanceStatus {
    ACCEPTED(1), DECLINED(0),PENDING(-1);

    private final int status;

    private AcceptanceStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public static AcceptanceStatus getStatus(int statusId)
    {
        switch(statusId)
        {

            case 0 :
                return AcceptanceStatus.DECLINED;

            case -1:
                return AcceptanceStatus.PENDING;

            case 1:
                return AcceptanceStatus.ACCEPTED;

            default :
                return AcceptanceStatus.PENDING;

        }
    }
}

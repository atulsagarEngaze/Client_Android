package com.redtop.engaze.common.enums;

import com.google.gson.annotations.Expose;

public enum AcceptanceStatus {
    @Expose
    Accepted(1), Rejected(0), Pending(-1);

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
                return AcceptanceStatus.Rejected;

            case -1:
                return AcceptanceStatus.Pending;

            case 1:
                return AcceptanceStatus.Accepted;

            default :
                return AcceptanceStatus.Pending;

        }
    }
}

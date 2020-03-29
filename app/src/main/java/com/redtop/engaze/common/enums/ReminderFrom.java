package com.redtop.engaze.common.enums;

public enum ReminderFrom{
    NONE(-1), SELF(0), DESTINATION(1),OTHER(2);

    private final int reminderFrom;

    private ReminderFrom(int reminderFrom) {
        this.reminderFrom = reminderFrom;
    }

    public int getDistanceReminderFrom() {
        return reminderFrom;
    }

    public static ReminderFrom getDistanceReminderFrom(int reminderFrom)
    {
        switch(reminderFrom)
        {
            case -1 :
                return ReminderFrom.NONE;

            case 0 :
                return ReminderFrom.SELF;

            case 1:
                return ReminderFrom.DESTINATION;

            default :
                return ReminderFrom.NONE;

        }
    }
}
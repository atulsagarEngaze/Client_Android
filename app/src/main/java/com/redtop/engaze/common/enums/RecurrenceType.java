package com.redtop.engaze.common.enums;

public enum RecurrenceType {
    DAILY(1), WEEKLY(2), MONTHLY(3), UNKNOWN(4);
    private final int recurrenceId;

    private RecurrenceType(int recurrenceId) {
        this.recurrenceId = recurrenceId;
    }

    public int GetRecurrenceTypeId() {
        return this.recurrenceId;
    }

    public static RecurrenceType getRecurrenceType(int recurrenceId) {
        switch (recurrenceId) {
            case 1:
                return RecurrenceType.DAILY;
            case 2:
                return RecurrenceType.WEEKLY;
            case 3:
                return RecurrenceType.MONTHLY;
            default:
                return RecurrenceType.UNKNOWN;
        }
    }
}
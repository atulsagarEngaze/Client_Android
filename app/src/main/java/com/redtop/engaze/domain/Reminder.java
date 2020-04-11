package com.redtop.engaze.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.redtop.engaze.Interface.DataModel;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;

public class Reminder implements Parcelable, DataModel {
    private int timeInterval;
    private String period;
    private String notificationType;
    public long ReminderOffsetInMinute;


    public Reminder(int timeInterval, String period, String notificationType) {
        this.timeInterval = timeInterval;
        this.period = period;
        this.notificationType = notificationType;

    }

    public Reminder() {
    }

    public Reminder(Parcel in) {
        readFromParcel(in);
    }

    public int getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    @Override
    public String toString() {
        return notificationType + "\n";
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeInt(timeInterval);
        dest.writeString(period);
        dest.writeString(notificationType);

    }

    /**
     * * Called from the constructor to create this * object from a parcel. * * @param in parcel from which to re-create object
     */
    private void readFromParcel(Parcel in) {
        timeInterval = in.readInt();
        period = in.readString();
        notificationType = in.readString();
    }

    public static final Creator CREATOR = new Creator() {
        public Reminder createFromParcel(Parcel in) {
            return new Reminder(in);
        }

        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    public static boolean validateReminderInput(Reminder reminder) {
        int userInput = reminder.getTimeInterval();
        switch (reminder.getPeriod()) {
            case "minute":
                if (userInput > 0 && userInput <= AppContext.context.getResources().getInteger(R.integer.event_reminder_start_max_minutes)) {
                    return true;
                } else {
                    Toast.makeText(AppContext.context,
                            AppContext.context.getResources().getString(R.string.message_createEvent_reminderMaxAlert),
                            Toast.LENGTH_LONG).show();
                }
                break;
            case "hour":
                if (userInput > 0 && userInput <= AppContext.context.getResources().getInteger(R.integer.event_reminder_start_max_hours)) {
                    return true;
                } else {
                    Toast.makeText(AppContext.context,
                            AppContext.context.getResources().getString(R.string.message_createEvent_reminderMaxAlert),
                            Toast.LENGTH_LONG).show();
                }
                break;
            case "day":
                if (userInput > 0 && userInput <= AppContext.context.getResources().getInteger(R.integer.event_reminder_start_max_days)) {
                    return true;
                } else {
                    Toast.makeText(AppContext.context,
                            AppContext.context.getResources().getString(R.string.message_createEvent_reminderMaxAlert),
                            Toast.LENGTH_LONG).show();
                }
                break;
            case "week":
                if (userInput > 0 && userInput <= AppContext.context.getResources().getInteger(R.integer.event_reminder_start_max_weeks)) {
                    return true;
                } else {
                    Toast.makeText(AppContext.context,
                            AppContext.context.getResources().getString(R.string.message_createEvent_reminderMaxAlert),
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
        return false;
    }
}

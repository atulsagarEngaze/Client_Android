package com.redtop.engaze.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.redtop.engaze.Interface.DataModel;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;

public class Duration implements Parcelable, DataModel {
    private int timeInterval;
    private String period;
    private Boolean enabled;
    public int OffsetInMinutes;


    public Duration(int timeInterval, String period, Boolean enabled) {
        this.timeInterval = timeInterval;
        this.period = period;
        this.enabled = enabled;


    }

    public Duration() {
    }

    public Duration(Parcel in) {
        readFromParcel(in);
    }

    public Boolean getTrackingState() {
        return enabled;
    }

    public void setTrackingState(Boolean enabled) {
        this.enabled = enabled;
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


    @Override
    public String toString() {
        return period + "\n";
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

    }

    /**
     * * Called from the constructor to create this * object from a parcel. * * @param in parcel from which to re-create object
     */
    private void readFromParcel(Parcel in) {
        timeInterval = in.readInt();
        period = in.readString();
    }

    public static final Creator CREATOR = new Creator() {
        public Duration createFromParcel(Parcel in) {
            return new Duration(in);
        }

        public Duration[] newArray(int size) {
            return new Duration[size];
        }
    };

    public static boolean validateDurationInput(Duration duration) {
        int userInput = duration.getTimeInterval();
        switch (duration.getPeriod()) {
            case "minute":
                if (userInput >= AppContext.context.getResources().getInteger(R.integer.event_creation_duration_min_minutes) && userInput <= AppContext.context.getResources().getInteger(R.integer.event_creation_duration_max_minutes)) {
                    return true;
                }

                break;
            case "hour":
                if (userInput > 0 && userInput <= AppContext.context.getResources().getInteger(R.integer.event_creation_duration_max_hours)) {
                    return true;
                }

                break;
        }
        return false;
    }

    public static boolean validateTrackingInput(Duration duration) {
        int userInput = duration.getTimeInterval();
        switch(duration.getPeriod()){
            case "minute" :
                if(userInput >0 && userInput <= AppContext.context.getResources().getInteger(R.integer.event_tracking_start_max_minutes)){
                    return true;
                }
                else{
                    Toast.makeText(AppContext.context,
                            AppContext.context.getResources().getString(R.string.message_createEvent_trackingStartMaxAlert),
                            Toast.LENGTH_LONG).show();
                }
                break;
            case "hour" :
                if(userInput >0 && userInput <= AppContext.context.getResources().getInteger(R.integer.event_tracking_start_max_hours)){
                    return true;
                }
                else{
                    Toast.makeText(AppContext.context,
                            AppContext.context.getResources().getString(R.string.message_createEvent_trackingStartMaxAlert),
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
        return false;
    }
}

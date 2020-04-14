package com.redtop.engaze.app;

import android.app.Application;

import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.utility.JsonParser;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.ActionHandler;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.Reminder;
import com.redtop.engaze.service.BackgroundServiceManager;

import androidx.appcompat.app.AppCompatActivity;

public class AppContext extends Application {
    public static final String TAG = AppContext.class
            .getSimpleName();

    public boolean isFirstTimeLoading = false;
    public String loginId;
    public String loginName;
    public Boolean isInternetEnabled = true;
    public AppCompatActivity currentActivity;

    public Duration defaultTrackingSettings;
    public Reminder defaultReminderSettings;
    public Duration defaultDurationSettings;
    public static AppContext context;


    public static ActionHandler actionHandler;

    public static JsonParser jsonParser;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        isInternetEnabled = AppUtility.isNetworkAvailable(this);
        //for testing

        loginId = PreffManager.getPref(Constants.LOGIN_ID);
        isFirstTimeLoading = true;
        if (loginId != null) {
            loginName = PreffManager.getPref(Constants.LOGIN_NAME);
            actionHandler = new ActionHandler();
            jsonParser = new JsonParser();

            defaultTrackingSettings = PreffManager.getPrefObject(Constants.DEFAULT_TRACKING_PREF_KEY, Duration.class);
            defaultReminderSettings = PreffManager.getPrefObject(Constants.DEFAULT_REMINDER_PREF_KEY, Reminder.class);
            defaultDurationSettings = PreffManager.getPrefObject(Constants.DEFAULT_DURATION_PREF_KEY, Duration.class);

            //starting the BackgroundServiceManager
            BackgroundServiceManager.startService(context);

        }
    }

    public void setDefaultSetting(){
        setDefaultTrackingSettings();
        setDefaultReminderSettings();
        setDefaultDurationSettings();

        defaultTrackingSettings = PreffManager.getPrefObject(Constants.DEFAULT_TRACKING_PREF_KEY, Duration.class);
        defaultReminderSettings = PreffManager.getPrefObject(Constants.DEFAULT_REMINDER_PREF_KEY, Reminder.class);
        defaultDurationSettings = PreffManager.getPrefObject(Constants.DEFAULT_DURATION_PREF_KEY, Duration.class);
    }

    private void setDefaultReminderSettings() {
        Reminder reminder = new Reminder(Constants.REMINDER_DEFAULT_INTERVAL,
                Constants.REMINDER_DEFAULT_PERIOD,
                Constants.REMINDER_DEFAULT_NOTIFICATION);
        reminder.ReminderOffsetInMinute = Constants.REMINDER_DEFAULT_INTERVAL;

        PreffManager.setPrefObject(Constants.DEFAULT_REMINDER_PREF_KEY, reminder);
    }

    private void setDefaultTrackingSettings() {

        Duration duration = new Duration
                (Constants.TRACKING_DEFAULT_INTERVAL,
                        Constants.TRACKING_DEFAULT_PERIOD,
                        Constants.TRACKING_DEFAULT_ENABLED);
        PreffManager.setPrefObject(Constants.DEFAULT_TRACKING_PREF_KEY, duration);
    }

    private void setDefaultDurationSettings() {

        Duration duration = new Duration
                (Constants.EVENT_DEFAULT_DURATION,
                        Constants.EVENT_DEFAULT_PERIOD,
                        true);
       PreffManager.setPrefObject(Constants.DEFAULT_DURATION_PREF_KEY, duration);
    }

}

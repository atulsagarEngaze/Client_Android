package com.redtop.engaze.app;

import android.app.Application;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.utility.JsonParser;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.ActionHandler;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.Reminder;
import com.redtop.engaze.manager.ContactAndGroupListManager;
import com.redtop.engaze.service.BackgroundLocationService;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AppContext extends Application {
    public static final String TAG = AppContext.class
            .getSimpleName();

    public String loginId;
    public String loginName;
    public Boolean isInternetEnabled = true;
    public AppCompatActivity currentActivity;

    public Duration defaultTrackingSettings;
    public Reminder defaultReminderSettings;
    public Duration defaultDurationSettings;
    public ArrayList<ContactOrGroup> sortedContacts;

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
        jsonParser = new JsonParser();
        if (loginId != null) {
            setDefaultValuesAndStartLocationService();
            AppContext.context.sortedContacts = ContactAndGroupListManager.getSortedContacts();

        }
    }

    public void PerformFirstTimeInitialization(){
        setDefaultTrackingSettings();
        setDefaultReminderSettings();
        setDefaultDurationSettings();
        InternalCaching.initializeCache();
    }

    public void setDefaultValuesAndStartLocationService(){
        loginName = PreffManager.getPref(Constants.LOGIN_NAME);
        actionHandler = new ActionHandler();

        defaultTrackingSettings = PreffManager.getPrefObject(Constants.DEFAULT_TRACKING_PREF_KEY, Duration.class);
        defaultReminderSettings = PreffManager.getPrefObject(Constants.DEFAULT_REMINDER_PREF_KEY, Reminder.class);
        defaultDurationSettings = PreffManager.getPrefObject(Constants.DEFAULT_DURATION_PREF_KEY, Duration.class);
        BackgroundLocationService.start(AppContext.context);

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

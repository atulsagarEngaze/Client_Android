package com.redtop.engaze.app;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.utility.JsonParser;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.ActionHandler;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.Reminder;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.receiver.CurrentLocationUploadService;
import com.redtop.engaze.service.BackgroundServiceManager;
import com.redtop.engaze.service.MyCurrentLocationListener;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

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
    public ArrayList<ContactOrGroup> sortedRegisteredContacts;
    public ArrayList<ContactOrGroup> sortedAllContacts;
    public boolean isContactListUpdated = false;
    public boolean isRegisteredContactListUpdated = false;

    public static AppContext context;
    public static ActionHandler actionHandler;
    public static JsonParser jsonParser;

    private Handler runningEventCheckHandler = null;
    private Runnable runningEventCheckRunnable = null;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        isInternetEnabled = AppUtility.isNetworkAvailable(this);
        //for testing

        loginId = PreffManager.getPref(Constants.LOGIN_ID);
        jsonParser = new JsonParser();
        isFirstTimeLoading = true;
        if (loginId != null) {
            loginName = PreffManager.getPref(Constants.LOGIN_NAME);
            actionHandler = new ActionHandler();


            defaultTrackingSettings = PreffManager.getPrefObject(Constants.DEFAULT_TRACKING_PREF_KEY, Duration.class);
            defaultReminderSettings = PreffManager.getPrefObject(Constants.DEFAULT_REMINDER_PREF_KEY, Reminder.class);
            defaultDurationSettings = PreffManager.getPrefObject(Constants.DEFAULT_DURATION_PREF_KEY, Duration.class);

            StartLocationListenerAndLocationUpdater();
            //starting the BackgroundServiceManager
            //BackgroundServiceManager.startService(context);

        }
    }

    public void setContactList(ArrayList<ContactOrGroup> sortedAllContacts) {
        this.sortedAllContacts = sortedAllContacts;
        isContactListUpdated = true;

    }

    public void setRegisteredContactList(ArrayList<ContactOrGroup> sortedRegisteredContacts) {
        this.sortedRegisteredContacts = sortedRegisteredContacts;
        isRegisteredContactListUpdated = true;
    }

    public void setDefaultSetting() {
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

    private void StartLocationListenerAndLocationUpdater(){

        CurrentLocationUploadService.register(this);
        startLocationListenerService();

        runningEventCheckHandler = new Handler();
        runningEventCheckRunnable = () -> {
            Log.v(TAG, "Running event check callback. Checking for any running event");
            if (EventService.shouldShareLocation()) {
                startLocationListenerService();
            } else {
                stopLocationListenerService();
                }
            runningEventCheckHandler.postDelayed(runningEventCheckRunnable, Config.RUNNING_EVENT_CHECK_INTERVAL);

        };

        runningEventCheckHandler.removeCallbacks(runningEventCheckRunnable);
        runningEventCheckHandler.postDelayed(runningEventCheckRunnable, Config.RUNNING_EVENT_CHECK_INTERVAL);
    }

    public void startLocationListenerService() {

        if (!AppUtility.isBackgroundServiceRunning(MyCurrentLocationListener.class, this)) {
            MyCurrentLocationListener.startService(this);
        }
    }

    public void stopLocationListenerService() {

        if (AppUtility.isBackgroundServiceRunning(MyCurrentLocationListener.class, this)) {
            MyCurrentLocationListener.stopService(this);
        }
    }

}

package com.redtop.engaze.service;

import java.util.Hashtable;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.redtop.engaze.Interface.OnRefreshMemberListCompleteListner;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.Reminder;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;

public class FirstTimeInitializationService extends IntentService {

    private static final String TAG = FirstTimeInitializationService.class.getName();
    ;
    private Context mContext;

    public FirstTimeInitializationService() {
        super(TAG);
        Log.d(TAG, "Constructor");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = this;
        //EventService.setLocationServiceCheckAlarm();
        setDefaultReminderSettings();
        setDefaultTrackingSettings();
        setDefaultDurationSettings();
        initializeContactList();
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

    private void initializeContactList() {
        try {

            InternalCaching.initializeCache();
            PreffManager.setPrefBoolean(Constants.IS_REGISTERED_CONTACT_LIST_INITIALIZED, false);

            ContactAndGroupListManager.cacheContactAndGroupList(new OnRefreshMemberListCompleteListner() {

                @Override
                public void RefreshMemberListComplete(Hashtable<String, ContactOrGroup> memberList) {
                    PreffManager.setPrefBoolean(Constants.IS_REGISTERED_CONTACT_LIST_INITIALIZED, true);

                }
            }, new OnRefreshMemberListCompleteListner() {

                @Override
                public void RefreshMemberListComplete(Hashtable<String, ContactOrGroup> memberList) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

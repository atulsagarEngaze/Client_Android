package com.redtop.engaze.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.domain.manager.EventManager;

public class EventRefreshService extends IntentService {

    private static final String TAG = "EventRefreshService";

    public EventRefreshService() {
        super(TAG);
        Log.i(TAG, "Constructor EventRefreshService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Context context = this;
        EventManager.refreshEventList(eventList -> {
            Intent eventRefreshedIntent = new Intent(Veranstaltung.EVENTS_REFRESHED);
            LocalBroadcastManager.getInstance(context).sendBroadcast(eventRefreshedIntent);

        }, (msg, action) -> {
            Log.i(TAG, "Event refresh action failed");
        });
    }
}

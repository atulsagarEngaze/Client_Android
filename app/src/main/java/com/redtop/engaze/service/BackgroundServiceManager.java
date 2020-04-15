package com.redtop.engaze.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.DurationConstants;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.receiver.CurrentLocationUploadService;

//this service upload the current address to server to be available to other users in the event
public class BackgroundServiceManager extends Service {


    private boolean isLocationNeededInApp = true;
    private Context serviceContext;

    public static final String TAG = BackgroundServiceManager.class.getName();


    private Handler runningEventCheckHandler = null;
    private Runnable runningEventCheckRunnable = null;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "\n Background service manger created ");
        serviceContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.v(TAG, "Background service manger started");
        startBackgroundServices();
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "Destroy Background service manger");

        runningEventCheckHandler.removeCallbacks(runningEventCheckRunnable);
        Log.v(TAG, "Destroy Running event check callback");
    }

    public synchronized static void startService(Context context) {

        context.startService(new Intent(context, BackgroundServiceManager.class));
    }

    public synchronized static void stopService(Context context) {

        context.startService(new Intent(context, BackgroundServiceManager.class));
    }

    private void startBackgroundServices() {
        handleLocationListenerService();
    }

    private void handleLocationListenerService() {

        CurrentLocationUploadService.register(this);

        startLocationListenerService();

        runningEventCheckHandler = new Handler();
        runningEventCheckRunnable = () -> {
            Log.v(TAG, "Running event check callback. Checking for any running event");
            if (EventService.shouldShareLocation() || isLocationNeededInApp) {
                startLocationListenerService();
            } else {
                stopLocationListenerService();
            }
            runningEventCheckHandler.postDelayed(runningEventCheckRunnable, DurationConstants.RUNNING_EVENT_CHECK_INTERVAL);

        };

        runningEventCheckHandler.removeCallbacks(runningEventCheckRunnable);
        runningEventCheckHandler.postDelayed(runningEventCheckRunnable, DurationConstants.RUNNING_EVENT_CHECK_INTERVAL);
    }


    private void startLocationListenerService() {

        if (!AppUtility.isBackgroundServiceRunning(MyCurrentLocationListener.class, this)) {
            MyCurrentLocationListener.startService(this);
        }
    }

    private void stopLocationListenerService() {

        if (!AppUtility.isBackgroundServiceRunning(MyCurrentLocationListener.class, this)) {
            MyCurrentLocationListener.stopService(this);
        }
    }
}

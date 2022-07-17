package com.redtop.engaze.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.redtop.engaze.HomeActivity;
import com.redtop.engaze.R;
import com.redtop.engaze.app.Config;
import com.redtop.engaze.domain.service.EventService;

//this service upload the current address to server to be available to other users in the event
public class BackgroundLocationService extends Service {


    private static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationupdatesforegroundservice";

    private static final String TAG = BackgroundLocationService.class.getSimpleName();

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_01";

    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */



    public static boolean isServiceRunning = false;


    private Handler runningEventCheckHandler = null;
    private Runnable runningEventCheckRunnable = null;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "\n BackgroundLocationService created ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "BackgroundLocationService started");
        startForeground(NOTIFICATION_ID, getNotification());
        handleLocationListenerService();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroy BackgroundLocationService");

        if(runningEventCheckHandler!=null) {
          runningEventCheckHandler.removeCallbacks(runningEventCheckRunnable);
        }
        Log.i(TAG, "Destroy Running event check callback");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification getNotification() {
        createNotificationChannel();
        CharSequence text = "Tap to view running events";

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, HomeActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(activityPendingIntent)
                .setContentText(text)
                .setContentTitle("Your current location is being shared")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.logo_notification)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    public synchronized static void start(Context context) {
        if(!isServiceRunning && EventService.shouldShareLocation()) {
            context.startForegroundService(new Intent(context, BackgroundLocationService.class));
            isServiceRunning = true;
        }
    }

    public synchronized static void stop(Context context) {
        if(isServiceRunning) {
            context.stopService(new Intent(context, BackgroundLocationService.class));
            isServiceRunning = false;
        }
    }


    private void handleLocationListenerService() {

        runningEventCheckHandler = new Handler();
        runningEventCheckRunnable = () -> {
            Log.v(TAG, "Running event check callback. Checking for any running event");
            if (EventService.shouldShareLocation()) {
                MyCurrentLocationManager.startLocationUpdates(this);
            } else {
                Log.v(TAG, "No running events. Shutting down background service");
                MyCurrentLocationManager.stopLocationUpdates(this);
                BackgroundLocationService.stop(this);
            }
            runningEventCheckHandler.postDelayed(runningEventCheckRunnable, Config.RUNNING_EVENT_CHECK_INTERVAL);

        };

        runningEventCheckHandler.removeCallbacks(runningEventCheckRunnable);
        runningEventCheckHandler.postDelayed(runningEventCheckRunnable, Config.RUNNING_EVENT_CHECK_INTERVAL);
    }


    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }
}

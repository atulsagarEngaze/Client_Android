package com.redtop.engaze.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.IntentConstants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.app.Config;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

//this service upload the current address to server to be available to other users in the event
public class MyCurrentLocationListener extends Service {

    private static Boolean isFirstLocationRequiredForNewEvent = false;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mFilter;
    private Location mLocation = null;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final String TAG = MyCurrentLocationListener.class.getName();

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "\n LocationUpdatorService created ");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocalLocationRequestReceiver();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                onLocationChanged(locationResult.getLocations().get(0));
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Location Updator Service started");

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                mFilter);

        return START_STICKY;
    }

    public void createLocalLocationRequestReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                // TODO Auto-generated method stub
                {
                    if (mLocation == null) {
                        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                            mLocation = location;
                            broadcastCurrentLocation(mLocation);
                        });
                    } else {
                        broadcastCurrentLocation(mLocation);
                    }
                }
            }
        };

        mFilter = new IntentFilter();
        mFilter.addAction(Veranstaltung.NEED_LOCATION_INSTANT);
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(Config.LOCATION_REFRESH_INTERVAL_NORMAL);
        locationRequest.setFastestInterval(Config.LOCATION_REFRESH_INTERVAL_FAST);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(AppContext.context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "Destroy Location Updator Service");
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Log.v(TAG, "Destroy Running event check callback");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    public void onLocationChanged(Location location) {
        mLocation = location;
        broadcastCurrentLocation(location);
    }

    private void broadcastCurrentLocation(Location location) {
        Intent intent = new Intent(Veranstaltung.CURRENT_LOCATION_FOUND);
        intent.putExtra(IntentConstants.CURRENT_LOCATION, location);
        LocalBroadcastManager.getInstance(AppContext.context).sendBroadcast(intent);
    }

    public synchronized static void startService(Context context) {

        context.startService(new Intent(context, MyCurrentLocationListener.class));
    }

    public synchronized static void stopService(Context context) {

        context.startService(new Intent(context, MyCurrentLocationListener.class));
    }
}

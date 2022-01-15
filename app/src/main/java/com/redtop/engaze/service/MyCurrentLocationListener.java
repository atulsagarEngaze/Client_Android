package com.redtop.engaze.service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
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
import com.google.android.gms.tasks.Task;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.IntentConstants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.app.Config;
import com.redtop.engaze.receiver.CurrentLocationUploadService;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

//this service upload the current address to server to be available to other users in the event
public class MyCurrentLocationListener {

    public static boolean IsLocationServiceRunning = false;
    private static FusedLocationProviderClient mFusedLocationProviderClient = null;
    private static LocationCallback mLocationCallback;
    private static BroadcastReceiver mBroadcastReceiver;
    private static final String TAG = MyCurrentLocationListener.class.getName();
    private static CurrentLocationUploadService mCurrentLocationUploadService;

    public MyCurrentLocationListener() {
        super();
    }

    @SuppressLint("MissingPermission")
    public synchronized static void startService(Context context) {
        if (mFusedLocationProviderClient == null) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            LocationRequest locationRequest = createLocationRequest();

            createLocalLocationRequestReceiver(context);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    Location location = locationResult.getLocations().get(0);
                    Log.d(TAG, "Current location is");
                    Log.d(TAG, location.toString());
                    broadcastCurrentLocation(location, context);
                }
            };

            mFusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    mLocationCallback,
                    Looper.getMainLooper());
        }
        if(mCurrentLocationUploadService==null){
            mCurrentLocationUploadService = new CurrentLocationUploadService();
        }

        IsLocationServiceRunning = true;
    }

    public synchronized static void stopService(Context context) {
        if (mFusedLocationProviderClient != null && mLocationCallback != null) {
            Log.v(TAG, "Destroy Running event check callback");
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver);
        }
        mFusedLocationProviderClient = null;
        mLocationCallback = null;
        mBroadcastReceiver = null;
        mCurrentLocationUploadService = null;
        IsLocationServiceRunning = false;

    }

    public static void createLocalLocationRequestReceiver(Context ctx) {
        mBroadcastReceiver = new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(final Context context, Intent intent) {
                // TODO Auto-generated method stub
                {
                    mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                        Log.d(TAG, "Current location is");
                        Log.d(TAG, location.toString());
                        broadcastCurrentLocation(location, ctx);
                    });

                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Veranstaltung.NEED_LOCATION_INSTANT);

        LocalBroadcastManager.getInstance(ctx).registerReceiver(mBroadcastReceiver,
                filter);
    }

    private static void broadcastCurrentLocation(Location location, Context contex) {
        Intent intent = new Intent(Veranstaltung.CURRENT_LOCATION_FOUND);
        intent.putExtra(IntentConstants.CURRENT_LOCATION, location);
        LocalBroadcastManager.getInstance(AppContext.context).sendBroadcast(intent);

        mCurrentLocationUploadService.onCurrentLocationReceived(location, contex);
    }

    protected static LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(Config.LOCATION_REFRESH_INTERVAL_NORMAL);
        locationRequest.setFastestInterval(Config.LOCATION_REFRESH_INTERVAL_FAST);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(AppContext.context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        });

        task.addOnFailureListener(e -> {

        });

        return locationRequest;
    }
}

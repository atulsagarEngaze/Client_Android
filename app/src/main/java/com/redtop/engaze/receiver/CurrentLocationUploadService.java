package com.redtop.engaze.receiver;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;

import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.app.Config;
import com.redtop.engaze.common.constant.IntentConstants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.manager.LocationManager;
import com.redtop.engaze.domain.service.EventService;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class CurrentLocationUploadService extends LocalBroadcastReceiver {

    public RunningEventActivity activity;
    private Boolean isUpdateInProgress = false;
    private Location lastLocation = null;

    public CurrentLocationUploadService(Context context) {
        super(context);
    }

    public static void register(Context context){

        CurrentLocationUploadService locationReceiver = new CurrentLocationUploadService(context);
        locationReceiver.mFilter = new IntentFilter();
        locationReceiver.mFilter.addAction(Veranstaltung.CURRENT_LOCATION_FOUND);
        LocalBroadcastManager.getInstance(context).registerReceiver(locationReceiver, locationReceiver.mFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Location currentLocation = intent.getParcelableExtra(IntentConstants.CURRENT_LOCATION);
        onCurrentLocationReceived(currentLocation, context);
    }

    private void onCurrentLocationReceived(Location currentLocation, Context context) {

        if (isUpdateInProgress || currentLocation == null || !EventService.shouldShareLocation()) {
            return;
        }

        if (lastLocation != null) {
            if (lastLocation.distanceTo(currentLocation) > Config.MIN_DISTANCE_IN_METER_LOCATION_UPDATE) {
                updateCurrentLocationToServer(currentLocation, context);
            }
        } else {
            updateCurrentLocationToServer(currentLocation, context);
        }
    }

    private void updateCurrentLocationToServer(final Location currentLocation, final Context context) {

        LocationManager.updateLocationToServer(context, currentLocation, PreffManager.getPref(Constants.LOGIN_ID), response -> {
            isUpdateInProgress = false;
            lastLocation = currentLocation;
        }, (msg, action) -> isUpdateInProgress = false);
    }

}

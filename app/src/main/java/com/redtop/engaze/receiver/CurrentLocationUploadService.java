package com.redtop.engaze.receiver;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.MyCurrentLocationHandlerActivity;
import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.app.Config;
import com.redtop.engaze.common.constant.IntentConstants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.common.utility.AppLocationService;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.domain.manager.LocationManager;
import com.redtop.engaze.domain.service.EventService;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class CurrentLocationUploadService extends LocalBroadcastReceiver {

    public RunningEventActivity activity;
    private Boolean isUpdateInProgress = false;
    private Location lastLocation = null;
    private UsersLocationDetail lastLocationUserLocationDetail = null;
    protected AppLocationService mLh;

    public CurrentLocationUploadService(Context context) {
        super(context);
        mLh = new AppLocationService(context);
    }

    public static void register(Context context) {

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

        if (lastLocation != null && lastLocationUserLocationDetail != null) {
            if (lastLocation.distanceTo(currentLocation) > Config.MIN_DISTANCE_IN_METER_LOCATION_UPDATE ||
                    (lastLocationUserLocationDetail.address == Constants.LOCATION_UNKNOWN || lastLocationUserLocationDetail.name == Constants.LOCATION_UNKNOWN)
            ) {
                updateCurrentLocationToServer(currentLocation, context);
            }
        } else {
            updateCurrentLocationToServer(currentLocation, context);
        }
    }

    private void updateCurrentLocationToServer(final Location currentLocation, final Context context) {
        Place place = mLh.getPlaceFromLatLang(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        UsersLocationDetail locationDetail = new UsersLocationDetail(PreffManager.getPref(Constants.LOGIN_ID), currentLocation.getLatitude(), currentLocation.getLongitude(), "1.0", "0");
        if (place != null) {
            locationDetail.address = place.getAddress();
            locationDetail.name = place.getName();
            if (locationDetail.address == null || locationDetail.address == "") {
                locationDetail.address = Constants.LOCATION_UNKNOWN;
            }
            if (locationDetail.name == null || locationDetail.name == "") {
                locationDetail.name = Constants.LOCATION_UNKNOWN;
            }
        }
        LocationManager.updateLocationToServer(context, locationDetail, new OnAPICallCompleteListener() {
            @Override
            public void apiCallSuccess(Object response) {
                isUpdateInProgress = false;
                lastLocation = currentLocation;
                lastLocationUserLocationDetail = locationDetail;
            }

            @Override
            public void apiCallFailure() {
                isUpdateInProgress = false;
            }
        });
    }

}

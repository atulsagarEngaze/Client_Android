package com.redtop.engaze.service;

import android.content.Context;
import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.AppLocationService;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.domain.manager.LocationManager;


public class CurrentLocationUploadService {

    public RunningEventActivity activity;
    private Boolean isUpdateInProgress = false;
    protected AppLocationService mLh;
    public void onCurrentLocationReceived(Location currentLocation, Context context) {

        if(mLh==null){
            mLh = new AppLocationService(context);
        }
        if (isUpdateInProgress || currentLocation == null || !EventManager.shouldShareLocation()) {
            return;
        }

        updateCurrentLocationToServer(currentLocation, context);

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
            }

            @Override
            public void apiCallFailure() {
                isUpdateInProgress = false;
            }
        });
    }

}

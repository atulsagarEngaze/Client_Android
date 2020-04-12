package com.redtop.engaze.manager;

import android.location.Location;
import android.util.Log;
import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.webservice.ILocationWS;
import com.redtop.engaze.webservice.LocationWS;
import com.redtop.engaze.webservice.proxy.LocationWSProxy;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationManager {

    private final static String TAG = LocationManager.class.getName();
    private final static ILocationWS locationWS = new LocationWSProxy();

    public static void updateLocationToServer(final Location location, final OnAPICallCompleteListner listnerOnSuccess,
                                              final OnActionFailedListner listnerOnFailure ) {

        if(!AppContext.context.isInternetEnabled){
            Log.d(TAG, "No internet connection. Aborting location update to server.");
            return;
        }

        LocationWSProxy.location = location;
        UsersLocationDetail usersLocationDetail = new UsersLocationDetail(
                AppContext.context.loginId,
                location.getLatitude(),
                location.getLongitude(), "1.0","0");

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(AppContext.jsonParser.Serialize(usersLocationDetail));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        locationWS.updateLocation( jsonObject, new OnAPICallCompleteListner() {
            @Override
            public void apiCallComplete(JSONObject response) {
                listnerOnSuccess.apiCallComplete(response);
            }
        }, new OnAPICallCompleteListner() {
            @Override
            public void apiCallComplete(JSONObject response) {
                listnerOnFailure.actionFailed(null, Action.SAVEEVENTSHAREMYLOCATION);
            }
        });
    }

    public static void getLocationsFromServer(String userId, String eventId,
                                              final OnAPICallCompleteListner listnerOnSuccess,
                                              final OnAPICallCompleteListner listnerOnFailure){



        locationWS.getLocationsFromServer(userId, eventId, new OnAPICallCompleteListner() {
            @Override
            public void apiCallComplete(JSONObject response) {
                listnerOnSuccess.apiCallComplete(response);
            }
        }, new OnAPICallCompleteListner() {
            @Override
            public void apiCallComplete(JSONObject response) {
                listnerOnFailure.apiCallComplete(response);
            }
        });

    }
}
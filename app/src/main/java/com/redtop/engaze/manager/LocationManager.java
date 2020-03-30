package com.redtop.engaze.manager;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.AppService;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.webservice.LocationWS;

import org.json.JSONObject;

public class LocationManager {

    private final static String TAG = LocationManager.class.getName();

    public static void updateLocationToServer(Context context, final Location location, final OnAPICallCompleteListner listnerOnSuccess,
                                              final OnActionFailedListner listnerOnFailure ) {

        if(!AppService.isNetworkAvailable(context)){
            Log.d(TAG, "No internet connection. Aborting location update to server.");
            return;
        }
        String tag_json_obj = "json_obj_post_user_location";
        JSONObject jobj = new JSONObject();

        try {
            jobj.put("UserId", AppContext.getInstance().loginId);
            jobj.put("Latitude", "" + location.getLatitude());
            jobj.put("Longitude", "" + location.getLongitude());
            jobj.put("ETA", "1.0");
            jobj.put("ArrivalStatus", "0");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to update location");
        }

        LocationWS.updateLocation(context,  jobj, new OnAPICallCompleteListner() {
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
}
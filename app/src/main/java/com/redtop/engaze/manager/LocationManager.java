package com.redtop.engaze.manager;

import android.content.Context;
import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.JsonParser;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.restApi.ILocationApi;
import com.redtop.engaze.restApi.LocationApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationManager {

    private final static String TAG = LocationManager.class.getName();
    private final static ILocationApi locationApi = new LocationApi();

    public static void updateLocationToServer(final Context context, final UsersLocationDetail location, final OnAPICallCompleteListener onAPICallCompleteListener) {
        //this will be called from background service, app may not be running at that time
        if (!AppUtility.isNetworkAvailable(context)) {
            Log.d(TAG, "No internet connection. Aborting location update to server.");
            return;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(new JsonParser().Serialize(location));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        locationApi.updateLocation(jsonObject, new OnAPICallCompleteListener<JSONObject>() {
            @Override
            public void apiCallSuccess(JSONObject response) {
                onAPICallCompleteListener.apiCallSuccess(response);
            }

            @Override
            public void apiCallFailure() {
                onAPICallCompleteListener.apiCallFailure();
            }
        });
    }

    public static void getLocationsFromServer(String userId, String eventId,
                                              final OnAPICallCompleteListener onAPICallCompleteListener) {


        locationApi.getLocationsFromServer(userId, eventId, new OnAPICallCompleteListener<String>() {
            @Override
            public void apiCallSuccess(String response) {

                JSONArray jsonArrayResult = null;
                try {
                    jsonArrayResult = new JSONArray(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                onAPICallCompleteListener.apiCallSuccess( jsonArrayResult);
            }

            @Override
            public void apiCallFailure() {
                onAPICallCompleteListener.apiCallFailure();
            }
        });

    }
}
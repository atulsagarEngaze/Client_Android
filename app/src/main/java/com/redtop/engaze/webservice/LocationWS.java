package com.redtop.engaze.webservice;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.app.AppContext;

import org.json.JSONObject;

public class LocationWS extends BaseWebService {

    public static void updateLocation(JSONObject jsonObject,
                                      final OnAPICallCompleteListner listnerOnSuccess,
                                      final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String url = MAP_API_URL + Routes.USER_LOCATION_UPLOAD;

            Log.d(TAG, "Calling URL:" + url);

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public static void getLocationsFromServer(String userId, String eventId,
                                              final OnAPICallCompleteListner listnerOnSuccess,
                                              final OnAPICallCompleteListner listnerOnFailure) {
        try {

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("RequestorId", userId);
            jsonObject.put("EventId", eventId);

            String url = MAP_API_URL + Routes.USER_LOCATION;
            Log.d(TAG, "Calling URL:" + url);

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }
}

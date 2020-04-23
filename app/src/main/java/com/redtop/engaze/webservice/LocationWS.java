package com.redtop.engaze.webservice;

import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListner;

import org.json.JSONObject;

public class LocationWS extends BaseWebService implements ILocationWS {

    public void updateLocation(JSONObject jsonObject,
                                      final OnAPICallCompleteListner listnerOnSuccess,
                                      final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String url = MAP_API_URL + ApiUrl.USER_LOCATION_UPLOAD;

            Log.d(TAG, "Calling URL:" + url);

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void getLocationsFromServer(String userId, String eventId,
                                              final OnAPICallCompleteListner listnerOnSuccess,
                                              final OnAPICallCompleteListner listnerOnFailure) {
        try {

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("RequestorId", userId);
            jsonObject.put("EventId", eventId);

            String url = MAP_API_URL + ApiUrl.USER_LOCATION;
            Log.d(TAG, "Calling URL:" + url);

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }
}

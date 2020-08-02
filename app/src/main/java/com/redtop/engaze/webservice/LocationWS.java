package com.redtop.engaze.webservice;

import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;

import org.json.JSONObject;

public class LocationWS extends BaseWebService implements ILocationWS {

    public void updateLocation(JSONObject jsonObject,
                                      final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            String url = MAP_API_URL + ApiUrl.USER_LOCATION_UPLOAD;

            Log.d(TAG, "Calling URL:" + url);

            postData(jsonObject, url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

    public void getLocationsFromServer(String userId, String eventId,
                                              final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("RequestorId", userId);
            jsonObject.put("EventId", eventId);

            String url = MAP_API_URL + ApiUrl.USER_LOCATION;
            Log.d(TAG, "Calling URL:" + url);

            postData(jsonObject, url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }
}

package com.redtop.engaze.webservice;

import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.app.AppContext;

import org.json.JSONObject;

public class LocationWS extends BaseWebService implements ILocationWS {

    public void updateLocation(JSONObject jsonObject,
                               final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            String url = ApiUrl.UPLOAD_USER_LOCATION.replace("{userId}", AppContext.context.loginId);
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
            String url = ApiUrl.FETCH_USER_LOCATION.replace("{eventId}", eventId).replace("{requesterId}", userId);
            getData(url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }
}

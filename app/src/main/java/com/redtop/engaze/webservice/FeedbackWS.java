package com.redtop.engaze.webservice;

import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListner;

import org.json.JSONObject;

public class FeedbackWS extends BaseWebService {

    private final static String TAG = SmsWS.class.getName();

    public static void saveFeedback( JSONObject jsonObject,
                                                    OnAPICallCompleteListner listnerOnSuccess,
                                                    OnAPICallCompleteListner listnerOnFailure) {
        try {

            String url = MAP_API_URL + Routes.SAVE_FEEDBACK;

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }
}

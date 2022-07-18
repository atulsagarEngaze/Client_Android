package com.redtop.engaze.webservice;

import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;

import org.json.JSONObject;

public class FeedbackWS extends BaseWebService {

    private final static String TAG = SmsWS.class.getName();

    public static void saveFeedback( JSONObject jsonObject,
                                                    OnAPICallCompleteListener onAPICallCompleteListener
                                                    ) {
        try {

            String url = MAP_API_URL + ApiUrl.SAVE_FEEDBACK;

            postData(jsonObject, url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }
}

package com.redtop.engaze.restApi;

import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;

import org.json.JSONObject;

public class FeedbackWS extends BaseWebApi {

    private final static String TAG = SmsApi.class.getName();

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

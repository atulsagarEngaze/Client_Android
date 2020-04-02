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

import org.json.JSONObject;

public class ProfileWS extends BaseWebService {

    private final static String TAG = SmsWS.class.getName();

    public static void saveProfile(Context context, JSONObject jRequestobj,
                                   final OnAPICallCompleteListner listnerOnSuccess,
                                   final OnAPICallCompleteListner listnerOnFailure) {
        try
        {
            String apiUrl = MAP_API_URL + Routes.ACCOUNT_REGISTER;

            postData(jRequestobj,apiUrl, listnerOnSuccess, listnerOnFailure);
        }
        catch(Exception ex){
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }

    }

}

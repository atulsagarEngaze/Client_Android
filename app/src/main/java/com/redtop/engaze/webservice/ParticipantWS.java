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
import com.redtop.engaze.common.constant.DurationConstants;

import org.json.JSONObject;

public class ParticipantWS extends BaseWebService {

    private final static String TAG = ParticipantWS.class.getName();

    public static void pokeParticipants(Context context, JSONObject pokeAllContactsJSON,
                                        final OnAPICallCompleteListner listnerOnSuccess,
                                        final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String JsonPostURL = MAP_API_URL + Routes.POKEALL_CONTACTS;
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    JsonPostURL, pokeAllContactsJSON, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                    listnerOnSuccess.apiCallComplete(response);


                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Volley Error: " + error.getMessage());
                    listnerOnFailure.apiCallComplete(null);

                }
            })
            {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            jsonObjReq.setRetryPolicy((RetryPolicy) new DefaultRetryPolicy(DurationConstants.DEFAULT_SHORT_TIME_TIMEOUT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Adding request to request queue
            addToRequestQueue(jsonObjReq, context);
        }
        catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }

    }

    public static void addRemoveParticipants(JSONObject jsonObject, final Context context, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String url = MAP_API_URL + Routes.UPDATE_PARTICIPANTS;

            postData(context, jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }
}

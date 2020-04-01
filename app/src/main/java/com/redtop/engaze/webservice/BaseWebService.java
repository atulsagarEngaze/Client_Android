package com.redtop.engaze.webservice;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.app.AppContext;

import org.json.JSONObject;

public abstract class BaseWebService {

    public static final String TAG = BaseWebService.class
            .getSimpleName();

    private static RequestQueue mRequestQueue;
    public static final String MAP_API_URL = "http://redtopdev.com/CoordifyAPI/api/";

    public static final int DEFAULT_SHORT_TIME_TIMEOUT = 20000;//millisecond
    public static final int DEFAULT_MEDIUM_TIME_TIMEOUT = 40000;//millisecond

    protected static RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }

        return mRequestQueue;
    }

    protected static void callAPI(JSONObject jRequestobj, String url, int method,
                                  final OnAPICallCompleteListner listnerOnSuccess,
                                  final OnAPICallCompleteListner listnerOnFailure) {

        Log.d(TAG, "Calling URL:" + url);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, jRequestobj, new Response.Listener<JSONObject>() {

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
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        jsonObjReq.setRetryPolicy((RetryPolicy) new DefaultRetryPolicy(DEFAULT_MEDIUM_TIME_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        addToRequestQueue(jsonObjReq, AppContext.context);

    }

    protected static void getData(JSONObject jRequestobj, String url,
                                  final OnAPICallCompleteListner listnerOnSuccess,
                                  final OnAPICallCompleteListner listnerOnFailure) {

        callAPI(jRequestobj, url, Request.Method.GET, listnerOnSuccess, listnerOnFailure);

    }


    protected static void postData(JSONObject jRequestobj, String url,
                                   final OnAPICallCompleteListner listnerOnSuccess,
                                   final OnAPICallCompleteListner listnerOnFailure) {
        callAPI(jRequestobj, url, Request.Method.POST, listnerOnSuccess, listnerOnFailure);
    }

    protected static <T> void addToRequestQueue(Request<T> req, String tag, Context context) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue(context).add(req);
    }

    protected static <T> void addToRequestQueue(Request<T> req, Context context) {
        req.setTag(TAG);
        getRequestQueue(context).add(req);
    }

    protected static void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}

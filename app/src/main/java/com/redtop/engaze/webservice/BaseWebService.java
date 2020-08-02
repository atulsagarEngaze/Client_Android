package com.redtop.engaze.webservice;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.app.AppContext;

import org.json.JSONObject;

public abstract class BaseWebService {

    public static final String TAG = BaseWebService.class
            .getSimpleName();

    private static RequestQueue mRequestQueue;
    public static final String MAP_API_URL = "http://127.0.0.1:8080/";

    public static final int DEFAULT_SHORT_TIME_TIMEOUT = 20000;//millisecond
    public static final int DEFAULT_MEDIUM_TIME_TIMEOUT = 40000;//millisecond

    protected static RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }

        return mRequestQueue;
    }

    protected static void getData(String url,
                                  final OnAPICallCompleteListener callCompleteListener) {


        Log.d(TAG, "Calling URL:" + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            Log.d(TAG, response);
            callCompleteListener.apiCallSuccess(response);

        }, error -> {
            Log.d(TAG, "Volley Error: " + error.getMessage());
            callCompleteListener.apiCallFailure();

        });
        stringRequest.setRetryPolicy((RetryPolicy) new DefaultRetryPolicy(DEFAULT_MEDIUM_TIME_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        addToRequestQueue(stringRequest, AppContext.context);
    }

    protected static void postData(JSONObject jRequestobj, String url,
                                   final OnAPICallCompleteListener callCompleteListener) {
        Log.d(TAG, "Calling URL:" + url);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, jRequestobj, (Response.Listener<JSONObject>) response -> {
            Log.d(TAG, response.toString());
            callCompleteListener.apiCallSuccess(response);
        }, (Response.ErrorListener) error -> {
            Log.d(TAG, "Volley Error: " + error.getMessage());
            callCompleteListener.apiCallFailure();
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        jsonObjReq.setRetryPolicy(GetDefaultReTryPolicy());
        // Adding request to request queue
        addToRequestQueue(jsonObjReq, AppContext.context);
    }

    private static RetryPolicy GetDefaultReTryPolicy() {
        return new DefaultRetryPolicy(DEFAULT_MEDIUM_TIME_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
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

package com.redtop.engaze.restApi;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.app.AppContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public abstract class BaseWebApi {

    public static final String TAG = BaseWebApi.class
            .getSimpleName();

    private static RequestQueue mRequestQueue;
    public static final String MAP_API_URL = "http://127.0.0.1:6000/";

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

    protected static void putData(JSONObject jRequestobj, String url,
                                   final OnAPICallCompleteListener callCompleteListener) {
        callAPI(jRequestobj,url,callCompleteListener,Request.Method.PUT);

    }

    protected static void postData(JSONObject jRequestobj, String url,
                                  final OnAPICallCompleteListener callCompleteListener) {
        callAPI(jRequestobj,url,callCompleteListener,Request.Method.POST);

    }

    private static void callAPI(JSONObject jRequestobj, String url,
                                final OnAPICallCompleteListener callCompleteListener, int httpMethod){
        Log.d(TAG, "Calling URL:" + url);
        if(jRequestobj!=null){
            Log.d(TAG, "Request Body:" + jRequestobj.toString());
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(httpMethod,
                url, jRequestobj, (Response.Listener<JSONObject>) response -> {
            callCompleteListener.apiCallSuccess(response);
        }, (ErrorListener) error -> {
            Log.d(TAG, "Volley Error: " + error.getMessage());
            callCompleteListener.apiCallFailure();
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

                    JSONObject result = null;

                    if (jsonString != null && jsonString.length() > 0) {
                        Log.d(TAG, jsonString);
                        result = new JSONObject(jsonString);
                    }

                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch ( JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };
        jsonObjReq.setRetryPolicy(GetDefaultReTryPolicy());
        // Adding request to request queue
        addToRequestQueue(jsonObjReq, AppContext.context);
    }


    protected static void postDataArrayResponse(JSONObject jRequestobj, String url,
                                   final OnAPICallCompleteListener callCompleteListener) {
        Log.d(TAG, "Calling URL:" + url);
        if(jRequestobj!=null){
            Log.d(TAG, "Request Body:" + jRequestobj.toString());
        }
        JsonRequest<JSONArray> jsonObjReq = new JsonRequest<JSONArray>(Request.Method.POST,
                url, jRequestobj.toString(), (Response.Listener<JSONArray>) response -> {
            callCompleteListener.apiCallSuccess(response);
        }, (ErrorListener) error -> {
            Log.d(TAG, "Volley Error: " + error.getMessage());
            callCompleteListener.apiCallFailure();
        }) {

            @Override
            public byte[] getBody() {
                return jRequestobj.toString().getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonStringArray = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

                    JSONArray result = null;

                    if (jsonStringArray != null && jsonStringArray.length() > 0) {
                        Log.d(TAG, jsonStringArray);
                        result = new JSONArray(jsonStringArray);
                    }

                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch ( JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };
        jsonObjReq.setRetryPolicy(GetDefaultReTryPolicy());
        // Adding request to request queue
        addToRequestQueue(jsonObjReq, AppContext.context);
    }


    protected static void postArrayDataStringResponse(JSONArray  jRequestArray, String url,
                                                      final OnAPICallCompleteListener callCompleteListener){
        if(jRequestArray!=null){
            Log.d(TAG, "Request Body:" + jRequestArray.toString());
        }
        callAPIStringResponse(jRequestArray.toString().getBytes(), url, callCompleteListener, Request.Method.POST);
    }

    protected static void putArrayDataStringResponse(JSONArray  jRequestArray, String url,
                                                     final OnAPICallCompleteListener callCompleteListener) {
        if(jRequestArray!=null){
            Log.d(TAG, "Request Body:" + jRequestArray.toString());
        }
        callAPIStringResponse(jRequestArray.toString().getBytes(), url, callCompleteListener, Request.Method.PUT);
    }

    protected static void postJsonDataStringResponse(JSONObject jRequestobj, String url,
                                                      final OnAPICallCompleteListener callCompleteListener){
        if(jRequestobj!=null){
            Log.d(TAG, "Request Body:" + jRequestobj.toString());
        }
        callAPIStringResponse(jRequestobj.toString().getBytes(), url, callCompleteListener, Request.Method.POST);
    }

    protected static void putJsonDataStringResponse(JSONObject jRequestobj, String url,
                                                     final OnAPICallCompleteListener callCompleteListener) {
        if(jRequestobj!=null){
            Log.d(TAG, "Request Body:" + jRequestobj.toString());
        }
        callAPIStringResponse(jRequestobj.toString().getBytes(), url, callCompleteListener, Request.Method.PUT);
    }




    private static void callAPIStringResponse(byte[] data, String url,
                                                 final OnAPICallCompleteListener callCompleteListener, int httpMethod) {
        Log.d(TAG, "Calling URL:" + url);


        StringRequest request = new StringRequest(httpMethod, url,
                response -> {
                    callCompleteListener.apiCallSuccess(response);
                },
                error -> {
                    callCompleteListener.apiCallFailure();
                }
        ) {
            @Override
            public byte[] getBody() {
                return data;
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        request.setRetryPolicy(GetDefaultReTryPolicy());
        // Adding request to request queue
        addToRequestQueue(request, AppContext.context);
    }


    private static RetryPolicy GetDefaultReTryPolicy() {
        return new DefaultRetryPolicy(DEFAULT_MEDIUM_TIME_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
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

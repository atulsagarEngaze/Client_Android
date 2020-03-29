package com.redtop.engaze.webservice;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

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

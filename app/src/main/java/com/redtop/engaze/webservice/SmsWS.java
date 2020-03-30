package com.redtop.engaze.webservice;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.redtop.engaze.R;


public class SmsWS extends BaseWebService {


    private final static String TAG = SmsWS.class.getName();

    public static void callSMSGateway(final Context context, JSONObject smsGatewayObj){
        try{
            String JsonPostURL =  MAP_API_URL + Routes.SMS_GATEWAY;

            Log.d(TAG, "Calling URL:" + JsonPostURL);

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
                    JsonPostURL, smsGatewayObj, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                    String Status;
                    try {
                        Status = (String)response.getString("Status");
                        if (Status == "true")
                        {
                            Log.d(TAG, "SMS Gateway Call Success: " + response);
//							Toast.makeText(context,
//									context.getResources().getString(R.string.message_smsGateway_success),
//									Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Web Error: " + error.getMessage());
                    Toast.makeText(context,
                            context.getResources().getString(R.string.message_smsGateway_error),
                            Toast.LENGTH_LONG).show();
                }
            })
            {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            jsonObjReq.setRetryPolicy((RetryPolicy) new DefaultRetryPolicy(DEFAULT_SHORT_TIME_TIMEOUT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Adding request to request queue
            addToRequestQueue(jsonObjReq, context);
        }
        catch(Exception ex){
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            Toast.makeText(context,
                    context.getResources().getString(R.string.message_smsGateway_error),
                    Toast.LENGTH_LONG).show();
        }
    }
}

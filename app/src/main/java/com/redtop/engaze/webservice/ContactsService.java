package com.redtop.engaze.webservice;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.PreffManager;
import com.redtop.engaze.constant.DurationConstants;
import com.redtop.engaze.domain.ContactOrGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ContactsService extends BaseWebService {

    private final static String TAG = ContactsService.class.getName();
    public static void AssignUserIdToRegisteredUser(Context context, final HashMap<String, ContactOrGroup> contactsAndgroups,
                                                    final OnAPICallCompleteListner listnerOnSuccess,
                                                    final OnAPICallCompleteListner listnerOnFailure){
        try
        {
            JSONObject jsnobj = createContactsJSON(context,contactsAndgroups);
            String apiUrl = MAP_API_URL + Routes.GET_REGISTERED_CONTACTS;
            Log.d(TAG, "Calling URL:" + apiUrl);
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    apiUrl, jsnobj, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                    listnerOnSuccess.apiCallComplete(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    if(listnerOnFailure!=null){
                        listnerOnFailure.apiCallComplete(null);
                    }
                }
            })
            {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            jsonObjReq.setRetryPolicy((RetryPolicy) new DefaultRetryPolicy(DurationConstants.DEFAULT_LONG_TIME_TIMEOUT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Adding request to request queue
            addToRequestQueue(jsonObjReq, context);
        }
        catch(Exception ex){
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    private static JSONObject createContactsJSON(Context context, HashMap<String, ContactOrGroup> contactsAndgroups) throws JSONException, JSONException {
        // making json object request
        JSONObject jobj = new JSONObject();
        JSONArray jsonarr = new JSONArray();
        for(String mobileNumber : contactsAndgroups.keySet()){

            jsonarr.put(mobileNumber.replaceAll("\\s",""));
        }
        // Construct the selected Users json object
        jobj.put("RequestorId", AppContext.getInstance().loginId);
        jobj.put("ContactList", jsonarr);

        return jobj;
    }
}

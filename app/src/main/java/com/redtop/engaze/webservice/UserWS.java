package com.redtop.engaze.webservice;

import android.content.Context;
import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.domain.ContactOrGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class UserWS extends BaseWebService implements IUserWS {

    private final static String TAG = SmsWS.class.getName();

    public void saveProfile(JSONObject jRequestobj,
                            final OnAPICallCompleteListner listnerOnSuccess,
                            final OnAPICallCompleteListner listnerOnFailure) {
        try {
            postData(jRequestobj, ApiUrl.ACCOUNT_REGISTER, listnerOnSuccess, listnerOnFailure);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void sendInvite(JSONObject jsonObject, final OnAPICallCompleteListner listnerOnSuccess,
                           final OnAPICallCompleteListner listnerOnFailure) {
        try {

            String url = MAP_API_URL + "Routes.Contacts/InviteContact";

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void AssignUserIdToRegisteredUser(final HashMap<String, ContactOrGroup> contactsAndgroups,
                                             final OnAPICallCompleteListner listnerOnSuccess,
                                             final OnAPICallCompleteListner listnerOnFailure) {
        try {
            JSONObject jsonObject = createContactsJSON(contactsAndgroups);

            postData(jsonObject, ApiUrl.REGISTERED_CONTACTS, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    private JSONObject createContactsJSON(HashMap<String, ContactOrGroup> contactsAndgroups) throws JSONException, JSONException {
        // making json object request
        JSONObject jobj = new JSONObject();
        JSONArray jsonarr = new JSONArray();
        for (String mobileNumber : contactsAndgroups.keySet()) {
            jsonarr.put(mobileNumber.replaceAll("\\s", ""));
        }
        // Construct the selected Users json object
        jobj.put("RequestorId", AppContext.context.loginId);
        jobj.put("ContactList", jsonarr);
        jobj.put("RequestorCountryCode", "+91");

        return jobj;
    }

}

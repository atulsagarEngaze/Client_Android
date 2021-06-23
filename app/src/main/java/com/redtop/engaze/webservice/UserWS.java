package com.redtop.engaze.webservice;

import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.domain.ContactOrGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class UserWS extends BaseWebService implements IUserWS {

    private final static String TAG = SmsWS.class.getName();

    public void saveProfile(JSONObject jRequestobj,
                            final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            postDataStringResponse(jRequestobj, ApiUrl.ACCOUNT_REGISTER, onAPICallCompleteListener);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

    public void sendInvite(JSONObject jsonObject, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            String url = MAP_API_URL + "Routes.Contacts/InviteContact";

            postData(jsonObject, url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

    public void AssignUserIdToRegisteredUser(final HashMap<String, ContactOrGroup> contactsAndgroups,
                                             final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            JSONObject jsonObject = createContactsJSON(contactsAndgroups);

            postDataArrayResponse(jsonObject, ApiUrl.REGISTERED_CONTACTS, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
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

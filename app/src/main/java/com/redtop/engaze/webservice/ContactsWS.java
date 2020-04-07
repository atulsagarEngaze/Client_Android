package com.redtop.engaze.webservice;

import android.util.Log;
import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.domain.ContactOrGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ContactsWS extends BaseWebService implements IContactsWS {

    private final static String TAG = ContactsWS.class.getName();

    public void sendInvite(JSONObject jsonObject, final OnAPICallCompleteListner listnerOnSuccess,
                                  final OnAPICallCompleteListner listnerOnFailure){
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
            String url = MAP_API_URL + Routes.GET_REGISTERED_CONTACTS;

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

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

        return jobj;
    }
}

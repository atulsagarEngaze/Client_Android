package com.redtop.engaze.restApi.proxy;


import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.restApi.IUserWS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class UserWSProxy implements IUserWS {

    private final static String TAG = UserWSProxy.class.getName();

    public void sendInvite(JSONObject jsonObject, final OnAPICallCompleteListener onAPICallCompleteListener) {


        try {
            JSONArray jUsers = new JSONArray();

            JSONObject jUser1 = new JSONObject();
            jUser1.put("MobileNumberStoredInRequestorPhone", "9538700019");
            jUser1.put("UserId", "a5d5c037-b6a9-498f-b4e6-c7b166fd7daa");
            jUsers.put(jUser1);

            JSONObject jUser2 = new JSONObject();
            jUser2.put("MobileNumberStoredInRequestorPhone", "+919611633226");
            jUser2.put("UserId", "d6ce9df5-2e49-47e4-975b-9f1bc3549dbb");
            jUsers.put(jUser2);

            JSONObject jUser3 = new JSONObject();
            jUser3.put("MobileNumberStoredInRequestorPhone", "+919591417855");
            jUser3.put("UserId", "3105beb6-3347-4bdf-8905-29b622b51dbd");
            jUsers.put(jUser3);

            JSONObject jUser4 = new JSONObject();
            jUser4.put("MobileNumberStoredInRequestorPhone", "+919740319705");
            jUser4.put("UserId", "6754afc9-9832-4943-8cb3-34ebd9a5fbc6");
            jUsers.put(jUser4);

            JSONObject response = new JSONObject();
            response.put("ListOfRegisteredContacts", jUsers);

            onAPICallCompleteListener.apiCallSuccess(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void AssignUserIdToRegisteredUser(final HashMap<String, ContactOrGroup> contactsAndgroups,
                                             final OnAPICallCompleteListener onAPICallCompleteListener
    ) {
        try {
            JSONArray jUsers = new JSONArray();

            JSONObject jUser1 = new JSONObject();
            jUser1.put("MobileNumberStoredInRequestorPhone", "9538700019");
            jUser1.put("UserId", "a5d5c037-b6a9-498f-b4e6-c7b166fd7daa");
            jUsers.put(jUser1);

            JSONObject jUser2 = new JSONObject();
            jUser2.put("MobileNumberStoredInRequestorPhone", "+919611633226");
            jUser2.put("UserId", "d6ce9df5-2e49-47e4-975b-9f1bc3549dbb");
            jUsers.put(jUser2);

            JSONObject jUser3 = new JSONObject();
            jUser3.put("MobileNumberStoredInRequestorPhone", "+919591417855");
            jUser3.put("UserId", "3105beb6-3347-4bdf-8905-29b622b51dbd");
            jUsers.put(jUser3);

            JSONObject jUser4 = new JSONObject();
            jUser4.put("MobileNumberStoredInRequestorPhone", "+919740319705");
            jUser4.put("UserId", "6754afc9-9832-4943-8cb3-34ebd9a5fbc6");
            jUsers.put(jUser4);

            JSONObject response = new JSONObject();
            response.put("ListOfRegisteredContacts", jUsers);
            response.put("Status", "true");

            onAPICallCompleteListener.apiCallSuccess(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveProfile(JSONObject jRequestobj,
                            final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            onAPICallCompleteListener.apiCallSuccess(null);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

}

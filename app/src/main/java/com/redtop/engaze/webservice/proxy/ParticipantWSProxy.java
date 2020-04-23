package com.redtop.engaze.webservice.proxy;

import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.webservice.BaseWebService;
import com.redtop.engaze.webservice.IParticipantWS;

import org.json.JSONException;
import org.json.JSONObject;

public class ParticipantWSProxy extends BaseWebService implements IParticipantWS {

    private final static String TAG = ParticipantWSProxy.class.getName();
    private JSONObject fakeJsonResponse;

    public ParticipantWSProxy() {
        fakeJsonResponse = new JSONObject();
        try {
            fakeJsonResponse.put("status", "successful");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void pokeParticipants(JSONObject pokeAllContactsJSON,
                                 final OnAPICallCompleteListner listnerOnSuccess,
                                 final OnAPICallCompleteListner listnerOnFailure) {
        try {

            listnerOnSuccess.apiCallComplete(fakeJsonResponse);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }

    }

    public void addRemoveParticipants(JSONObject jsonObject, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            listnerOnSuccess.apiCallComplete(fakeJsonResponse);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }
}

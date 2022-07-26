package com.redtop.engaze.restApi.proxy;

import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.restApi.BaseWebApi;
import com.redtop.engaze.restApi.IParticipantApi;

import org.json.JSONException;
import org.json.JSONObject;

public class ParticipantApiProxy extends BaseWebApi implements IParticipantApi {

    private final static String TAG = ParticipantApiProxy.class.getName();
    private JSONObject fakeJsonResponse;

    public ParticipantApiProxy() {
        fakeJsonResponse = new JSONObject();
        try {
            fakeJsonResponse.put("status", "successful");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void pokeParticipants(JSONObject pokeAllContactsJSON,
                                 final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            onAPICallCompleteListener.apiCallSuccess(fakeJsonResponse);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }

    }

    public void addRemoveParticipants(JSONObject jsonObject, final String eventId, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            onAPICallCompleteListener.apiCallSuccess(fakeJsonResponse);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }
}

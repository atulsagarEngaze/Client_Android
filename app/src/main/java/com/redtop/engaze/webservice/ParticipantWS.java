package com.redtop.engaze.webservice;

import android.util.Log;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;

import org.json.JSONObject;

public class ParticipantWS extends BaseWebService implements IParticipantWS {

    private final static String TAG = ParticipantWS.class.getName();

    public void pokeParticipants(JSONObject pokeAllContactsJSON,
                                        final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            String JsonPostURL = MAP_API_URL + ApiUrl.POKEALL_CONTACTS;

            postData(pokeAllContactsJSON, JsonPostURL, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }

    }

    public void addRemoveParticipants(JSONObject jsonObject, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            String url = MAP_API_URL + ApiUrl.UPDATE_PARTICIPANTS;

            postData(jsonObject, url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }
}

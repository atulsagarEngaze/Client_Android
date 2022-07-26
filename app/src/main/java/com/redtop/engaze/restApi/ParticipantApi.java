package com.redtop.engaze.restApi;

import android.util.Log;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class ParticipantApi extends BaseWebApi {

    private final static String TAG = ParticipantApi.class.getName();

    public void pokeParticipants(JSONObject pokeAllContactsJSON,
                                        final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            String JsonPostURL = ApiUrl.POKEALL_CONTACTS;

            postData(pokeAllContactsJSON, JsonPostURL, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }

    }

    public void addRemoveParticipants(JSONArray participants, final String eventId, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            String url = ApiUrl.UPDATE_PARTICIPANTS.replace("{eventId}", eventId);

            putArrayDataStringResponse(participants, url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }
}

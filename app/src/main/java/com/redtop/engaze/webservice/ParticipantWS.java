package com.redtop.engaze.webservice;

import android.content.Context;
import android.util.Log;
import com.redtop.engaze.Interface.OnAPICallCompleteListner;

import org.json.JSONObject;

public class ParticipantWS extends BaseWebService {

    private final static String TAG = ParticipantWS.class.getName();

    public static void pokeParticipants(JSONObject pokeAllContactsJSON,
                                        final OnAPICallCompleteListner listnerOnSuccess,
                                        final OnAPICallCompleteListner listnerOnFailure) {
        try {

            String JsonPostURL = MAP_API_URL + Routes.POKEALL_CONTACTS;

            postData(pokeAllContactsJSON, JsonPostURL, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }

    }

    public static void addRemoveParticipants(JSONObject jsonObject, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            String url = MAP_API_URL + Routes.UPDATE_PARTICIPANTS;

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }
}

package com.redtop.engaze.restApi;


import com.redtop.engaze.Interface.OnAPICallCompleteListener;

import org.json.JSONObject;

public interface IParticipantApi {

    public void pokeParticipants(JSONObject pokeAllContactsJSON,
                                 final OnAPICallCompleteListener onAPICallCompleteListener);

    public void addRemoveParticipants(JSONObject jsonObject, final String eventId ,final OnAPICallCompleteListener onAPICallCompleteListener);

}

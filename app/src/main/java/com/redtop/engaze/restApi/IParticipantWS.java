package com.redtop.engaze.webservice;


import com.redtop.engaze.Interface.OnAPICallCompleteListener;

import org.json.JSONObject;

public interface IParticipantWS {

    public void pokeParticipants(JSONObject pokeAllContactsJSON,
                                 final OnAPICallCompleteListener onAPICallCompleteListener);

    public void addRemoveParticipants(JSONObject jsonObject, final String eventId ,final OnAPICallCompleteListener onAPICallCompleteListener);

}

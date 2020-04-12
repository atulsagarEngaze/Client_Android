package com.redtop.engaze.webservice;


import com.redtop.engaze.Interface.OnAPICallCompleteListner;

import org.json.JSONObject;

public interface IParticipantWS {

    public void pokeParticipants(JSONObject pokeAllContactsJSON,
                                 final OnAPICallCompleteListner listnerOnSuccess,
                                 final OnAPICallCompleteListner listnerOnFailure);

    public void addRemoveParticipants(JSONObject jsonObject, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure);

}

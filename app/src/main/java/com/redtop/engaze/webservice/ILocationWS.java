package com.redtop.engaze.webservice;


import com.redtop.engaze.Interface.OnAPICallCompleteListner;

import org.json.JSONObject;

public interface ILocationWS {

    void updateLocation(JSONObject jsonObject,
                        final OnAPICallCompleteListner listnerOnSuccess,
                        final OnAPICallCompleteListner listnerOnFailure);


    void getLocationsFromServer(String userId, String eventId,
                                final OnAPICallCompleteListner listnerOnSuccess,
                                final OnAPICallCompleteListner listnerOnFailure);

}

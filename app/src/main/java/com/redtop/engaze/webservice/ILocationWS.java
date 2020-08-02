package com.redtop.engaze.webservice;


import com.redtop.engaze.Interface.OnAPICallCompleteListener;

import org.json.JSONObject;

public interface ILocationWS {

    void updateLocation(JSONObject jsonObject,
                        final OnAPICallCompleteListener onAPICallCompleteListener);


    void getLocationsFromServer(String userId, String eventId,
                                final OnAPICallCompleteListener onAPICallCompleteListener);

}

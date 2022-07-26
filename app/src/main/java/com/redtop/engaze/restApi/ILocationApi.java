package com.redtop.engaze.restApi;


import com.redtop.engaze.Interface.OnAPICallCompleteListener;

import org.json.JSONObject;

public interface ILocationApi {

    void updateLocation(JSONObject jsonObject,
                        final OnAPICallCompleteListener onAPICallCompleteListener);


    void getLocationsFromServer(String userId, String eventId,
                                final OnAPICallCompleteListener onAPICallCompleteListener);

}

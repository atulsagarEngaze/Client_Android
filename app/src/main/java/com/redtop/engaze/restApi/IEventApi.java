package com.redtop.engaze.restApi;


import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.domain.EventPlace;

import org.json.JSONObject;

public interface IEventApi {

    public void SaveEvent(JSONObject jsonObject, final OnAPICallCompleteListener onAPICallCompleteListener);


    public void saveUserResponse(final AcceptanceStatus acceptanceStatus, final String eventId, final OnAPICallCompleteListener onAPICallCompleteListener);


    public void endEvent(final String eventID, final OnAPICallCompleteListener onAPICallCompleteListener);


    public void leaveEvent(final String eventID, final OnAPICallCompleteListener onAPICallCompleteListener);


    public void RefreshEventListFromServer(final OnAPICallCompleteListener onAPICallCompleteListener);


    public void extendEventEndTime(final String newEndTime, final String eventID, final OnAPICallCompleteListener onAPICallCompleteListener);

    public void changeDestination(final EventPlace destinationPlace, final String eventId, final OnAPICallCompleteListener onAPICallCompleteListener);


    public void getEventDetail(String eventid, final OnAPICallCompleteListener onAPICallCompleteListener);


}

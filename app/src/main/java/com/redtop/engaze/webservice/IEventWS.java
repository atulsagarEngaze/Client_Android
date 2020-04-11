package com.redtop.engaze.webservice;


import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.domain.EventPlace;

import org.json.JSONObject;

public interface IEventWS {

    public void CreateEvent(JSONObject jsonObject, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure);


    public void saveUserResponse(final AcceptanceStatus acceptanceStatus, final String eventid, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure);


    public void endEvent(final String eventID, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure);


    public void leaveEvent(final String eventID, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure);


    public void RefreshEventListFromServer(final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure);


    public void extendEventEndTime(final int duration, final String eventID, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure);

    public void changeDestination(final EventPlace destinationPlace, final String eventId, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure);


    public void getEventDetail(String eventid, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure);


}

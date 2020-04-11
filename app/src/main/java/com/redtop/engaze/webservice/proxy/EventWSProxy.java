package com.redtop.engaze.webservice.proxy;


import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.webservice.IEventWS;

import org.json.JSONObject;

public class EventWSProxy implements IEventWS {

    public void CreateEvent(JSONObject jsonObject, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            jsonObject.put("EventId", AppUtility.getRandamNumber());

            listnerOnSuccess.apiCallComplete(jsonObject);

        } catch (Exception ex) {
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void saveUserResponse(final AcceptanceStatus acceptanceStatus, final String eventid, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {
            listnerOnSuccess.apiCallComplete(null);

        } catch (Exception ex) {

            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void endEvent(final String eventID, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            listnerOnSuccess.apiCallComplete(null);

        } catch (Exception ex) {

            listnerOnFailure.apiCallComplete(null);

        }
    }

    public void leaveEvent(final String eventID, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            listnerOnSuccess.apiCallComplete(null);

        } catch (Exception ex) {

            listnerOnFailure.apiCallComplete(null);

        }
    }

    public void RefreshEventListFromServer(final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            listnerOnSuccess.apiCallComplete(null);

        } catch (Exception ex) {

            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void extendEventEndTime(final int duration, final String eventID, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {
            listnerOnSuccess.apiCallComplete(null);
        } catch (Exception ex) {

            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void changeDestination(final EventPlace destinationPlace, final String eventId, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {
            listnerOnSuccess.apiCallComplete(null);

        } catch (Exception ex) {

            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void getEventDetail(String eventid, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {

        try {

            listnerOnSuccess.apiCallComplete(null);

        } catch (Exception ex) {

            listnerOnFailure.apiCallComplete(null);
        }
    }

}

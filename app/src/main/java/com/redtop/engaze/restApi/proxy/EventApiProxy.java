/*package com.redtop.engaze.webservice.proxy;



import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.webservice.IEventApi;

import org.json.JSONException;
import org.json.JSONObject;

public class EventWSProxy implements IEventApi {
    private JSONObject fakeJsonResponse;


    public EventWSProxy() {
        fakeJsonResponse = new JSONObject();
        try {
            fakeJsonResponse.put("status", "successful");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void CreateEvent(JSONObject jsonObject, final OnAPICallCompleteListener listnerOnSuccess, final OnAPICallCompleteListener listnerOnFailure) {
        try {

            jsonObject.put("EventId", AppUtility.getRandamNumber());

            listnerOnSuccess.apiCallSuccess(jsonObject);

        } catch (Exception ex) {
            listnerOnFailure.apiCallSuccess(null);
        }
    }

    public void saveUserResponse(final AcceptanceStatus acceptanceStatus, final String eventid, final OnAPICallCompleteListener listnerOnSuccess, final OnAPICallCompleteListener listnerOnFailure) {
        try {
            listnerOnSuccess.apiCallSuccess(fakeJsonResponse);

        } catch (Exception ex) {

            listnerOnFailure.apiCallSuccess(null);
        }
    }

    public void endEvent(final String eventID, final OnAPICallCompleteListener listnerOnSuccess, final OnAPICallCompleteListener listnerOnFailure) {
        try {

            listnerOnSuccess.apiCallSuccess(fakeJsonResponse);

        } catch (Exception ex) {

            listnerOnFailure.apiCallSuccess(null);

        }
    }

    public void leaveEvent(final String eventID, final OnAPICallCompleteListener listnerOnSuccess, final OnAPICallCompleteListener listnerOnFailure) {
        try {

            listnerOnSuccess.apiCallSuccess(fakeJsonResponse);

        } catch (Exception ex) {

            listnerOnFailure.apiCallSuccess(null);

        }
    }

    public void RefreshEventListFromServer(final OnAPICallCompleteListener listnerOnSuccess, final OnAPICallCompleteListener listnerOnFailure) {
        try {

            listnerOnSuccess.apiCallSuccess(fakeJsonResponse);

        } catch (Exception ex) {

            listnerOnFailure.apiCallSuccess(null);
        }
    }

    public void extendEventEndTime(final int duration, final String eventID, final OnAPICallCompleteListener listnerOnSuccess, final OnAPICallCompleteListener listnerOnFailure) {
        try {
            listnerOnSuccess.apiCallSuccess(fakeJsonResponse);
        } catch (Exception ex) {

            listnerOnFailure.apiCallSuccess(null);
        }
    }

    public void changeDestination(final EventPlace destinationPlace, final String eventId, final OnAPICallCompleteListener listnerOnSuccess, final OnAPICallCompleteListener listnerOnFailure) {
        try {
            listnerOnSuccess.apiCallSuccess(fakeJsonResponse);

        } catch (Exception ex) {

            listnerOnFailure.apiCallSuccess(null);
        }
    }

    public void getEventDetail(String eventid, final OnAPICallCompleteListener listnerOnSuccess, final OnAPICallCompleteListener listnerOnFailure) {

        try {

            listnerOnSuccess.apiCallSuccess(fakeJsonResponse);

        } catch (Exception ex) {

            listnerOnFailure.apiCallSuccess(null);
        }
    }

}
*/
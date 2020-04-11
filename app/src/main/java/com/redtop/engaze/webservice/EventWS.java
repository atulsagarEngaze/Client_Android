package com.redtop.engaze.webservice;

import android.content.Context;
import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.domain.EventPlace;

import org.json.JSONObject;

public class EventWS extends BaseWebService implements IEventWS {

    private final static String TAG = EventWS.class.getName();

    public void CreateEvent(JSONObject jsonObject, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String url = "";
            if (jsonObject.has("EventId")) {
                url = MAP_API_URL + Routes.UPDATE_EVENT;
            } else {
                url = MAP_API_URL + Routes.CREATE_EVENT;
            }

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);

        }
    }

    public void saveUserResponse(final AcceptanceStatus acceptanceStatus, final String eventid, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String url = MAP_API_URL + Routes.RESPOND_INVITE;
            // making json object request
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("EventId", eventid);
            jsonObject.put("RequestorId", AppContext.context.loginId);
            jsonObject.put("EventAcceptanceStateId", acceptanceStatus.getStatus());
            jsonObject.put("TrackingAccepted", "true");

            postData( jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void endEvent(final String eventID, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            String url = MAP_API_URL + Routes.END_EVENT;
            // making json object request
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("RequestorId", AppContext.context.loginId);
            jsonObject.put("EventId", eventID);
            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);

        }
    }

    public void leaveEvent(final String eventID, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            String url = MAP_API_URL + Routes.LEAVE_EVENT;
            // making json object request
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("RequestorId", AppContext.context.loginId);
            jsonObject.put("EventId", eventID);

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);

        }
    }

    public void RefreshEventListFromServer(final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            String url = MAP_API_URL + Routes.EVENT_DETAIL;

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("RequestorId", AppContext.context.loginId);

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void extendEventEndTime(final int duration, final String eventID, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String url = MAP_API_URL + Routes.EXTEND_EVENT;
            // making json object request
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("RequestorId", AppContext.context.loginId);
            jsonObject.put("EventId", eventID);
            jsonObject.put("ExtendEventDuration", duration);

            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void changeDestination(final EventPlace destinationPlace, final String eventId, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String url = MAP_API_URL + Routes.UPDATE_DESTINATION;

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("RequestorId", AppContext.context.loginId);

            if (destinationPlace != null) {
                jsonObject.put("DestinationLatitude", destinationPlace.getLatLang().latitude);
                jsonObject.put("DestinationLongitude", destinationPlace.getLatLang().longitude);
                jsonObject.put("DestinationAddress", destinationPlace.getAddress());
                jsonObject.put("DestinationName", destinationPlace.getName());
                //jobj.put( "DestinationName", mEventLocationTextView.getText());
            } else {
                jsonObject.put("DestinationLatitude", "");
                jsonObject.put("DestinationLongitude", "");
                jsonObject.put("DestinationAddress", "");
                jsonObject.put("DestinationName", "");
            }

            jsonObject.put("EventId", eventId);
            postData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void getEventDetail(String eventid, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {

        try {

            String url = MAP_API_URL + Routes.EVENT_DETAIL;
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("EventId", eventid);
            jsonObject.put("RequestorId", AppContext.context.loginId);

            getData(jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

}

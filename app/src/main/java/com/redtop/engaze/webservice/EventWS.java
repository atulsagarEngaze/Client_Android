package com.redtop.engaze.webservice;

import android.content.Context;
import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.domain.EventPlace;

import org.json.JSONObject;

public class EventWS extends BaseWebService {

    private final static String TAG = EventWS.class.getName();

    public static void CreateEvent(Context context, JSONObject jsonObject, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String url = "";
            if (jsonObject.has("EventId")) {
                url = MAP_API_URL + Routes.UPDATE_EVENT;
            } else {
                url = MAP_API_URL + Routes.CREATE_EVENT;
            }

            postData(context, jsonObject, url, listnerOnSuccess, listnerOnFailure);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);

        }
    }

    public static void saveUserResponse(final AcceptanceStatus acceptanceStatus, final Context context, final String eventid, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String url = MAP_API_URL + Routes.RESPOND_INVITE;
            // making json object request
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("EventId", eventid);
            jsonObject.put("RequestorId", AppContext.getInstance().loginId);
            jsonObject.put("EventAcceptanceStateId", acceptanceStatus.getStatus());
            jsonObject.put("TrackingAccepted", "true");

            postData(context, jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public static void endEvent(final Context context, final String eventID, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            String url = MAP_API_URL + Routes.END_EVENT;
            // making json object request
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("RequestorId", AppContext.getInstance().loginId);
            jsonObject.put("EventId", eventID);
            postData(context, jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);

        }
    }

    public static void leaveEvent(final Context context, final String eventID, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            String url = MAP_API_URL + Routes.LEAVE_EVENT;
            // making json object request
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("RequestorId", AppContext.getInstance().loginId);
            jsonObject.put("EventId", eventID);

            postData(context, jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);

        }
    }

    public static void RefreshEventListFromServer(final Context context, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {

            String url = MAP_API_URL + Routes.EVENT_DETAIL;

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("RequestorId", AppContext.getInstance().loginId);

            postData(context, jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public static void extendEventEndTime(final int i, final Context context, final String eventID, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String url = MAP_API_URL + Routes.EXTEND_EVENT;
            // making json object request
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("RequestorId", AppContext.getInstance().loginId);
            jsonObject.put("EventId", eventID);
            jsonObject.put("ExtendEventDuration", i);

            postData(context, jsonObject, url, listnerOnSuccess, listnerOnFailure);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public static void changeDestination(final EventPlace destinationPlace, final Context context, final String eventId, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {
        try {
            String url = MAP_API_URL + Routes.UPDATE_DESTINATION;

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("RequestorId", AppContext.getInstance().loginId);

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
            postData(context, jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public static void getEventDetail(Context context, String eventid, final OnAPICallCompleteListner listnerOnSuccess, final OnAPICallCompleteListner listnerOnFailure) {

        try {

            String url = MAP_API_URL + Routes.EVENT_DETAIL;
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("EventId", eventid);
            jsonObject.put("RequestorId", AppContext.getInstance().loginId);

            getData(context, jsonObject, url, listnerOnSuccess, listnerOnFailure);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

}

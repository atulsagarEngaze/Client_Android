package com.redtop.engaze.webservice;

import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.domain.EventPlace;

import org.json.JSONObject;

public class EventWS extends BaseWebService implements IEventWS {

    private final static String TAG = EventWS.class.getName();

    public void CreateEvent(JSONObject jsonObject, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            String url = "";
            if (jsonObject.has("EventId")) {
                url = ApiUrl.UPDATE_EVENT;
            } else {
                url = ApiUrl.CREATE_EVENT;
            }

            postData(jsonObject, url, onAPICallCompleteListener);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();

        }
    }

    public void saveUserResponse(final AcceptanceStatus acceptanceStatus, final String eventid, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            String url = ApiUrl.RESPOND_INVITE;
            // making json object request
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("EventId", eventid);
            jsonObject.put("RequestorId", AppContext.context.loginId);
            jsonObject.put("EventAcceptanceStateId", acceptanceStatus.getStatus());
            jsonObject.put("TrackingAccepted", "true");

            postData(jsonObject, url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

    public void endEvent(final String eventID, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {


            String url = ApiUrl.END_EVENT.replace("{eventId}", eventID);
            // making json object request
            JSONObject jsonObject = new JSONObject();
            putData(jsonObject, url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();

        }
    }

    public void leaveEvent(final String eventID, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            String url = ApiUrl.LEAVE_EVENT;
            // making json object request
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("RequestorId", AppContext.context.loginId);
            jsonObject.put("EventId", eventID);

            postData(jsonObject, url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();

        }
    }

    public void RefreshEventListFromServer(final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            String url = ApiUrl.EVENT_DETAIL.replace("{userId}", AppContext.context.loginId);
            getData(url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

    public void extendEventEndTime(final int duration, final String eventID, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            String url = ApiUrl.EXTEND_EVENT;
            // making json object request
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("RequestorId", AppContext.context.loginId);
            jsonObject.put("EventId", eventID);
            jsonObject.put("ExtendEventDuration", duration);

            postData(jsonObject, url, onAPICallCompleteListener);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

    public void changeDestination(final EventPlace destinationPlace, final String eventId, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            String url = ApiUrl.UPDATE_DESTINATION;

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
            postData(jsonObject, url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

    public void getEventDetail(String eventid, final OnAPICallCompleteListener onAPICallCompleteListener) {

        try {

            String url = ApiUrl.EVENT_DETAIL.replace("{userId}", AppContext.context.loginId)
                    .replace("{eventId}", eventid);
            getData(url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

}

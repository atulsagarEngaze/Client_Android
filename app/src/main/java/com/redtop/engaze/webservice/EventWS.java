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
                url = MAP_API_URL + ApiUrl.UPDATE_EVENT;
            } else {
                url = MAP_API_URL + ApiUrl.CREATE_EVENT;
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
            String url = MAP_API_URL + ApiUrl.RESPOND_INVITE;
            // making json object request
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("EventId", eventid);
            jsonObject.put("RequestorId", AppContext.context.loginId);
            jsonObject.put("EventAcceptanceStateId", acceptanceStatus.getStatus());
            jsonObject.put("TrackingAccepted", "true");

            postData( jsonObject, url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

    public void endEvent(final String eventID, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            String url = MAP_API_URL + ApiUrl.END_EVENT;
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

    public void leaveEvent(final String eventID, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            String url = MAP_API_URL + ApiUrl.LEAVE_EVENT;
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

            String url = MAP_API_URL + (ApiUrl.EVENT_DETAIL).replace("{userId}", "94973d2a-614e-4b2c-8654-7e6b13cdc44e");//AppContext.context.loginId);
            getData(url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

    public void extendEventEndTime(final int duration, final String eventID, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            String url = MAP_API_URL + ApiUrl.EXTEND_EVENT;
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
            String url = MAP_API_URL + ApiUrl.UPDATE_DESTINATION;

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

            String url = MAP_API_URL + (ApiUrl.EVENT_DETAIL).replace("{userId}", "6172ab1a-2e58-4dab-9431-9a06dd88905c")
                    .replace("{eventId}", eventid);//AppContext.context.loginId);
            getData( url, onAPICallCompleteListener);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

}

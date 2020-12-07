package com.redtop.engaze.webservice;

import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.domain.EventPlace;

import org.json.JSONObject;

public class EventWS extends BaseWebService implements IEventWS {

    private final static String TAG = EventWS.class.getName();

    public void SaveEvent(JSONObject jsonObject, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            if (jsonObject.has("EventId")) {
                putData(jsonObject, ApiUrl.SAVE_EVENT, onAPICallCompleteListener);
            } else {
               postData(jsonObject, ApiUrl.SAVE_EVENT, onAPICallCompleteListener);
            }

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

    public void saveUserResponse(final AcceptanceStatus acceptanceStatus, final String eventId, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {

            String url = ApiUrl.RESPOND_INVITE.replace("{eventId}", eventId)
                    .replace("{participantId}", AppContext.context.loginId)
                    .replace("{status}", Integer.toString( acceptanceStatus.getStatus()));
            // making json object request
            JSONObject jsonObject = new JSONObject();
            putData(jsonObject, url, onAPICallCompleteListener);

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

    public void extendEventEndTime(final String  newEndTime, final String eventID, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            String url = ApiUrl.EXTEND_EVENT.replace("{eventId}", eventID).replace("{endTime}", newEndTime);
            // making json object request
            JSONObject jsonObject = new JSONObject();
            putData(jsonObject, url, onAPICallCompleteListener);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            onAPICallCompleteListener.apiCallFailure();
        }
    }

    public void changeDestination(final EventPlace destinationPlace, final String eventId, final OnAPICallCompleteListener onAPICallCompleteListener) {
        try {
            String url = ApiUrl.UPDATE_DESTINATION.replace("{eventId}", eventId);

            JSONObject jsonObject = new JSONObject();
            if (destinationPlace != null) {
                jsonObject.put("Latitude", destinationPlace.getLatLang().latitude);
                jsonObject.put("Longitude", destinationPlace.getLatLang().longitude);
                jsonObject.put("Address", destinationPlace.getAddress());
                jsonObject.put("Name", destinationPlace.getName());
                //jobj.put( "DestinationName", mEventLocationTextView.getText());
            } else {
                jsonObject.put("Latitude", "");
                jsonObject.put("Longitude", "");
                jsonObject.put("Address", "");
                jsonObject.put("Name", "");
            }
            putData(jsonObject, url, onAPICallCompleteListener);

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

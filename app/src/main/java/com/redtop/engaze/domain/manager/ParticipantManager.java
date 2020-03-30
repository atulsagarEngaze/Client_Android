package com.redtop.engaze.domain.manager;

import android.content.Context;
import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.R;
import com.redtop.engaze.common.AppService;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.domain.EventDetail;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.webservice.EventWS;
import com.redtop.engaze.webservice.ParticipantWS;

import org.json.JSONObject;

import java.util.List;

public class ParticipantManager {
    private final static String TAG = ParticipantManager.class.getName();

    public static void pokeParticipants(final Context context, JSONObject pokeParticipantsJSON,
                                        final OnActionCompleteListner onActionCompleteListner,
                                        final OnActionFailedListner onActionFailedListner) {
        String message ="";
        if(!AppService.isNetworkAvailable(context))
        {
            message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            onActionFailedListner.actionFailed(message, Action.POKEALL);
            return ;
        }

        ParticipantWS.pokeParticipants(context, pokeParticipantsJSON, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                Log.d(TAG, "PokeAllResponse:" + response.toString());

                try {
                    String Status = (String)response.getString("Status");

                    if (Status == "true")
                    {
                        onActionCompleteListner.actionComplete(Action.POKEALL);
                    }
                    else{
                        onActionFailedListner.actionFailed(null, Action.POKEALL);
                    }

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    onActionFailedListner.actionFailed(null, Action.POKEALL);
                }

            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                if(response!=null){
                    Log.d(TAG, "EventResponse:" + response.toString());
                }
                onActionFailedListner.actionFailed(null, Action.POKEALL);
            }
        });
    }

    public static void addRemoveParticipants(JSONObject addRemoveContactsJSON, final Context context, final OnActionCompleteListner listenerOnSuccess, final OnActionFailedListner listenerOnFailure) {
        String message ="";
        if(!AppService.isNetworkAvailable(context))
        {
            message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listenerOnFailure.actionFailed(message, Action.ADDREMOVEPARTICIPANTS);
            return ;
        }

        ParticipantWS.addRemoveParticipants(addRemoveContactsJSON, context, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());
                try{

                    String Status = (String)response.getString("Status");
                    if (Status == "true")
                    {
                        List<EventDetail> eventDetailList =  EventService.parseEventDetailList(response.getJSONArray("ListOfEvents"), context);
                        EventDetail event = eventDetailList.get(0);
                        InternalCaching.saveEventToCache(event, context);
                        listenerOnSuccess.actionComplete(Action.ADDREMOVEPARTICIPANTS);
                    }
                    else
                    {
                        listenerOnFailure.actionFailed(null, Action.ADDREMOVEPARTICIPANTS);
                    }
                }
                catch(Exception ex){
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listenerOnFailure.actionFailed(null, Action.ADDREMOVEPARTICIPANTS);
                }

            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                listenerOnFailure.actionFailed(null, Action.ADDREMOVEPARTICIPANTS);
            }
        });

    }
}

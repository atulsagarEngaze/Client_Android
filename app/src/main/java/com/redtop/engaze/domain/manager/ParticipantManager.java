package com.redtop.engaze.domain.manager;

import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.utility.BitMapHelper;
import com.redtop.engaze.common.utility.MaterialColor;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.service.ParticipantService;
import com.redtop.engaze.webservice.IParticipantWS;
import com.redtop.engaze.webservice.proxy.ParticipantWSProxy;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ParticipantManager {
    private final static String TAG = ParticipantManager.class.getName();

    private final static IParticipantWS participantWS = new ParticipantWSProxy();

    public static void pokeParticipants(JSONObject pokeParticipantsJSON,
                                        final OnActionCompleteListner onActionCompleteListner,
                                        final OnActionFailedListner onActionFailedListner) {
        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            onActionFailedListner.actionFailed(message, Action.POKEALL);
            return;
        }

        participantWS.pokeParticipants(pokeParticipantsJSON, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                Log.d(TAG, "PokeAllResponse:" + response.toString());

                try {

                    onActionCompleteListner.actionComplete(Action.POKEALL);

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    onActionFailedListner.actionFailed(null, Action.POKEALL);
                }

            }

            @Override
            public void apiCallFailure() {
                onActionFailedListner.actionFailed(null, Action.POKEALL);
            }
        });
    }

    public static void addRemoveParticipants(JSONObject addRemoveContactsJSON, final OnActionCompleteListner listenerOnSuccess, final OnActionFailedListner listenerOnFailure) {
        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listenerOnFailure.actionFailed(message, Action.ADDREMOVEPARTICIPANTS);
            return;
        }

        participantWS.addRemoveParticipants(addRemoveContactsJSON, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());
                try {


                  /*  List<Event> eventList = EventParser.parseEventDetailList(response.getJSONArray("ListOfEvents"));
                    Event event = eventList.get(0);
                    InternalCaching.saveEventToCache(event);*/
                    listenerOnSuccess.actionComplete(Action.ADDREMOVEPARTICIPANTS);

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listenerOnFailure.actionFailed(null, Action.ADDREMOVEPARTICIPANTS);
                }

            }

            @Override
            public void apiCallFailure() {
                listenerOnFailure.actionFailed(null, Action.ADDREMOVEPARTICIPANTS);

            }
        });

    }

    public static void setContactsGroup(ArrayList<EventParticipant> eventMembers) {
        HashMap<String, ContactOrGroup> registeredList = InternalCaching.getRegisteredContactListFromCache();

        for (EventParticipant mem : eventMembers) {
            if (mem.contactOrGroup == null) {
                mem.contactOrGroup = registeredList.get(mem.userId);
            }
            if (mem.contactOrGroup == null) {
                ContactOrGroup cg = new ContactOrGroup();
                cg.setIconImageBitmap(ContactOrGroup.getAppUserIconBitmap());
                if (ParticipantService.isParticipantCurrentUser(mem.userId) || mem.profileName.startsWith("~")) {
                    cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(MaterialColor.getColor(mem.profileName), 40, mem.profileName.substring(1, 2).toUpperCase()));
                } else {
                    cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(MaterialColor.getColor(mem.profileName), 40, mem.profileName.substring(0, 1).toUpperCase()));
                }

                mem.contactOrGroup = cg;
            }
        }
    }

}

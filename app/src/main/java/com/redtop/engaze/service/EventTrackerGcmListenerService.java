package com.redtop.engaze.service;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.enums.EventState;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.domain.service.EventParser;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.domain.service.ParticipantService;
import com.redtop.engaze.manager.EventNotificationManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

public class EventTrackerGcmListenerService extends FirebaseMessagingService implements OnActionFailedListner {

    private static final String TAG = "MyGcmListenerService";
    private Context mContext;
    private String mEventId;

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage fcmMessage) {
        try {
            mContext = this;
            String from = fcmMessage.getFrom();
            Map fcmMap = fcmMessage.getData();
            String messageType = fcmMap.get("Type").toString();
            String message = fcmMap.get("Data").toString();
            JSONObject data = new JSONObject(message);
            Log.d(TAG, "From: " + from);
            Log.d(TAG, "MessageType: " + messageType);
            Log.d(TAG, "Data: " + message);

            mEventId = data.getString("EventId").toString();

            if (mEventId != null) {
                if (messageType.equals("EventEnd") || messageType.equals("EventDelete") || messageType.equals("RemovedFromEvent")) {

                    actionsBasedOnGCMMessageTypes(messageType, data);
                } else {
                    EventManager.refreshEventList(eventDetailList -> {
                        actionsBasedOnGCMMessageTypes(messageType, data);

                    }, this);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void actionsBasedOnGCMMessageTypes(String messageType, final JSONObject data) {
        try {
            Event event;
            Intent intent = null;
            switch (messageType) {
                case "EventUpdate":
                    intent = new Intent(Veranstaltung.EVENT_UPDATED_BY_INITIATOR);
                    intent.putExtra("eventId", mEventId);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    break;

                case "EventResponse": {
                    final String userId = data.get("EventResponderId").toString();
                    final String userName = data.get("EventResponderName").toString();
                    final int eventAcceptanceStateId = Integer.parseInt(data.get("EventAcceptanceStateId").toString());
                    EventManager.updateEventWithParticipantResponse(mEventId, userId, userName, eventAcceptanceStateId, action -> {
                        Intent intent18 = new Intent(Veranstaltung.EVENT_USER_RESPONSE);
                        intent18.putExtra("eventId", mEventId);
                        intent18.putExtra("userId", userId);
                        intent18.putExtra("eventAcceptanceStateId", eventAcceptanceStateId);
                        intent18.putExtra("EventResponderName", userName);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent18);

                    }, this);

                    break;
                }

                case "EventLeave":
                    final String userId = data.get("EventResponderId").toString();
                    final String userName = data.get("EventResponderName").toString();
                    EventManager.updateEventWithParticipantLeft(mContext, mEventId, userId, userName, action -> {
                        Intent intent17 = new Intent(Veranstaltung.PARTICIPANT_LEFT_EVENT);
                        intent17.putExtra("eventId", mEventId);
                        intent17.putExtra("userId", userId);
                        intent17.putExtra("EventResponderName", userName);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent17);

                    }, this);

                    break;

                case "EventInvite":
                    event = InternalCaching.getEventFromCache(mEventId);
                    if (EventService.isEventShareMyLocationEventForCurrentUser(event)) {
                        event.state = EventState.TRACKING_ON;
                    }

                    Intent eventReceived = new Intent(Veranstaltung.EVENT_RECEIVED);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(eventReceived);
                    EventService.setEndEventAlarm(event);
                    EventNotificationManager.showEventInviteNotification(event);

                    break;

                case "RegisteredUserUpdate":
                    //code against that
                    break;
                case "EventEnd":
                    EventManager.eventEndedByInitiator(mEventId, action -> {
                        Intent intent12 = new Intent(Veranstaltung.EVENT_ENDED_BY_INITIATOR);
                        intent12.putExtra("eventId", mEventId);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent12);
                    }, this);
                    break;

                case "EventExtend":
                    String extendDuration = data.get("ExtendEventDuration").toString();
                    EventManager.eventExtendedByInitiator(mEventId, action -> {
                        //LocalBroadCast
                        Intent intent1 = new Intent(Veranstaltung.EVENT_EXTENDED_BY_INITIATOR);
                        intent1.putExtra("eventId", mEventId);
                        intent1.putExtra("com.redtop.engaze.service.ExtendEventDuration", extendDuration);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent1);

                    }, this);

                    break;

                case "EventDelete":

                    EventManager.eventDeletedByInitiator(mEventId, action -> {
                        //LocalBroadCast
                        Intent intent13 = new Intent(Veranstaltung.EVENT_DELETE_BY_INITIATOR);
                        intent13.putExtra("eventId", mEventId);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent13);

                    }, this);

                    break;

                case "EventUpdateLocation":
                    String destination = data.get("DestinationName").toString();
                    EventManager.eventDestinationChangedByInitiator(mEventId, action -> {
                        //LocalBroadCast
                        Intent intent14 = new Intent(Veranstaltung.EVENT_DESTINATION_UPDATED_BY_INITIATOR);
                        intent14.putExtra("com.redtop.engaze.service.UpdatedDestination", destination);
                        intent14.putExtra("eventId", mEventId);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent14);
                    }, this);

                    break;
                case "RemovedFromEvent":
                    EventManager.currentparticipantRemovedByInitiator(mContext, mEventId, action -> {
                        //LocalBroadCast
                        Intent intent15 = new Intent(Veranstaltung.REMOVED_FROM_EVENT_BY_INITIATOR);
                        intent15.putExtra("eventId", mEventId);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent15);
                    }, this);
                    break;

                case "EventUpdateParticipants":

                    EventManager.participantsUpdatedByInitiator(mEventId, action -> {
                        //LocalBroadCast
                        Intent intent16 = new Intent(Veranstaltung.EVENT_PARTICIPANTS_UPDATED_BY_INITIATOR);
                        intent16.putExtra("eventId", mEventId);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent16);
                    }, this);

                    break;

                case "RemindContact":
                    event = InternalCaching.getEventFromCache(mEventId);
                    if (ParticipantService.isNotifyUser(event)) {
                        EventNotificationManager.pokeNotification(mContext, mEventId);
                    }
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getEventDetail(JSONObject data) {
        try {
            EventManager.getEventDataFromServer(data.get("EventId").toString(), action -> {
                Intent eventReceived = new Intent(Veranstaltung.EVENT_RECEIVED);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(eventReceived);
            }, this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionFailed(String msg, Action action) {
//		try{
//			if(msg==null){
//				msg = UserMessageHandler.getFailureMessage(action, mContext);						
//			}
//			Toast.makeText(mContext,msg,Toast.LENGTH_LONG).show();
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}		
    }
}

package com.redtop.engaze.domain.manager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.redtop.engaze.Interface.IActionHandler;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.Interface.OnEventSaveCompleteListner;
import com.redtop.engaze.Interface.OnRefreshEventListCompleteListner;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.enums.EventState;
import com.redtop.engaze.common.constant.IntentConstants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.domain.Reminder;
import com.redtop.engaze.domain.TrackLocationMember;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.domain.service.EventParser;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.domain.service.ParticipantService;
import com.redtop.engaze.manager.EventNotificationManager;
import com.redtop.engaze.webservice.EventWS;
import com.redtop.engaze.webservice.IEventWS;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


@SuppressLint("SimpleDateFormat")
public class EventManager {
    private final static String TAG = EventManager.class.getName();

    private final static IEventWS eventWS = new EventWS();

    public static Event getEvent(String eventId, Boolean attachContactgroup) {
        Event event = InternalCaching.getEventFromCache(eventId);
        if (event == null) {
            return null;
        }
        if (attachContactgroup) {
            ParticipantManager.setContactsGroup(event.participants);
        }

        if (event.UsersLocationDetailList != null) {
            for (UsersLocationDetail ud : event.UsersLocationDetailList) {
                for (EventParticipant participant : event.participants) {
                    if (participant.userId != null && participant.userId.equals(ud.userId)) {
                        ud.contactOrGroup = participant.contactOrGroup;
                    }
                }
            }
        }
        return event;
    }

    public static List<Event> getRunningEventList() {
        List<Event> list = InternalCaching.getEventListFromCache();
        //list = removePastEvents(context, list);
        List<Event> runningList = new ArrayList<Event>();
        if (list != null) {
            for (Event event : list) {
                if (event.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.Accepted
                        && event.state == EventState.TRACKING_ON) {
                    runningList.add(event);
                }
            }
            EventService.SortListByStartDate(runningList);
        }
        return runningList;
    }

    public static List<Event> getPendingEventList() {
        List<Event> pendingList = new ArrayList<Event>();
        List<Event> list = InternalCaching.getEventListFromCache();
        list.addAll(InternalCaching.getTrackEventListFromCache());
        if (list != null) {
            //list = removePastEvents(context, list);
            if (list != null) {
                for (Event e : list) {
                    if (e.getCurrentParticipant().acceptanceStatus != AcceptanceStatus.Accepted &&
                            e.getCurrentParticipant().acceptanceStatus != AcceptanceStatus.Rejected) {
                        pendingList.add(e);
                    }
                }
                EventService.SortListByStartDate(pendingList);
            }
        }
        return pendingList;
    }

    public static void startEvent(String eventid) {
        Event event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Toast.makeText(AppContext.context, message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, message);
            return;
        }

        event.state = EventState.TRACKING_ON;
        InternalCaching.saveEventToCache(event);

    }

    public static void eventTrackingStart(String eventid) {
        Event event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {
            String message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Toast.makeText(AppContext.context, message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, message);
            return;
        }

        event.state = EventState.TRACKING_ON;
        InternalCaching.saveEventToCache(event);
    }

    public static void eventOver(String eventid) {
        Event event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {
            String message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Toast.makeText(AppContext.context, message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, message);
            return;
        }
        event.state = EventState.EVENT_END;
        EventNotificationManager.cancelAllNotifications(event);
        InternalCaching.removeEventFromCache(eventid);
        checkForReccurrence(event);
    }

    public static void saveEvent(final Event event, final Boolean isMeetNow, final Reminder reminder, final OnEventSaveCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure) {
        try {
            if (!AppContext.context.isInternetEnabled) {
                String message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
                Log.d(TAG, message);
                listnerOnFailure.actionFailed(message, Action.SAVEEVENT);
                return;
            }

            event.startTime = DateUtil.convertToUtcDateTime(event.startTimeInDateFormat, null);
            event.endTime = DateUtil.convertToUtcDateTime(event.endTimeInDateFormat, null);//parseFormat.format(endDate);

            JSONObject jObject = new JSONObject(AppContext.jsonParser.Serialize(event));

            eventWS.SaveEvent(jObject, new OnAPICallCompleteListener<JSONObject>() {

                @Override
                public void apiCallSuccess(JSONObject response) {


                    if (response != null) {
                        Log.d(TAG, "EventResponse:" + response.toString());

                        try {

                            event.eventId = response.getString("id");
                        } catch (Exception ex) {
                            Log.d(TAG, ex.toString());
                            ex.printStackTrace();
                            listnerOnFailure.actionFailed(null, Action.SAVEEVENT);
                        }
                    }

                    event.startTime = DateUtil.convertUtcToLocalDateTime(event.startTime, null);
                    event.endTime = DateUtil.convertUtcToLocalDateTime(event.endTime, null);

                    for (EventParticipant participant : event.participants) {
                        participant.setProfileName();
                    }

                    EventService.setEndEventAlarm(event);
                    if (event.eventType == EventType.QUIK) {
                        event.state = EventState.TRACKING_ON;
                    } else if (event.eventType == EventType.GENERAL) {
                        EventService.setTracking(event);
                        EventService.setEventStarAlarm(event);
                        if (reminder != null) {
                            EventService.setEventReminder(event);

                        }
                        event.state = EventState.EVENT_OPEN;
                    }

                    EventNotificationManager.cancelNotification(event);
                    InternalCaching.saveEventToCache(event);
                    listnerOnSuccess.eventSaveComplete(event);
                }

                @Override
                public void apiCallFailure() {
                    listnerOnFailure.actionFailed(null, Action.SAVEEVENT);
                }
            });
        } catch (
                JSONException e) {
            listnerOnFailure.actionFailed(null, Action.SAVEEVENT);
        }

    }

    public static void saveUserResponse(final AcceptanceStatus userAcceptanceResponse, final String eventid, final OnActionCompleteListner actionlistnerOnSuccess, final OnActionFailedListner listnerOnFailure) {

        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.SAVEUSERRESPONSE);
            return;
        }
        final Event event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.SAVEUSERRESPONSE);
            return;
        }


        eventWS.saveUserResponse(userAcceptanceResponse, eventid, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {

                try {

                    if (userAcceptanceResponse == AcceptanceStatus.Accepted) {
                        event.getCurrentParticipant().
                                acceptanceStatus = AcceptanceStatus.Accepted;
                        SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                        Date startDate = originalformat.parse(event.startTime);
                        Date currentDate = Calendar.getInstance().getTime();
                        if (currentDate.getTime() >= startDate.getTime()) { //quick event
                            event.state = EventState.TRACKING_ON;
                        } else {
                            EventService.setEventStarAlarm(event);
                            EventService.setEventReminder(event);
                            EventService.setTracking(event);
                        }
                    } else {
                        event.getCurrentParticipant().
                                acceptanceStatus = AcceptanceStatus.Rejected;
                    }
                    EventNotificationManager.cancelNotification(event);
                    InternalCaching.saveEventToCache(event);
                    actionlistnerOnSuccess.actionComplete(Action.SAVEUSERRESPONSE);


                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.SAVEUSERRESPONSE);
                }

            }

            @Override
            public void apiCallFailure() {
                listnerOnFailure.actionFailed(null, Action.SAVEUSERRESPONSE);
            }
        });
    }

    public static void getEventDataFromServer(final String eventid, final OnActionCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure) {
        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.GETEVENTDATAFROMSERVER);
            return;
        }
        eventWS.getEventDetail(eventid, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());
                try {


                    List<Event> eventList = EventParser.parseEventDetailList(response.getJSONArray("ListOfEvents"));
                    Event event = eventList.get(0);
                    if (EventService.isEventShareMyLocationEventForCurrentUser(event)) {
                        event.state = EventState.TRACKING_ON;
                    }
                    InternalCaching.saveEventToCache(event);
                    EventService.setEndEventAlarm(event);
                    EventNotificationManager.showEventInviteNotification(event);
                    listnerOnSuccess.actionComplete(Action.GETEVENTDATAFROMSERVER);

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.GETEVENTDATAFROMSERVER);
                }
            }

            @Override
            public void apiCallFailure() {
                listnerOnFailure.actionFailed(null, Action.GETEVENTDATAFROMSERVER);
            }
        });
    }

    public static void leaveEvent(final Event event, final OnActionCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure) {

        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.LEAVEEVENT);
            return;
        }

        if (event == null) {

            message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.LEAVEEVENT);
            return;
        }

        final String eventid = event.eventId;

        eventWS.leaveEvent(eventid, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {

                try {
                    event.getCurrentParticipant().
                            acceptanceStatus = AcceptanceStatus.Rejected;

                    EventNotificationManager.cancelNotification(event);
                    InternalCaching.saveEventToCache(event);
                    listnerOnSuccess.actionComplete(Action.LEAVEEVENT);

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.LEAVEEVENT);
                }

            }

            @Override
            public void apiCallFailure() {
                listnerOnFailure.actionFailed(null, Action.LEAVEEVENT);
            }
        });
    }

    public static void endEvent(final Event event, final OnActionCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure) {

        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.ENDEVENT);
            return;
        }

        if (event == null) {
            message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.ENDEVENT);
            return;
        }
        final String eventid = event.eventId;

        eventWS.endEvent(event.eventId, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                try {
                    EventNotificationManager.cancelAllNotifications(event);
                    EventService.RemoveEndEventAlarm(eventid);
                    InternalCaching.removeEventFromCache(eventid);

                    // Remove the event related items from preferences
                    PreffManager.removePref(eventid);
                    for (EventParticipant i : event.participants) {
                        PreffManager.removePref(i.userId);
                    }

                    checkForReccurrence(event);

                    listnerOnSuccess.actionComplete(Action.ENDEVENT);
                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.ENDEVENT);
                }
            }

            @Override
            public void apiCallFailure() {
                listnerOnFailure.actionFailed(null, Action.ENDEVENT);
            }
        });
    }

    public static void deleteEvent(final Event event, final IActionHandler actionHandler) {
        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            actionHandler.actionFailed(message, Action.DELETEEVENT);
            return;
        }
        if (event == null) {
            message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            actionHandler.actionFailed(message, Action.DELETEEVENT);
            return;
        }
        final String eventid = event.eventId;

        eventWS.endEvent(event.eventId, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());
                try {
                    EventService.RemoveEndEventAlarm(eventid);
                    InternalCaching.removeEventFromCache(eventid);
                    //LocalBroadCast
                    Intent intent = new Intent(IntentConstants.EVENT_DELETE_BY_INITIATOR);
                    intent.putExtra("eventId", event.eventId);
                    LocalBroadcastManager.getInstance(AppContext.context).sendBroadcast(intent);
                    actionHandler.actionComplete(Action.DELETEEVENT);

                } catch (
                        Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    actionHandler.actionFailed(null, Action.DELETEEVENT);
                }

            }

            @Override
            public void apiCallFailure() {
                actionHandler.actionFailed(null, Action.DELETEEVENT);
            }
        });
    }

    public static void changeDestination(final EventPlace destinationPlace, final Context context, final Event event, final OnActionCompleteListner listenerOnSuccess, final OnActionFailedListner listnerOnFailure) {
        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.CHANGEDESTINATION);
            return;
        }

        if (event == null) {
            message = context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.CHANGEDESTINATION);
            return;
        }
        final String eventId = event.eventId;

        eventWS.changeDestination(destinationPlace, eventId, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                try {
                    event.destination = destinationPlace;
                    InternalCaching.saveEventToCache(event);
                    listenerOnSuccess.actionComplete(Action.CHANGEDESTINATION);
                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.CHANGEDESTINATION);
                }
            }

            @Override
            public void apiCallFailure() {
                listnerOnFailure.actionFailed(null, Action.CHANGEDESTINATION);
            }
        });
    }

    public static void extendEventEndTime(final int duration, final Context context, final Event event, final OnActionCompleteListner listenerOnSuccess, final OnActionFailedListner listnerOnFailure) {
        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.EXTENDEVENTENDTIME);
            return;
        }

        if (event == null) {
            message = context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.EXTENDEVENTENDTIME);
            return;
        }
        final String eventid = event.eventId;

        DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(writeFormat.parse(event.endTime));
        } catch (ParseException e) {
            e.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.EXTENDEVENTENDTIME);
            return;
        }
        cal.add(Calendar.MINUTE, duration);
        final Date newEndTime = cal.getTime();
        final String newUTCEndTime = DateUtil.convertToUtcDateTime(newEndTime, null);

        eventWS.extendEventEndTime(newUTCEndTime, eventid, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                event.endTime = DateUtil.convertUtcToLocalDateTime(newUTCEndTime, null);
                EventService.RemoveEndEventAlarm(eventid);
                EventService.setEndEventAlarm(event);
                InternalCaching.saveEventToCache(event);
                try {
                    listenerOnSuccess.actionComplete(Action.EXTENDEVENTENDTIME);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void apiCallFailure() {
                listnerOnFailure.actionFailed(null, Action.EXTENDEVENTENDTIME);
            }
        });
    }

    public static void updateEventWithParticipantResponse(String eventid, String userId, String userName, int eventAcceptanceStateId, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        Event event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.UPDATEEVENTWITHPARTICIPANTRESPONSE);
            return;
        }
        try {
            for (EventParticipant em : event.participants) {
                if (em.userId.toLowerCase().equals(userId.toLowerCase())) {
                    em.acceptanceStatus = AcceptanceStatus.getStatus(eventAcceptanceStateId);
                }
            }
            InternalCaching.saveEventToCache(event);
            if (ParticipantService.isNotifyUser(event) && ParticipantService.isCurrentUserInitiator(event.initiatorId)) {
                EventNotificationManager.showEventResponseNotification(AppContext.context, event, userName, eventAcceptanceStateId);
            }
            listnerOnSuccess.actionComplete(Action.UPDATEEVENTWITHPARTICIPANTRESPONSE);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.UPDATEEVENTWITHPARTICIPANTRESPONSE);
        }

    }

    public static void updateEventWithParticipantLeft(Context context, String eventid, String userId, String userName, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        Event event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.UPDATEEVENTWITHPARTICIPANTLEFT);
            return;
        }
        try {
            for (EventParticipant em : event.participants) {
                if (em.userId.toLowerCase().equals(userId.toLowerCase())) {
                    em.acceptanceStatus = AcceptanceStatus.Rejected;
                }
            }
            InternalCaching.saveEventToCache(event);
            if (ParticipantService.isNotifyUser(event)) {
                EventNotificationManager.showEventLeftNotification(context, event, userName);
            }
            listnerOnSuccess.actionComplete(Action.UPDATEEVENTWITHPARTICIPANTLEFT);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.UPDATEEVENTWITHPARTICIPANTLEFT);
        }

    }

    public static void eventEndedByInitiator(final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        Event event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.EVENTEXTENDEDBYINITIATOR);
            return;
        }
        try {
            event.state = EventState.EVENT_END;
            // Remove Event End Alarm and the entire event from cache
            EventService.RemoveEndEventAlarm(eventid);
            EventNotificationManager.cancelAllNotifications(event);
            if (ParticipantService.isNotifyUser(event)) {
                EventNotificationManager.showEventEndNotification(event);
            }
            InternalCaching.removeEventFromCache(eventid);
            listnerOnSuccess.actionComplete(Action.EVENTEXTENDEDBYINITIATOR);
            checkForReccurrence(event);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.EVENTEXTENDEDBYINITIATOR);
        }
    }

    public static void eventExtendedByInitiator(final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        Event event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.EVENTEXTENDEDBYINITIATOR);
            return;
        }
        try {
            if (ParticipantService.isNotifyUser(event)) {
                EventNotificationManager.showEventExtendedNotification(event);
            }
            //Remove old End Event Alarm and set new one
            EventService.RemoveEndEventAlarm(eventid);
            EventService.setEndEventAlarm(event);

            listnerOnSuccess.actionComplete(Action.EVENTEXTENDEDBYINITIATOR);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.EVENTEXTENDEDBYINITIATOR);
        }
    }

    public static void participantsUpdatedByInitiator(final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        Event event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.PARTICIPANTSUPDATEDBYINITIATOR);
            return;
        }
        try {
            if (ParticipantService.isNotifyUser(event)) {
                EventNotificationManager.showParticipantsUpdatedNotification(event);
            }
            listnerOnSuccess.actionComplete(Action.PARTICIPANTSUPDATEDBYINITIATOR);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.PARTICIPANTSUPDATEDBYINITIATOR);
        }
    }

    public static void eventDeletedByInitiator(final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        Event event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.EVENTDELETEDBYINITIATOR);
            return;
        }
        try {
            EventNotificationManager.cancelAllNotifications(event);
            if (ParticipantService.isNotifyUser(event)) {
                EventNotificationManager.showEventDeleteNotification(event);
            }
            EventService.RemoveEndEventAlarm(eventid);
            InternalCaching.removeEventFromCache(eventid);
            listnerOnSuccess.actionComplete(Action.EVENTDELETEDBYINITIATOR);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.EVENTDELETEDBYINITIATOR);
        }
    }

    public static void eventDestinationChangedByInitiator(final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        Event event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.EVENTDESTINATIONCHANGEDBYINITIATOR);
            return;
        }
        try {
            if (ParticipantService.isNotifyUser(event)) {
                EventNotificationManager.showDestinationChangedNotification(event);
            }
            listnerOnSuccess.actionComplete(Action.EVENTDESTINATIONCHANGEDBYINITIATOR);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.EVENTDESTINATIONCHANGEDBYINITIATOR);
        }
    }

    public static void currentparticipantRemovedByInitiator(final Context context, final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        Event event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.CURRENTPARTICIPANTREMOVEDBYINITIATOR);
            return;
        }
        try {
            EventNotificationManager.cancelNotification(event);
            if (ParticipantService.isNotifyUser(event)) {
                EventNotificationManager.showRemovedFromEventNotification(event);
            }
            EventService.RemoveEndEventAlarm(eventid);
            InternalCaching.removeEventFromCache(eventid);
            listnerOnSuccess.actionComplete(Action.CURRENTPARTICIPANTREMOVEDBYINITIATOR);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.CURRENTPARTICIPANTREMOVEDBYINITIATOR);
        }
    }

    public static void refreshEventList(final OnRefreshEventListCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure) {

        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            if (listnerOnFailure != null) {
                listnerOnFailure.actionFailed(message, Action.REFRESHEVENTLIST);
            }
            return;
        }

        eventWS.RefreshEventListFromServer(new OnAPICallCompleteListener<String>() {

            @SuppressLint("NewApi")
            @Override
            public void apiCallSuccess(String response) {
                try {
                    List<Event> eventList = EventParser.parseEventDetailList(new JSONArray(response));
                    for (Event eventData : eventList) {
                        eventData.startTime = DateUtil.convertUtcToLocalDateTime(eventData.startTime, null);
                        eventData.endTime = DateUtil.convertUtcToLocalDateTime(eventData.endTime, null);
                        for (EventParticipant participant : eventData.participants) {
                            participant.setProfileName();
                        }
                    }

                    if (eventList.size() > 0) {
                        EventService.RemovePastEvents(eventList);
                        EventService.upDateEventStatus(eventList);
                        InternalCaching.saveEventListToCache(eventList);
                    }
                    if (listnerOnSuccess != null) {
                        listnerOnSuccess.RefreshEventListComplete(eventList);
                    }


                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    if (listnerOnFailure != null) {
                        listnerOnFailure.actionFailed(null, Action.REFRESHEVENTLIST);
                    }
                }

            }

            @Override
            public void apiCallFailure() {
                if (listnerOnFailure != null) {
                    listnerOnFailure.actionFailed(null, Action.REFRESHEVENTLIST);
                }
            }
        });
    }

    @SuppressLint("NewApi")
    public static void RemoveALlPastEvents(){
        ArrayList<String> eventIds = new ArrayList<>();
        InternalCaching.getEventListFromCache().forEach(event -> eventIds.add(event.eventId));
        if (eventIds.size() > 0) {
            InternalCaching.removeEventsFromCache(eventIds);
        }
    }

    public static void saveUsersLocationDetailList(Context context, Event event,
                                                   ArrayList<UsersLocationDetail> usersLocationDetailList) {
        if (event != null && event.getCurrentParticipant().acceptanceStatus != AcceptanceStatus.Rejected
                && usersLocationDetailList != null && usersLocationDetailList.size() > 0) {
            event.UsersLocationDetailList = usersLocationDetailList;
            InternalCaching.saveEventToCache(event);
        }

    }

    private static void checkForReccurrence(Event event) {
        Boolean strIsReccurrence = event.IsRecurrence;
        if (strIsReccurrence != null && strIsReccurrence == true) {
            refreshEventList(eventList -> {
                Intent eventRefreshed = new Intent(Veranstaltung.EVENTS_REFRESHED);
                LocalBroadcastManager.getInstance(AppContext.context).sendBroadcast(eventRefreshed);

            }, new OnActionFailedListner() {

                @Override
                public void actionFailed(String msg, Action action) {
                    Log.d(TAG, msg);

                }
            });
        }
    }

//	private static List<EventDetail> removePastEvents(final Context context, List<EventDetail> eventDetailList){		
//		EventHelper.RemovePastEvents(context, eventDetailList);	
//		EventHelper.upDateEventStatus(eventDetailList);
//		InternalCaching.saveEventListToCache(eventDetailList, context);	
//		EventTrackerLocationService.perofomrSartStop(context.getApplicationContext());
//		return eventDetailList;
//	}

    public static List<TrackLocationMember> getListOfTrackingMembers(
            Context context, String inorOut) {
        ArrayList<TrackLocationMember> slist = new ArrayList<TrackLocationMember>();
        List<Event> list = getTrackingEventList(context);
        EventType eventType;
        ArrayList<EventParticipant> members;

        switch (inorOut) {
            case "LocationsOut":
                for (Event e : list) {
                    members = e.participants;
                    ParticipantManager.setContactsGroup(members);
                    eventType = e.eventType;
                    //Out going locations - 100 - Share my location - current user is initiator - add all members except me
                    if (eventType == EventType.SHAREMYLOACTION && ParticipantService.isCurrentUserInitiator(e.initiatorId)) {
                        members.remove(e.getCurrentParticipant());
                        for (EventParticipant mem : members) {
                            slist.add(new TrackLocationMember(e, mem, mem.acceptanceStatus));
                        }
                    }
                    //Out going locations 200 - Track Buddy - Current user is not Initiator - add only initiator but only if I have accepted earlier else it will be in my pending items
                    else if (eventType == EventType.TRACKBUDDY && !ParticipantService.isCurrentUserInitiator(e.initiatorId) && e.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.Accepted) {
                        slist.add(new TrackLocationMember(e, e.getParticipant(e.initiatorId), AcceptanceStatus.Accepted));
                    }
                }
                break;
            case "locationsIn":
                for (Event e : list) {
                    members = e.participants;
                    ParticipantManager.setContactsGroup(members);
                    eventType = e.eventType;
                    //In coming locations - 100 - Share my location - Current user is not Initiator - add only initiator but only if I have accepted earlier else it will be in my pending items
                    if (eventType == EventType.SHAREMYLOACTION && !ParticipantService.isCurrentUserInitiator(e.initiatorId) && e.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.Accepted) {
                        slist.add(new TrackLocationMember(e, e.getParticipant(e.initiatorId), AcceptanceStatus.Accepted));
                    }
                    //In coming locations - 200 - track buddy - Current user is initiator - add all members except me
                    else if (eventType == EventType.TRACKBUDDY && ParticipantService.isCurrentUserInitiator(e.initiatorId)) {
                        e.participants.remove(e.getCurrentParticipant());
                        for (EventParticipant mem : members) {
                            slist.add(new TrackLocationMember(e, mem, mem.acceptanceStatus));
                        }
                    }
                }
                break;
        }
        return slist;
    }

    public static List<Event> getTrackingEventList(Context context) {
        List<Event> list = InternalCaching.getTrackEventListFromCache();
        //removePastEvents(context, list);
        return list;
    }

    public static void removeBuddyFromSharing(Context mContext, String userId,
                                              OnActionCompleteListner onActionCompleteListner,
                                              OnActionFailedListner onActionFailedListner) {
    }
}



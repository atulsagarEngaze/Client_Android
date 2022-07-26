package com.redtop.engaze.manager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import com.redtop.engaze.common.constant.Constants;
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
import com.redtop.engaze.restApi.IEventApi;
import com.redtop.engaze.service.BackgroundLocationService;
import com.redtop.engaze.service.EventNotificationService;
import com.redtop.engaze.restApi.EventApi;
import com.redtop.engaze.service.EventTrackerAlarmReceiverService;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


@SuppressLint("SimpleDateFormat")
public class EventManager {
    private final static String TAG = EventManager.class.getName();

    private final static IEventApi eventApi = new EventApi();

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
            SortListByStartDate(runningList);
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
                SortListByStartDate(pendingList);
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
        EventNotificationService.cancelAllNotifications(event);
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

            eventApi.SaveEvent(jObject, new OnAPICallCompleteListener<JSONObject>() {

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

                    setEndEventAlarm(event);
                    if (event.eventType == EventType.QUIK) {
                        event.state = EventState.TRACKING_ON;
                    } else if (event.eventType == EventType.GENERAL) {
                        setTracking(event);
                        setEventStarAlarm(event);
                        if (reminder != null) {
                            setEventReminder(event);

                        }
                        event.state = EventState.EVENT_OPEN;
                    }

                    EventNotificationService.cancelNotification(event);
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


        eventApi.saveUserResponse(userAcceptanceResponse, eventid, new OnAPICallCompleteListener<JSONObject>() {

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
                            setEventStarAlarm(event);
                            setEventReminder(event);
                            setTracking(event);
                        }
                    } else {
                        event.getCurrentParticipant().
                                acceptanceStatus = AcceptanceStatus.Rejected;
                    }
                    EventNotificationService.cancelNotification(event);
                    InternalCaching.saveEventToCache(event);
                    BackgroundLocationService.start(AppContext.context);
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
        eventApi.getEventDetail(eventid, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());
                try {


                    List<Event> eventList = parseEventDetailList(response.getJSONArray("ListOfEvents"));
                    Event event = eventList.get(0);
                    if (event.isEventShareMyLocationEventForCurrentUser()) {
                        event.state = EventState.TRACKING_ON;
                    }
                    InternalCaching.saveEventToCache(event);
                    setEndEventAlarm(event);
                    EventNotificationService.showEventInviteNotification(event);
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

        eventApi.leaveEvent(eventid, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {

                try {
                    event.getCurrentParticipant().
                            acceptanceStatus = AcceptanceStatus.Rejected;

                    EventNotificationService.cancelNotification(event);
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

        eventApi.endEvent(event.eventId, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                try {
                    EventNotificationService.cancelAllNotifications(event);
                    RemoveEndEventAlarm(eventid);
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

        eventApi.endEvent(event.eventId, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());
                try {
                    RemoveEndEventAlarm(eventid);
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

        eventApi.changeDestination(destinationPlace, eventId, new OnAPICallCompleteListener<JSONObject>() {

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

        eventApi.extendEventEndTime(newUTCEndTime, eventid, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                event.endTime = DateUtil.convertUtcToLocalDateTime(newUTCEndTime, null);
                RemoveEndEventAlarm(eventid);
                setEndEventAlarm(event);
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
            if (ParticipantManager.isNotifyUser(event) && ParticipantManager.isCurrentUserInitiator(event.initiatorId)) {
                EventNotificationService.showEventResponseNotification(AppContext.context, event, userName, eventAcceptanceStateId);
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
            if (ParticipantManager.isNotifyUser(event)) {
                EventNotificationService.showEventLeftNotification(context, event, userName);
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
            RemoveEndEventAlarm(eventid);
            EventNotificationService.cancelAllNotifications(event);
            if (ParticipantManager.isNotifyUser(event)) {
                EventNotificationService.showEventEndNotification(event);
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
            if (ParticipantManager.isNotifyUser(event)) {
                EventNotificationService.showEventExtendedNotification(event);
            }
            //Remove old End Event Alarm and set new one
            RemoveEndEventAlarm(eventid);
            setEndEventAlarm(event);

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
            if (ParticipantManager.isNotifyUser(event)) {
                EventNotificationService.showParticipantsUpdatedNotification(event);
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
            EventNotificationService.cancelAllNotifications(event);
            if (ParticipantManager.isNotifyUser(event)) {
                EventNotificationService.showEventDeleteNotification(event);
            }
            RemoveEndEventAlarm(eventid);
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
            if (ParticipantManager.isNotifyUser(event)) {
                EventNotificationService.showDestinationChangedNotification(event);
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
            EventNotificationService.cancelNotification(event);
            if (ParticipantManager.isNotifyUser(event)) {
                EventNotificationService.showRemovedFromEventNotification(event);
            }
            RemoveEndEventAlarm(eventid);
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

        eventApi.RefreshEventListFromServer(new OnAPICallCompleteListener<String>() {

            @SuppressLint("NewApi")
            @Override
            public void apiCallSuccess(String response) {
                try {
                    List<Event> eventList = parseEventDetailList(new JSONArray(response));
                    for (Event eventData : eventList) {
                        eventData.startTime = DateUtil.convertUtcToLocalDateTime(eventData.startTime, null);
                        eventData.endTime = DateUtil.convertUtcToLocalDateTime(eventData.endTime, null);
                        for (EventParticipant participant : eventData.participants) {
                            participant.setProfileName();
                        }
                    }

                    if (eventList.size() > 0) {
                        RemovePastEvents(eventList);
                        upDateEventStatus(eventList);
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
                    if (eventType == EventType.SHAREMYLOACTION && ParticipantManager.isCurrentUserInitiator(e.initiatorId)) {
                        members.remove(e.getCurrentParticipant());
                        for (EventParticipant mem : members) {
                            slist.add(new TrackLocationMember(e, mem, mem.acceptanceStatus));
                        }
                    }
                    //Out going locations 200 - Track Buddy - Current user is not Initiator - add only initiator but only if I have accepted earlier else it will be in my pending items
                    else if (eventType == EventType.TRACKBUDDY && !ParticipantManager.isCurrentUserInitiator(e.initiatorId) && e.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.Accepted) {
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
                    if (eventType == EventType.SHAREMYLOACTION && !ParticipantManager.isCurrentUserInitiator(e.initiatorId) && e.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.Accepted) {
                        slist.add(new TrackLocationMember(e, e.getParticipant(e.initiatorId), AcceptanceStatus.Accepted));
                    }
                    //In coming locations - 200 - track buddy - Current user is initiator - add all members except me
                    else if (eventType == EventType.TRACKBUDDY && ParticipantManager.isCurrentUserInitiator(e.initiatorId)) {
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

    public static void SortListByStartDate(List<Event> list) {
        final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Collections.sort(list, new Comparator<Event>() {
            public int compare(Event ed1, Event ed2) {


                try {
                    if (dateformat.parse(ed1.startTime).getTime() > dateformat.parse(ed2.startTime).getTime())
                        return 1;
                    else if (dateformat.parse(ed1.startTime).getTime() < dateformat.parse(ed2.startTime).getTime())
                        return -1;
                    else
                        return 0;
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return 0;
                }
            }
        });
    }

    public static void setEndEventAlarm(List<Event> eventList) {
        for (Event event : eventList) {
            setEndEventAlarm(event);
        }
    }

    public static void setEndEventAlarm(Event event) {
        try {

            DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            //DateFormat writeFormat = new SimpleDateFormat( "EEE, dd MMM yyyy hh:mm a");
            Date endDate;
            endDate = writeFormat.parse(event.endTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            Intent intentAlarm = new Intent(AppContext.context, EventTrackerAlarmReceiverService.class);
            intentAlarm.putExtra("AlarmType", Veranstaltung.EVENT_OVER);
            intentAlarm.putExtra("EventId", event.eventId);
            AlarmManager alarmManager = (AlarmManager) AppContext.context.getSystemService(Context.ALARM_SERVICE);
            //set the alarm for particular time
            alarmManager.set(AlarmManager.RTC_WAKEUP, endDate.getTime(), PendingIntent.getBroadcast(AppContext.context, Constants.EventEndBroadcastId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public static void setEventReminder(String eventId) {
        Event eDetail = InternalCaching.getEventFromCache(eventId);
        setEventReminder(eDetail);

    }

    public static void RemoveEndEventAlarm(String eventId) {
        AlarmManager alarmManager = (AlarmManager) AppContext.context
                .getSystemService(Context.ALARM_SERVICE);

        Intent intentAlarm = new Intent(AppContext.context, EventTrackerAlarmReceiverService.class);
        intentAlarm.putExtra("AlarmType", Veranstaltung.EVENT_OVER);
        intentAlarm.putExtra("EventId", eventId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(AppContext.context,
                Constants.EventStartBroadcastId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);

    }

    public static void setEventStarAlarm(Event event) {
        try {

            DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            //DateFormat writeFormat = new SimpleDateFormat( "EEE, dd MMM yyyy hh:mm a");
            Date startDate;
            startDate = writeFormat.parse(event.startTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            Intent intentAlarm = new Intent(AppContext.context, EventTrackerAlarmReceiverService.class);
            intentAlarm.putExtra("AlarmType", Veranstaltung.EVENT_START);
            intentAlarm.putExtra("EventId", event.eventId);
            AlarmManager alarmManager = (AlarmManager) AppContext.context.getSystemService(Context.ALARM_SERVICE);
            //set the alarm for particular time
            alarmManager.set(AlarmManager.RTC_WAKEUP, startDate.getTime(), PendingIntent.getBroadcast(AppContext.context, Constants.EventStartBroadcastId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public static void setEventReminder(Event event) {
        try {

            DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            //DateFormat writeFormat = new SimpleDateFormat( "EEE, dd MMM yyyy hh:mm a");

            Date startDate = writeFormat.parse(event.startTime);
            Calendar cal = Calendar.getInstance();

            cal.setTime(startDate);
            cal.add(Calendar.MINUTE, (int)event.reminder.ReminderOffsetInMinute * -1);
            Date reminderDate = cal.getTime();
            //if(reminderDate.getTime() > currentDate.getTime()){

            Intent intentAlarm = new Intent(AppContext.context, EventTrackerAlarmReceiverService.class);
            intentAlarm.putExtra("AlarmType", Veranstaltung.EVENT_REMINDER);
            intentAlarm.putExtra("ReminderType", event.reminder.getNotificationType());
            intentAlarm.putExtra("EventId", event.eventId);
            AlarmManager alarmManager = (AlarmManager) AppContext.context.getSystemService(Context.ALARM_SERVICE);
            //set the alarm for particular time
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderDate.getTime(), PendingIntent.getBroadcast(AppContext.context, Constants.ReminderBroadcastId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
            //}

        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public static void setTracking(Event event) {
        try {

            long trackingAlarmOffset = 0;

            DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            //tracking start time
            Date startDate = writeFormat.parse(event.startTime);
            Calendar cal = Calendar.getInstance();

            Date currentDate = cal.getTime();
            cal.setTime(startDate);

            cal.add(Calendar.MINUTE, event.tracking.getOffSetInMinutes() * -1);
            Date trackingStartDate = cal.getTime();


            if (trackingStartDate.getTime() < currentDate.getTime()) {
                trackingAlarmOffset = currentDate.getTime() + 5000;
            } else {

                trackingAlarmOffset = trackingStartDate.getTime();
            }
            Intent intentAlarm = new Intent(AppContext.context, EventTrackerAlarmReceiverService.class);
            intentAlarm.putExtra("AlarmType", Veranstaltung.TRACKING_STARTED);
            intentAlarm.putExtra("EventId", event.eventId);
            AlarmManager alarmManager = (AlarmManager) AppContext.context.getSystemService(Context.ALARM_SERVICE);
            //set the alarm for particular time
            alarmManager.set(AlarmManager.RTC_WAKEUP, trackingAlarmOffset, PendingIntent.getBroadcast(AppContext.context, Constants.TrackingStartBroadcastId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public static void upDateEventStatus(List<Event> eventList) {
        try {
            SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date startDate = null;
            Calendar cal = null;
            for (Event ed : eventList) {
                cal = Calendar.getInstance();
                startDate = originalformat.parse(ed.startTime);
                cal.setTime(startDate);
                cal.add(Calendar.MINUTE, ed.tracking.getOffSetInMinutes() * -1);
                Date currentDate = Calendar.getInstance().getTime();
                if (cal.getTime().getTime() - currentDate.getTime() < 0) {
                    ed.state = EventState.TRACKING_ON;
                } else {
                    ed.state = EventState.EVENT_OPEN;
                }
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void RemovePastEvents(List<Event> eventList) {
        List<Event> tobeRemoved = new ArrayList<Event>();
        for (Event event : eventList) {
            if (event.isEventPast()) {
                tobeRemoved.add(event);
            }
        }
        eventList.removeAll(tobeRemoved);
    }



    public static long getTimeToFinish(String eventEndTime, String format) {

        DateFormat writeFormat = new SimpleDateFormat(format);
        Date parsedEventEndTime = null;
        try {
            parsedEventEndTime = writeFormat.parse(eventEndTime);
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        long diff = (parsedEventEndTime.getTime() - new Date().getTime());
        return diff;
    }

    public static long pendingEventTime(String eventEndTime) {

        DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date parsedEventEndTime = null;
        try {
            parsedEventEndTime = writeFormat.parse(eventEndTime);
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        long diff = (parsedEventEndTime.getTime() - new Date().getTime());
        return diff;
    }

    public static void removeLocationServiceCheckAlarm() {
        AlarmManager alarmManager = (AlarmManager) AppContext.context
                .getSystemService(Context.ALARM_SERVICE);

        Intent intentAlarm = new Intent(AppContext.context, EventTrackerAlarmReceiverService.class);
        intentAlarm.putExtra("AlarmType", Constants.CHECK_LOCATION_SERVICE);


        PendingIntent pendingIntent = PendingIntent.getBroadcast(AppContext.context,
                Constants.EventStartBroadcastId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);
    }

    public static void setLocationServiceCheckAlarm() {
        try {

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, 5);

            Intent intentAlarm = new Intent(AppContext.context, EventTrackerAlarmReceiverService.class);
            intentAlarm.putExtra("AlarmType", Constants.CHECK_LOCATION_SERVICE);

            AlarmManager alarmManager = (AlarmManager) AppContext.context.getSystemService(Context.ALARM_SERVICE);
            //remove existing alarm
            PendingIntent pendingIntent = PendingIntent.getBroadcast(AppContext.context,
                    Constants.EventStartBroadcastId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.cancel(pendingIntent);

            //set new  alarm
            alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTime().getTime(), PendingIntent.getBroadcast(AppContext.context, Constants.LocationServiceCheckBroadcastId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public static Boolean isAnyEventInState(EventState state, Boolean checkOnlyWhenEventAccepted) {
        List<Event> events = InternalCaching.getEventListFromCache();
        if (events == null) {
            return false;
        }
        for (Event ed : events) {
            if (ed.state == state) {
                if (checkOnlyWhenEventAccepted) {

                    if (ed.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.Accepted
                    ) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;

    }

    public static Boolean shouldShareLocation() {
        List<Event> events = InternalCaching.getEventListFromCache();
        List<Event> trackingEvents = InternalCaching.getTrackEventListFromCache();
        if (events == null || events.size() == 0) {
            return false;
        }
        for (Event ed : events) {
            if (ed.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.Accepted
                    && ed.state == EventState.TRACKING_ON
            ) {
                return true;
            }
        }
        if (trackingEvents == null || trackingEvents.size() == 0) {
            return false;
        }
        for (Event ed : trackingEvents) {
            if (ed.isEventShareMyLocationEventForCurrentUser()) {
                return true;
            }
        }
        return false;
    }

    public static JSONObject createPokeAllContactsJSON(Event ed) {
        JSONObject jobj = new JSONObject();

        try {
            jobj.put("RequestorId", AppContext.context.loginId);
            jobj.put("EventId", ed.eventId);
            jobj.put("RequestorName", AppContext.context.loginName);
            jobj.put("EventName", ed.name);
            jobj.put("EventId", ed.eventId);
            //			jobj.put("ContactNumbersForRemind", conactsArray);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jobj;
    }

    public static List<Event> parseEventDetailList(JSONArray jsonStr) {
        JSONArray eventDetailJsonArray = jsonStr;
        List<Event> eventList = new ArrayList<Event>();

        try {
            for (int i = 0; i < eventDetailJsonArray.length(); i++) {
                eventList.add(AppContext.jsonParser.deserialize
                        (eventDetailJsonArray.getJSONObject(i).toString(),
                                Event.class));
            }

            for (Event ev : eventList) {
                ParticipantManager.setCurrentParticipant(ev);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eventList;
    }
}



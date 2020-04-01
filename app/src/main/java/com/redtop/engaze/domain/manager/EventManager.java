package com.redtop.engaze.domain.manager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.Interface.OnEventSaveCompleteListner;
import com.redtop.engaze.Interface.OnRefreshEventListCompleteListner;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.PreffManager;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.enums.EventState;
import com.redtop.engaze.common.constant.IntentConstants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.domain.EventDetail;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.domain.Reminder;
import com.redtop.engaze.domain.TrackLocationMember;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.domain.service.ParticipantService;
import com.redtop.engaze.manager.EventNotificationManager;
import com.redtop.engaze.service.EventTrackerLocationService;
import com.redtop.engaze.webservice.EventWS;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


@SuppressLint("SimpleDateFormat")
public class EventManager {
    private final static String TAG = EventManager.class.getName();

    public static List<EventDetail> getRunningEventList() {
        List<EventDetail> list = InternalCaching.getEventListFromCache();
        //list = removePastEvents(context, list);
        List<EventDetail> runningList = new ArrayList<EventDetail>();
        if (list != null) {
            for (EventDetail e : list) {
                if (e.getCurrentParticipant().getAcceptanceStatus() == AcceptanceStatus.ACCEPTED
                        && e.getState().equals(EventState.TRACKING_ON)) {
                    runningList.add(e);
                }
            }
            EventService.SortListByStartDate(runningList);
        }
        return runningList;
    }

    public static List<EventDetail> getPendingEventList() {
        List<EventDetail> pendingList = new ArrayList<EventDetail>();
        List<EventDetail> list = InternalCaching.getEventListFromCache();
        list.addAll(InternalCaching.getTrackEventListFromCache());
        if (list != null) {
            //list = removePastEvents(context, list);
            if (list != null) {
                for (EventDetail e : list) {
                    if (e.getCurrentParticipant().getAcceptanceStatus() != AcceptanceStatus.ACCEPTED &&
                            e.getCurrentParticipant().getAcceptanceStatus() != AcceptanceStatus.DECLINED) {
                        pendingList.add(e);
                    }
                }
                EventService.SortListByStartDate(pendingList);
            }
        }
        return pendingList;
    }

    public static void startEvent(String eventid) {
        EventDetail event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Toast.makeText(AppContext.context, message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, message);
            return;
        }

        event.setState(EventState.TRACKING_ON);
        InternalCaching.saveEventToCache(event);
        EventTrackerLocationService.peroformSartStop();

    }

    public static void eventTrackingStart(String eventid) {
        EventDetail event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {
            String message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Toast.makeText(AppContext.context, message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, message);
            return;
        }

        event.setState(EventState.TRACKING_ON);
        InternalCaching.saveEventToCache(event);
        EventTrackerLocationService.peroformSartStop();

    }

    public static void eventOver(String eventid) {
        EventDetail event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {
            String message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Toast.makeText(AppContext.context, message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, message);
            return;
        }
        event.setState(EventState.EVENT_END);
        EventNotificationManager.cancelAllNotifications(event);
        EventTrackerLocationService.peroformSartStop();
        InternalCaching.removeEventFromCache(eventid);
        checkForReccurrence(event);
    }

    public static void saveEvent(final Context context, final JSONObject mEventJobj, final Boolean isMeetNow, final Reminder reminder, final OnEventSaveCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure) {

        if (!AppUtility.isNetworkAvailable(context)) {
            String message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.SAVEEVENT);
            return;
        }
        EventWS.CreateEvent(mEventJobj, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());

                try {
                    String Status = (String) response.getString("Status");

                    if (Status == "true") {
                        EventDetail eventDetailData = EventService.parseEventDetailList(response.getJSONArray("ListOfEvents")).get(0);
                        int eventTypeId = Integer.parseInt(eventDetailData.getEventTypeId());
                        EventService.setEndEventAlarm(eventDetailData);
                        if (isMeetNow) {
                            eventDetailData.setState(EventState.TRACKING_ON);
                            eventDetailData.isQuickEvent = "true";
                        } else if (eventTypeId == 100 || eventTypeId == 200) {
                        } else {
                            EventService.setTracking(eventDetailData);
                            EventService.setEventStarAlarm(eventDetailData);
                            if (reminder != null) {
                                EventService.setEventReminder(eventDetailData);

                            }
                            EventService.setEventReminder(eventDetailData);
                            eventDetailData.setState(EventState.EVENT_OPEN);
                            eventDetailData.isQuickEvent = "false";
                        }
                        EventNotificationManager.cancelNotification(eventDetailData);
                        InternalCaching.saveEventToCache(eventDetailData);
                        EventTrackerLocationService.peroformSartStop();
                        listnerOnSuccess.eventSaveComplete(eventDetailData);
                    } else {
                        listnerOnFailure.actionFailed(null, Action.SAVEEVENT);
                    }

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.SAVEEVENT);
                }

            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                listnerOnFailure.actionFailed(null, Action.SAVEEVENT);
            }
        });
    }

    public static void saveUserResponse(final AcceptanceStatus userAcceptanceResponse, final String eventid, final OnActionCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure) {

        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.SAVEUSERRESPONSE);
            return;
        }
        final EventDetail event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.SAVEUSERRESPONSE);
            return;
        }


        EventWS.saveUserResponse(userAcceptanceResponse, eventid, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());

                try {
                    String Status = (String) response.getString("Status");

                    if (Status == "true") {

                        if (userAcceptanceResponse == AcceptanceStatus.ACCEPTED) {
                            event.getCurrentParticipant().
                                    setAcceptanceStatus(AcceptanceStatus.ACCEPTED);
                            SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                            Date startDate = originalformat.parse(event.getStartTime());
                            Date currentDate = Calendar.getInstance().getTime();
                            if (currentDate.getTime() >= startDate.getTime()) { //quick event
                                event.setState(EventState.TRACKING_ON);
                            } else {
                                EventService.setEventStarAlarm(event);
                                EventService.setEventReminder(event);
                                EventService.setTracking(event);
                            }
                        } else {
                            event.getCurrentParticipant().
                                    setAcceptanceStatus(AcceptanceStatus.DECLINED);
                        }
                        EventNotificationManager.cancelNotification(event);
                        InternalCaching.saveEventToCache(event);
                        EventTrackerLocationService.peroformSartStop();
                        listnerOnSuccess.actionComplete(Action.SAVEUSERRESPONSE);
                    } else {
                        listnerOnFailure.actionFailed(null, Action.SAVEUSERRESPONSE);
                    }

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.SAVEUSERRESPONSE);
                }

            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                if (response != null) {
                    Log.d(TAG, "EventResponse:" + response.toString());
                }
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
        EventWS.getEventDetail(eventid, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());
                try {

                    String Status = (String) response.getString("Status");
                    if (Status == "true") {
                        List<EventDetail> eventDetailList = EventService.parseEventDetailList(response.getJSONArray("ListOfEvents"));
                        EventDetail event = eventDetailList.get(0);
                        if (EventService.isEventShareMyLocationEventForCurrentuser(event)) {
                            event.setState(EventState.TRACKING_ON);
                        }
                        InternalCaching.saveEventToCache(event);
                        EventService.setEndEventAlarm(event);
                        EventNotificationManager.showEventInviteNotification(event);
                        listnerOnSuccess.actionComplete(Action.GETEVENTDATAFROMSERVER);
                    } else {
                        listnerOnFailure.actionFailed(null, Action.GETEVENTDATAFROMSERVER);
                    }
                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.GETEVENTDATAFROMSERVER);
                }

            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                if (response != null) {
                    Log.d(TAG, "EventResponse:" + response.toString());
                }
                listnerOnFailure.actionFailed(null, Action.GETEVENTDATAFROMSERVER);

            }
        });
    }

    public static void leaveEvent(final EventDetail event, final OnActionCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure) {

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

        final String eventid = event.getEventId();

        EventWS.leaveEvent(eventid, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());

                try {
                    String Status = (String) response.getString("Status");

                    if (Status == "true") {
                        event.getCurrentParticipant().
                                setAcceptanceStatus(AcceptanceStatus.DECLINED);

                        EventNotificationManager.cancelNotification(event);
                        InternalCaching.saveEventToCache(event);
                        EventTrackerLocationService.peroformSartStop();
                        listnerOnSuccess.actionComplete(Action.LEAVEEVENT);
                    } else {
                        listnerOnFailure.actionFailed(null, Action.LEAVEEVENT);
                    }

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.LEAVEEVENT);
                }

            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                if (response != null) {
                    Log.d(TAG, "EventResponse:" + response.toString());
                }
                listnerOnFailure.actionFailed(null, Action.LEAVEEVENT);
            }
        });
    }

    public static void endEvent(final EventDetail event, final OnActionCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure) {

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
        final String eventid = event.getEventId();

        EventWS.endEvent(event.getEventId(), new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());
                try {

                    String Status = (String) response.getString("Status");
                    if (Status == "true") {
                        EventNotificationManager.cancelAllNotifications(event);
                        EventService.RemoveEndEventAlarm(eventid);
                        EventTrackerLocationService.peroformSartStop();
                        InternalCaching.removeEventFromCache(eventid);

                        // Remove the event related items from preferences
                        PreffManager.removePref(eventid);
                        for (EventParticipant i : event.getParticipants()) {
                            PreffManager.removePref(i.getUserId());
                        }

                        checkForReccurrence(event);
                    } else {
                        listnerOnFailure.actionFailed(null, Action.ENDEVENT);
                    }

                    listnerOnSuccess.actionComplete(Action.ENDEVENT);
                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.ENDEVENT);
                }


            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                listnerOnFailure.actionFailed(null, Action.ENDEVENT);
            }
        });

    }

    public static void deleteEvent(final EventDetail event, final OnActionCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure) {
        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.DELETEEVENT);
            return;
        }
        if (event == null) {
            message = AppContext.context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.DELETEEVENT);
            return;
        }
        final String eventid = event.getEventId();

        EventWS.endEvent(event.getEventId(), new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());
                try {

                    String Status = (String) response.getString("Status");
                    if (Status == "true") {
                        EventService.RemoveEndEventAlarm(eventid);
                        InternalCaching.removeEventFromCache(eventid);
                        //LocalBroadCast
                        Intent intent = new Intent(IntentConstants.EVENT_DELETE_BY_INITIATOR);
                        intent.putExtra("eventId", event.getEventId());
                        LocalBroadcastManager.getInstance(AppContext.context).sendBroadcast(intent);
                        listnerOnSuccess.actionComplete(Action.DELETEEVENT);
                    } else {
                        listnerOnFailure.actionFailed(null, Action.DELETEEVENT);
                    }
                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.DELETEEVENT);
                }

            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                listnerOnFailure.actionFailed(null, Action.DELETEEVENT);
            }
        });
    }


    public static void changeDestination(final EventPlace destinationPlace, final Context context, final EventDetail event, final OnActionCompleteListner listenerOnSuccess, final OnActionFailedListner listnerOnFailure) {
        String message = "";
        if (!AppUtility.isNetworkAvailable(context)) {
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
        final String eventId = event.getEventId();

        EventWS.changeDestination(destinationPlace, eventId, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                try {
                    String Status = (String) response.getString("Status");
                    if (Status == "true") {
                        event.setDestinationLatitude(String.valueOf(destinationPlace.getLatitude()));
                        event.setDestinationLongitude(String.valueOf(destinationPlace.getLongitude()));
                        event.setDestinationName(destinationPlace.getName());
                        event.setDestinationAddress(destinationPlace.getAddress());
                        InternalCaching.saveEventToCache(event);
                        listenerOnSuccess.actionComplete(Action.CHANGEDESTINATION);
                    } else {
                        listnerOnFailure.actionFailed(null, Action.CHANGEDESTINATION);
                    }
                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.CHANGEDESTINATION);
                }
            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                listnerOnFailure.actionFailed(null, Action.CHANGEDESTINATION);
            }
        });

    }

    public static void extendEventEndTime(final int i, final Context context, final EventDetail event, final OnActionCompleteListner listenerOnSuccess, final OnActionFailedListner listnerOnFailure) {
        String message = "";
        if (!AppUtility.isNetworkAvailable(context)) {
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
        final String eventid = event.getEventId();

        EventWS.extendEventEndTime(i, eventid, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                try {
                    String Status = (String) response.getString("Status");
                    if (Status == "true") {
                        DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                        Date endTime = writeFormat.parse(event.getEndTime());
                        Calendar cal = Calendar.getInstance();

                        cal.setTime(endTime);
                        cal.add(Calendar.MINUTE, i);

                        String newEndTime = writeFormat.format(cal.getTime());
                        event.setEndTime(newEndTime);

                        EventService.RemoveEndEventAlarm(eventid);
                        EventService.setEndEventAlarm(event);
                        InternalCaching.saveEventToCache(event);
                        listenerOnSuccess.actionComplete(Action.EXTENDEVENTENDTIME);
                    } else {
                        listnerOnFailure.actionFailed(null, Action.EXTENDEVENTENDTIME);
                    }
                } catch (JSONException | ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.EXTENDEVENTENDTIME);
                }
            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                listnerOnFailure.actionFailed(null, Action.EXTENDEVENTENDTIME);
            }
        });

    }

    public static void updateEventWithParticipantResponse(Context context, String eventid, String userId, String userName, int eventAcceptanceStateId, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        EventDetail event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.UPDATEEVENTWITHPARTICIPANTRESPONSE);
            return;
        }
        try {
            for (EventParticipant em : event.getParticipants()) {
                if (em.getUserId().toLowerCase().equals(userId.toLowerCase())) {
                    em.setAcceptanceStatus(AcceptanceStatus.getStatus(eventAcceptanceStateId));
                }
            }
            InternalCaching.saveEventToCache(event);
            if (ParticipantService.isNotifyUser(event) && ParticipantService.isCurrentUserInitiator(event.getInitiatorId())) {
                EventNotificationManager.showEventResponseNotification(context, event, userName, eventAcceptanceStateId);
            }
            listnerOnSuccess.actionComplete(Action.UPDATEEVENTWITHPARTICIPANTRESPONSE);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.UPDATEEVENTWITHPARTICIPANTRESPONSE);
        }

    }

    public static void updateEventWithParticipantLeft(Context context, String eventid, String userId, String userName, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        EventDetail event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.UPDATEEVENTWITHPARTICIPANTLEFT);
            return;
        }
        try {
            for (EventParticipant em : event.getParticipants()) {
                if (em.getUserId().toLowerCase().equals(userId.toLowerCase())) {
                    em.setAcceptanceStatus(AcceptanceStatus.DECLINED);
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

    public static void eventEndedByInitiator(final Context context, final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        EventDetail event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = context.getResources().getString(R.string.message_general_event_null_error);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.EVENTEXTENDEDBYINITIATOR);
            return;
        }
        try {
            event.setState(EventState.EVENT_END);
            // Remove Event End Alarm and the entire event from cache
            EventService.RemoveEndEventAlarm(eventid);
            EventNotificationManager.cancelAllNotifications(event);
            if (ParticipantService.isNotifyUser(event)) {
                EventNotificationManager.showEventEndNotification(event);
            }
            EventTrackerLocationService.peroformSartStop();
            InternalCaching.removeEventFromCache(eventid);
            listnerOnSuccess.actionComplete(Action.EVENTEXTENDEDBYINITIATOR);
            checkForReccurrence(event);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.EVENTEXTENDEDBYINITIATOR);
        }
    }

    public static void eventExtendedByInitiator(final Context context, final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        EventDetail event = InternalCaching.getEventFromCache(eventid);
        if (event == null) {

            String message = context.getResources().getString(R.string.message_general_event_null_error);
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
            EventTrackerLocationService.peroformSartStop();

            listnerOnSuccess.actionComplete(Action.EVENTEXTENDEDBYINITIATOR);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.EVENTEXTENDEDBYINITIATOR);
        }
    }

    public static void participantsUpdatedByInitiator(final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        EventDetail event = InternalCaching.getEventFromCache(eventid);
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
        EventDetail event = InternalCaching.getEventFromCache(eventid);
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
            EventTrackerLocationService.peroformSartStop();
            InternalCaching.removeEventFromCache(eventid);
            listnerOnSuccess.actionComplete(Action.EVENTDELETEDBYINITIATOR);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.actionFailed(null, Action.EVENTDELETEDBYINITIATOR);
        }
    }

    public static void eventDestinationChangedByInitiator(final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure) {
        EventDetail event = InternalCaching.getEventFromCache(eventid);
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
        EventDetail event = InternalCaching.getEventFromCache(eventid);
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
            EventTrackerLocationService.peroformSartStop();
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
        if (AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            if (listnerOnFailure != null) {
                listnerOnFailure.actionFailed(message, Action.REFRESHEVENTLIST);
            }
            return;
        }

        EventWS.RefreshEventListFromServer(new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {

                try {
                    String Status = (String) response.getString("Status");
                    Log.d(TAG, "EventResponse status:" + Status);
                    if (Status == "true") {
                        List<EventDetail> eventDetailList = EventService.parseEventDetailList(response.getJSONArray("ListOfEvents"));
                        EventService.RemovePastEvents(eventDetailList);
                        EventService.upDateEventStatus(eventDetailList);
                        InternalCaching.saveEventListToCache(eventDetailList);
                        EventTrackerLocationService.peroformSartStop();
                        if (listnerOnSuccess != null) {
                            listnerOnSuccess.RefreshEventListComplete(eventDetailList);
                        }
                    } else {
                        if (listnerOnFailure != null) {
                            listnerOnFailure.actionFailed(null, Action.REFRESHEVENTLIST);
                        }
                    }

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    if (listnerOnFailure != null) {
                        listnerOnFailure.actionFailed(null, Action.REFRESHEVENTLIST);
                    }
                }

            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                if (response != null) {
                    Log.d(TAG, "EventResponse:" + response.toString());
                }
                if (listnerOnFailure != null) {
                    listnerOnFailure.actionFailed(null, Action.REFRESHEVENTLIST);
                }
            }
        });
    }

    public static void saveUsersLocationDetailList(Context context, EventDetail event,
                                                   ArrayList<UsersLocationDetail> usersLocationDetailList) {
        if (event != null && event.getCurrentParticipant().getAcceptanceStatus() != AcceptanceStatus.DECLINED
                && usersLocationDetailList != null && usersLocationDetailList.size() > 0) {
            event.setUsersLocationDetailList(usersLocationDetailList);
            InternalCaching.saveEventToCache(event);
        }

    }

    private static void checkForReccurrence(EventDetail event) {
        String strIsReccurrence = event.getIsRecurrence();
        if (strIsReccurrence != null && strIsReccurrence.equals("true")) {
            refreshEventList(new OnRefreshEventListCompleteListner() {

                @Override
                public void RefreshEventListComplete(List<EventDetail> eventDetailList) {
                    Intent eventRefreshed = new Intent(Veranstaltung.EVENTS_REFRESHED);
                    LocalBroadcastManager.getInstance(AppContext.context).sendBroadcast(eventRefreshed);

                }
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
        List<EventDetail> list = getTrackingEventList(context);
        int eventTypeId;
        ArrayList<EventParticipant> members;

        switch (inorOut) {
            case "LocationsOut":
                for (EventDetail e : list) {
                    members = e.getParticipants();
                    ContactAndGroupListManager.assignContactsToEventMembers(members);
                    eventTypeId = Integer.parseInt(e.getEventTypeId());
                    //Out going locations - 100 - Share my location - current user is initiator - add all members except me
                    if (eventTypeId == 100 && ParticipantService.isCurrentUserInitiator(e.getInitiatorId())) {
                        members.remove(e.getCurrentParticipant());
                        for (EventParticipant mem : members) {
                            slist.add(new TrackLocationMember(e, mem, mem.getAcceptanceStatus()));
                        }
                    }
                    //Out going locations 200 - Track Buddy - Current user is not Initiator - add only initiator but only if I have accepted earlier else it will be in my pending items
                    else if (eventTypeId == 200 && !ParticipantService.isCurrentUserInitiator(e.getInitiatorId()) && e.getCurrentParticipant().getAcceptanceStatus() == AcceptanceStatus.ACCEPTED) {
                        slist.add(new TrackLocationMember(e, e.getMember(e.getInitiatorId()), AcceptanceStatus.ACCEPTED));
                    }
                }
                break;
            case "locationsIn":
                for (EventDetail e : list) {
                    members = e.getParticipants();
                    ContactAndGroupListManager.assignContactsToEventMembers(members, context);
                    eventTypeId = Integer.parseInt(e.getEventTypeId());
                    //In coming locations - 100 - Share my location - Current user is not Initiator - add only initiator but only if I have accepted earlier else it will be in my pending items
                    if (eventTypeId == 100 && !ParticipantService.isCurrentUserInitiator(e.getInitiatorId()) && e.getCurrentParticipant().getAcceptanceStatus() == AcceptanceStatus.ACCEPTED) {
                        slist.add(new TrackLocationMember(e, e.getMember(e.getInitiatorId()), AcceptanceStatus.ACCEPTED));
                    }
                    //In coming locations - 200 - track buddy - Current user is initiator - add all members except me
                    else if (eventTypeId == 200 && ParticipantService.isCurrentUserInitiator(e.getInitiatorId())) {
                        e.getParticipants().remove(e.getCurrentParticipant());
                        for (EventParticipant mem : members) {
                            slist.add(new TrackLocationMember(e, mem, mem.getAcceptanceStatus()));
                        }
                    }
                }
                break;
        }
        return slist;
    }

    public static List<EventDetail> getTrackingEventList(Context context) {
        List<EventDetail> list = InternalCaching.getTrackEventListFromCache();
        //removePastEvents(context, list);
        return list;
    }

    public static void removeBuddyFromSharing(Context mContext, String userId,
                                              OnActionCompleteListner onActionCompleteListner,
                                              OnActionFailedListner onActionFailedListner) {
    }
}



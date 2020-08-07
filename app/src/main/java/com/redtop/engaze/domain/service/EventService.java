package com.redtop.engaze.domain.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.EventState;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.service.EventTrackerAlarmReceiverService;

@SuppressLint("SimpleDateFormat")
public class EventService {

    @SuppressLint("SimpleDateFormat")
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
            if (isEventPast(event)) {
                tobeRemoved.add(event);
            }
        }
        eventList.removeAll(tobeRemoved);
    }

    public static Boolean isEventPast(Event event) {

        try {
            Calendar cal = Calendar.getInstance();
            Date currentDate = cal.getTime();
            DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            cal.setTime(writeFormat.parse(event.endTime));
            Date endDate = cal.getTime();
            if (currentDate.getTime() > endDate.getTime()) {
                return true;
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
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

    public static boolean isEventTrackBuddyEventForCurrentUser(Event event) {

        boolean isCurrentUserInitiator = ParticipantService.isCurrentUserInitiator(event.initiatorId);

        if ((isCurrentUserInitiator && event.eventType == EventType.TRACKBUDDY) ||
                (!isCurrentUserInitiator && event.eventType == EventType.SHAREMYLOACTION)) {
            return true;
        }
        return false;
    }

    public static boolean isEventShareMyLocationEventForCurrentUser(Event event) {

        boolean isCurrentUserInitiator = ParticipantService.isCurrentUserInitiator(event.initiatorId);

        if ((isCurrentUserInitiator && event.eventType == EventType.SHAREMYLOACTION) ||
                (!isCurrentUserInitiator && event.eventType == EventType.TRACKBUDDY)) {
            return true;
        }
        return false;
    }

    public static Boolean isAnyEventInState(EventState state, Boolean checkOnlyWhenEventAccepted) {
        List<Event> events = InternalCaching.getEventListFromCache();
        if (events == null) {
            return false;
        }
        for (Event ed : events) {
            if (ed.state == state) {
                if (checkOnlyWhenEventAccepted) {

                    if (ed.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.ACCEPTED
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
            if (ed.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.ACCEPTED
                    && ed.state == EventState.TRACKING_ON
            ) {
                return true;
            }
        }
        if (trackingEvents == null || trackingEvents.size() == 0) {
            return false;
        }
        for (Event ed : trackingEvents) {
            if (isEventShareMyLocationEventForCurrentUser(ed)) {
                return true;
            }
        }
        return false;
    }
}
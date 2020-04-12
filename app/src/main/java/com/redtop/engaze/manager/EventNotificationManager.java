package com.redtop.engaze.manager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.redtop.engaze.EventsActivity;
import com.redtop.engaze.HomeActivity;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.R;
import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.utility.UserMessageHandler;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.domain.service.ParticipantService;
import com.redtop.engaze.service.EventTrackerAlarmReceiverService;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

// NotificationManager : Allows us to notify the user that something happened in the background
// AlarmManager : Allows you to schedule for your application to do something at a later date
// even if it is in the background

public class EventNotificationManager {

    private static ArrayList<String> responseInProcessEvents = new ArrayList<String>();
    private final static String TAG = EventNotificationManager.class.getName();
    private static int distanceAlarmDuration = 15000;//milliseconds
    static int notificationId = 0;
    static String currentNotificationEventId;
    static Boolean isPokeNotification = false;
    static String notificationType;

    // Used to track if notification is active in the task bar
    static boolean isNotificActive = false;
    private static Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private static Uri pokenotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    private static Ringtone ringtone;

    public static void showReminderNotification(Event event) {

        notificationId = getIncrementedNotificationId();
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(AppContext.context);

        DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        long minutes = 0;
        try {
            Date startDate = writeFormat.parse(event.StartTime);
            Calendar cal = Calendar.getInstance();
            Date currentDate = cal.getTime();
            minutes = (startDate.getTime() - currentDate.getTime()) / (60 * 1000);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String durationMessage = "";
        if (minutes > 0) {
            durationMessage = "will start in " + Long.toString(minutes) + " mins";

            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(EventsActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            Intent activityIntent = new Intent(AppContext.context, EventsActivity.class);
            stackBuilder.addNextIntent(activityIntent);
        } else {
            durationMessage = "is running";
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(RunningEventActivity.class);
            Intent activityIntent = new Intent(AppContext.context, RunningEventActivity.class);
            activityIntent.putExtra("EventId", event.EventId);
            stackBuilder.addNextIntent(activityIntent);
        }

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent snoozeResponseIntent = new Intent(AppContext.context, notificationActionsListener.class);
        snoozeResponseIntent.putExtra("eventid", event.EventId);
        snoozeResponseIntent.putExtra("responseCode", "snooze");
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(AppContext.context, getIncrementedNotificationId(), snoozeResponseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(AppContext.context)
                        .setSmallIcon(R.drawable.logo_notification)
                        .setContentTitle(event.Name)
                        .setContentText(durationMessage)
                        .setAutoCancel(true)
                        //.setSound(notificationSound)
                        .addAction(R.drawable.ic_timer_black_18dp, "Snooze", snoozePendingIntent);
        //.setContentIntent(viewPendingIntent)

        if (!event.IsMute) {
            mBuilder.setSound(notificationSound);
        }

        //if(!event.getCurrentMember().getUserId().equalsIgnoreCase(event.getInitiatorId()))
        if (!ParticipantService.isCurrentUserInitiator(event.InitiatorId)) {
            Intent declineResponseIntent = new Intent(AppContext.context, notificationActionsListener.class);
            declineResponseIntent.putExtra("eventid", event.EventId);
            declineResponseIntent.putExtra("responseCode", "leave");
            PendingIntent declinePendingIntent =
                    PendingIntent.getBroadcast(AppContext.context, getIncrementedNotificationId(), declineResponseIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            mBuilder.addAction(R.drawable.ic_clear_black_18dp, "Leave", declinePendingIntent);
        } else {
            Intent endResponseIntent = new Intent(AppContext.context, notificationActionsListener.class);
            endResponseIntent.putExtra("eventid", event.EventId);
            endResponseIntent.putExtra("responseCode", "end");
            PendingIntent endPendingIntent =
                    PendingIntent.getBroadcast(AppContext.context, getIncrementedNotificationId(), endResponseIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            mBuilder.addAction(R.drawable.ic_clear_black_18dp, "End Event", endPendingIntent);
        }

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) AppContext.context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.

        event.SnoozeNotificationId = notificationId;
        InternalCaching.saveEventToCache(event);
        mNotificationManager.notify(notificationId, mBuilder.build());
        currentNotificationEventId = event.EventId;
        isNotificActive = true;

    }

    public static void showEventInviteNotification(Event event) {
        //int layoutId = R.layout.notification_event_invitation;
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager =
                (NotificationManager) AppContext.context.getSystemService(ns);
        notificationId = getIncrementedNotificationId();
        event.AcceptNotificationId = notificationId;
        InternalCaching.saveEventToCache(event);

        SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        DateFormat newFormat = new SimpleDateFormat("EEE, dd MMM yyyy  hh:mm a");

        try {


            String parsedDate = newFormat.format(originalformat.parse(event.StartTime));

            /* Add Big View Specific Configuration */
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(AppContext.context);
            String title = "";
            String[] events = new String[2];
            if (EventService.isEventTrackBuddyEventForCurrentUser(event)) {
                title = "Tracking request";
                events[0] = new String(event.InitiatorName + " wants to share his/her location");
                events[1] = new String(parsedDate);

            } else if (EventService.isEventShareMyLocationEventForCurrentUser(event)) {
                title = "Tracking request";
                events[0] = new String(event.InitiatorName + " wants to track your location");
                events[1] = new String(parsedDate);
            } else {
                title = event.Name;
                events[0] = new String(parsedDate);
                events[1] = new String("From " + event.InitiatorName);
            }


            // Sets a title for the Inbox style big view
            inboxStyle.setBigContentTitle(title);
            mBuilder.setContentTitle(title);
            mBuilder.setContentText(events[0]);
            // Moves events into the big view
            for (int i = 0; i < events.length; i++) {
                inboxStyle.addLine(events[i]);

            }

            mBuilder.setStyle(inboxStyle);
            Intent acceptResponseIntent = new Intent(AppContext.context, notificationActionsListener.class);
            acceptResponseIntent.putExtra("eventid", event.EventId);
            acceptResponseIntent.putExtra("responseCode", "accept");
            PendingIntent acceptIntent = PendingIntent.getBroadcast(AppContext.context, getIncrementedNotificationId(), acceptResponseIntent, PendingIntent.FLAG_CANCEL_CURRENT);


            Intent rejectResponseIntent = new Intent(AppContext.context, notificationActionsListener.class);
            rejectResponseIntent.putExtra("eventid", event.EventId);
            rejectResponseIntent.putExtra("responseCode", "reject");
            PendingIntent rejectIntent = PendingIntent.getBroadcast(AppContext.context, getIncrementedNotificationId(), rejectResponseIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            //remoteViews.setOnClickPendingIntent(R.id.btn_reject, rejectIntent);

            // Define that we have the intention of opening MoreInfoNotification
            Intent moreInfoIntent = new Intent(AppContext.context, EventsActivity.class);

            // Used to stack tasks across activites so we go to the proper place when back is clicked
            TaskStackBuilder tStackBuilder = TaskStackBuilder.create(AppContext.context);

            // Add all parents of this activity to the stack
            tStackBuilder.addParentStack(EventsActivity.class);

            // Add our new Intent to the stack
            tStackBuilder.addNextIntent(moreInfoIntent);

            // Define an Intent and an action to perform with it by another application
            // FLAG_UPDATE_CURRENT : If the intent exists keep it but update it if needed
            PendingIntent pendingIntent = tStackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            mBuilder
                    // Set Icon
                    .setSmallIcon(R.drawable.logo_notification)
                    // Dismiss Notification
                    .setAutoCancel(false)
                    //.setSound(notificationSound)
                    .addAction(R.drawable.ic_check_black_24dp, "Accept", acceptIntent)
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.ic_clear_black_24dp, "Decline", rejectIntent);

            if (!event.IsMute) {
                mBuilder.setSound(notificationSound);
            }

            //notificication.bigContentView = remoteViews;

            // Post the notification
            notificationManager.notify(notificationId, mBuilder.build());
            currentNotificationEventId = event.EventId;

            // Used so that we can't stop a notification that has already been stopped
            isNotificActive = true;

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void showEventExtendedNotification(Event event) {
        String notificationMessage = "";
        String title = "";
        if (EventService.isEventTrackBuddyEventForCurrentUser(event)) {
            title = "Tracking extended";
            notificationMessage = event.InitiatorName + " has extended sharing his/her location";

        } else if (EventService.isEventShareMyLocationEventForCurrentUser(event)) {
            title = "Tracking extended";
            notificationMessage = event.InitiatorName + " has extended tracking your location";
        } else {
            notificationMessage = event.InitiatorName + " has extended the event " + event.Name;
        }
        showGenericNotification(event, notificationMessage, title);
    }

    public static void showDestinationChangedNotification(Event event) {
        String notificationMessage = "";
        String notificationTitle = "";
        if (EventService.isEventTrackBuddyEventForCurrentUser(event)
                || EventService.isEventShareMyLocationEventForCurrentUser(event)) {
            notificationTitle = "Tracking destination changed";
            notificationMessage = event.InitiatorName + " has changed the destination ";
        } else {
            notificationMessage = event.InitiatorName + " has changed the meeting place of the event " + event.Name;
        }

        showGenericNotification(event, notificationMessage, notificationTitle);
    }

    public static void showParticipantsUpdatedNotification(Event event) {
        String notificationMessage = "";
        String notificationTitle = "";
        if (EventService.isEventTrackBuddyEventForCurrentUser(event)
                || EventService.isEventShareMyLocationEventForCurrentUser(event)) {
            notificationTitle = "Tracking participants updated";
            notificationMessage = event.InitiatorName + " has added/removed participant(s) ";
        } else {
            notificationMessage = event.InitiatorName + " has added/removed participant(s) of the event " + event.Name;
        }
        showGenericNotification(event, notificationMessage, notificationTitle);
    }

    public static void showRemovedFromEventNotification(Event event) {
        String notificationMessage = "";
        String notificationTitle = "";
        if (EventService.isEventTrackBuddyEventForCurrentUser(event)) {
            notificationTitle = "Removed from Tracking";
            notificationMessage = event.InitiatorName + " has stopped sharing location with you";
        } else if (EventService.isEventShareMyLocationEventForCurrentUser(event)) {
            notificationTitle = "Removed from Tracking";
            notificationMessage = event.InitiatorName + " has stopped tracking your location";
        } else {
            notificationMessage = event.InitiatorName + " has removed you from the event " + event.Name;
        }
        showGenericNotification(event, notificationMessage, notificationTitle);
    }

    public static void showEventDeleteNotification(Event event) {

        String notificationMessage = event.InitiatorName + " has cancelled " + event.Name;
        showGenericNotification(event, notificationMessage, "");
    }

    public static void showEventEndNotification(Event event) {

        String notificationMessage = "";
        String notificationTitle = "";
        if (EventService.isEventTrackBuddyEventForCurrentUser(event)) {
            notificationTitle = "Tracking ended";
            notificationMessage = event.InitiatorName + " has stopped sharing location";
        } else if (EventService.isEventShareMyLocationEventForCurrentUser(event)) {
            notificationTitle = "Tracking ended";
            notificationMessage = event.InitiatorName + " has stopped tracking your location";
        } else {
            notificationMessage = event.InitiatorName + " has ended " + event.Name;
        }

        showGenericNotification(event, notificationMessage, notificationTitle);
    }

    public static void pokeNotification(Context context, String mEventId) {

        Event event = InternalCaching.getEventFromCache(mEventId);
        String notificationMessage = event.InitiatorName + " has poked you. Did you miss to respond to an invitation ?";
        String title = "You have been poked!";
        isPokeNotification = true;
        notificationType = "POKE";
        showGenericNotification(event, notificationMessage, title);
    }

    public static void approachingAlertNotification(Context context, Event mEvent, String notificationMessage) {
        ringAlarm();
        notificationType = "APPROACHING";
        showGenericNotification(mEvent, notificationMessage, "");
    }

    public static void showEventResponseNotification(Context context, Event event, String userName, int eventAcceptanceStateId) {

        String notificationMessage = "";
        String notificationTitle = "";
        if (EventService.isEventTrackBuddyEventForCurrentUser(event)) {
            notificationTitle = "Tracking request";
        } else if (EventService.isEventShareMyLocationEventForCurrentUser(event)) {
            notificationTitle = "Tracking request";
        } else {
            notificationTitle = "";
        }

        if (AcceptanceStatus.getStatus(eventAcceptanceStateId) == AcceptanceStatus.ACCEPTED) {
            if (notificationTitle != "") {
                notificationTitle = notificationTitle + " accepted";
            }
            notificationMessage = userName + " has accepted your request!";
        } else {
            if (notificationTitle != "") {
                notificationTitle = notificationTitle + " rejected";
            }
            notificationMessage = userName + " has rejected your request!";
        }

        showGenericNotification(event, notificationMessage, notificationTitle);
    }

    public static void showEventLeftNotification(Context context, Event event, String userName) {
        String notificationTitle = "Tracking stopped";
        String notificationMessage = "";
        if (EventService.isEventTrackBuddyEventForCurrentUser(event)) {
            notificationMessage = userName + " has stopped sharing location";
        } else if (EventService.isEventShareMyLocationEventForCurrentUser(event)) {
            notificationMessage = userName + " has stopped viewing your location";
        } else {
            notificationTitle = "";
            notificationMessage = userName + " has left " + event.Name;
        }

        showGenericNotification(event, notificationMessage, notificationTitle);
    }

    private static void saveResponse(final AcceptanceStatus status, final String eventid) {
        try {
            //responseInProcessEvents.

            if (responseInProcessEvents.contains(eventid)) {
                Toast.makeText(AppContext.context, AppContext.context.getResources().getString(R.string.message_saveresponse_inprocess), Toast.LENGTH_SHORT).show();
                return;
            }

            responseInProcessEvents.add(eventid);
            String msg = "";
            EventManager.saveUserResponse(status, eventid, new OnActionCompleteListner() {

                @Override
                public void actionComplete(Action action) {
                    responseInProcessEvents.remove(eventid);
                    Intent intent = new Intent(Veranstaltung.EVENT_USER_RESPONSE);
                    LocalBroadcastManager.getInstance(AppContext.context).sendBroadcast(intent);
                    UserMessageHandler.getSuccessMessage(Action.SAVEUSERRESPONSE);
                    Toast.makeText(AppContext.context, UserMessageHandler.getSuccessMessage(Action.SAVEUSERRESPONSE), Toast.LENGTH_SHORT).show();
                }
            }, new OnActionFailedListner() {

                @Override
                public void actionFailed(String msg, Action action) {
                    responseInProcessEvents.remove(eventid);
                    Toast.makeText(AppContext.context, UserMessageHandler.getFailureMessage(Action.SAVEUSERRESPONSE), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception ex) {
            Toast.makeText(AppContext.context, UserMessageHandler.getFailureMessage(Action.SAVEUSERRESPONSE), Toast.LENGTH_SHORT).show();
            responseInProcessEvents.remove(eventid);
        }
    }

    private static void endEvent(final Event event) {
        try {
            EventManager.endEvent(event, new OnActionCompleteListner() {

                @Override
                public void actionComplete(Action action) {
                    Intent intent = new Intent(Veranstaltung.EVENT_ENDED);
                    LocalBroadcastManager.getInstance(AppContext.context).sendBroadcast(intent);
                    String message = AppContext.context.getResources().getString(R.string.message_general_event_end_success);
                    Toast.makeText(AppContext.context, message, Toast.LENGTH_LONG).show();
                    Log.d(TAG, message);
                    UserMessageHandler.getSuccessMessage(Action.ENDEVENT);
                }
            }, new OnActionFailedListner() {
                @Override
                public void actionFailed(String msg, Action action) {
                    UserMessageHandler.getFailureMessage(Action.ENDEVENT);
                }
            });
        } catch (Exception ex) {
            UserMessageHandler.getFailureMessage(Action.SAVEUSERRESPONSE);
            Log.d(TAG, ex.toString());

        }
    }

    public static class notificationActionsListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String eventid = intent.getExtras().getString("eventid");
            String responseCode = intent.getExtras().getString("responseCode");
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            switch (responseCode) {
                case "accept":
                    saveResponse(AcceptanceStatus.ACCEPTED, eventid);
                    break;

                case "reject":
                    saveResponse(AcceptanceStatus.DECLINED, eventid);
                    break;

                case "leave":
                    saveResponse(AcceptanceStatus.DECLINED, eventid);
                    break;

                case "snooze":
                    Event ed = InternalCaching.getEventFromCache(eventid);
                    if (ed != null) {
                        setAlarm("notification", eventid, 10);//reminder interval in minute
                        notificationManager.cancel(ed.SnoozeNotificationId);
                    }
                    break;

                case "end":
                    Event eventD = InternalCaching.getEventFromCache(eventid);
                    endEvent(eventD);

                    break;
                case "approachingAlarmDismiss":
                    // dismiss the approaching alarm
                    if (ringtone.isPlaying()) {
                        ringtone.stop();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static void cancelAllNotifications(Event eventData) {
        try {
            NotificationManager notificationManager = (NotificationManager) AppContext.context.getSystemService(Context.NOTIFICATION_SERVICE);
            // If the notification is still active close it
            notificationManager.cancel(eventData.AcceptNotificationId);
            notificationManager.cancel(eventData.SnoozeNotificationId);
            for (int notficationId : eventData.NotificationIds) {
                notificationManager.cancel(notficationId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cancelNotification(Event eventData) {
        try {
            NotificationManager notificationManager = (NotificationManager) AppContext.context.getSystemService(Context.NOTIFICATION_SERVICE);

            switch (eventData.getCurrentParticipant().getAcceptanceStatus()) {
                case ACCEPTED:
                    notificationManager.cancel(eventData.AcceptNotificationId);
                    break;

                case DECLINED:
                    notificationManager.cancel(eventData.AcceptNotificationId);
                    notificationManager.cancel(eventData.SnoozeNotificationId);
                    for (int notficationId : eventData.NotificationIds) {
                        notificationManager.cancel(notficationId);
                    }
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setAlarm(String reminderType, String eventId, int reminderInterval) {
        //eDetail.getReminderType()
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.MINUTE, reminderInterval);
        Date reminderDate = cal.getTime();
        Intent intentAlarm = new Intent(AppContext.context, EventTrackerAlarmReceiverService.class);
        intentAlarm.putExtra("AlarmType", "Reminder");
        intentAlarm.putExtra("ReminderType", reminderType);
        intentAlarm.putExtra("EventId", eventId);
        AlarmManager alarmManager = (AlarmManager) AppContext.context.getSystemService(Context.ALARM_SERVICE);
        //set the alarm for particular time
        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderDate.getTime(), PendingIntent.getBroadcast(AppContext.context, Constants.ReminderBroadcastId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

    }

    public static void ringAlarm() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }

        ringtone = RingtoneManager.getRingtone(AppContext.context, alert);
        if (!ringtone.isPlaying()) {
            ringtone.play();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (ringtone.isPlaying()) {
                    ringtone.stop();
                }
            }
        }, distanceAlarmDuration);
    }

    public static void showGenericNotification(Event event, String msg, String title) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(AppContext.context);
        String notificationMessage = msg;
        if (title == "") {
            title = event.Name;
        }
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(HomeActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        Intent activityIntent = new Intent(AppContext.context, HomeActivity.class);
        stackBuilder.addNextIntent(activityIntent);


        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(AppContext.context)
                        .setSmallIcon(R.drawable.logo_notification)
                        .setContentTitle(title)
                        .setContentText(notificationMessage)
                        .setAutoCancel(true);


        if (!event.IsMute) {
            if (isPokeNotification) {
                mBuilder.setSound(pokenotificationSound);
            } else {
                mBuilder.setSound(notificationSound);
            }
        }

        switch (notificationType) {
            case "POKE":
                isPokeNotification = false;
                mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                break;
            case "APPROACHING":
                Intent approachingAlarmDismissIntent = new Intent(AppContext.context, notificationActionsListener.class);
                approachingAlarmDismissIntent.putExtra("eventid", event.EventId);
                approachingAlarmDismissIntent.putExtra("responseCode", "approachingAlarmDismiss");
                PendingIntent approachingAlarmPendingIntent =
                        PendingIntent.getBroadcast(AppContext.context, getIncrementedNotificationId(), approachingAlarmDismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                mBuilder.addAction(R.drawable.ic_clear_black_18dp, "Dismiss", approachingAlarmPendingIntent);
                break;
            default:
                break;
        }

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(notificationMessage);
        mBuilder.setStyle(bigTextStyle);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) AppContext.context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        notificationId = getIncrementedNotificationId();
        event.NotificationIds.add(notificationId);
        InternalCaching.saveEventToCache(event);
        mNotificationManager.notify(notificationId, mBuilder.build());
        //currentNotificationEventId = event.EventId;
        isNotificActive = true;
    }

    public static int getIncrementedNotificationId() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(AppContext.context);
        int notificationId = preferences.getInt("notificationId", 0);
        if (notificationId == 0 || notificationId == 99999999) {
            notificationId = 1;
        } else {
            notificationId = notificationId + 1;
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("notificationId", notificationId);
        editor.commit();

        return notificationId;
    }
}


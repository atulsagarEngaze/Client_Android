package com.redtop.engaze.domain.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.redtop.engaze.BaseActivity;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.PreffManager;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.constant.Constants;
import com.redtop.engaze.constant.Veranstaltung;
import com.redtop.engaze.domain.EventDetail;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.entity.EventDetail;
import com.redtop.engaze.interfaces.OnActionCompleteListner;
import com.redtop.engaze.interfaces.OnActionFailedListner;
import com.redtop.engaze.service.EventTrackerAlarmReceiverService;
import com.redtop.engaze.utils.Constants.Action;

@SuppressLint("SimpleDateFormat")
public class EventService {

	@SuppressLint("SimpleDateFormat")
	public static void SortListByStartDate(List<EventDetail> list)
	{				
		final SimpleDateFormat  dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Collections.sort(list, new Comparator<EventDetail>(){
			public int compare(EventDetail ed1, EventDetail ed2) {


				try {
					if (dateformat.parse(ed1.getStartTime()).getTime() > dateformat.parse(ed2.getStartTime()).getTime())
						return 1;
					else if (dateformat.parse(ed1.getStartTime()).getTime() < dateformat.parse(ed2.getStartTime()).getTime())
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

	public static void setEndEventAlarm(Context context,List<EventDetail> eventDetailList){
		for(EventDetail event : eventDetailList){
			setEndEventAlarm(context,event);
		}
	}

	public static void setEndEventAlarm(Context context, EventDetail eDetail){		
		try {		

			DateFormat writeFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss");
			//DateFormat writeFormat = new SimpleDateFormat( "EEE, dd MMM yyyy hh:mm a");
			Date endDate;
			endDate = writeFormat.parse(eDetail.getEndTime());
			Calendar cal = Calendar.getInstance();
			cal.setTime(endDate);		
			Intent intentAlarm = new Intent(context, EventTrackerAlarmReceiverService.class);				
			intentAlarm.putExtra("AlarmType", Veranstaltung.EVENT_OVER);
			intentAlarm.putExtra("EventId", eDetail.getEventId());
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			//set the alarm for particular time
			alarmManager.set(AlarmManager.RTC_WAKEUP,endDate.getTime(), PendingIntent.getBroadcast(context,Constants.EventEndBroadcastId,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}

	public static void setEventReminder(Context context, String eventid){
		EventDetail eDetail = InternalCaching.getEventFromCache(eventid, context);
		setEventReminder(context,eDetail);

	}

	public static void RemoveEndEventAlarm(Context context, String eventid ){
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent intentAlarm = new Intent(context, EventTrackerAlarmReceiverService.class);
		intentAlarm.putExtra("AlarmType", Veranstaltung.EVENT_OVER);
		intentAlarm.putExtra("EventId", eventid);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				Constants.EventStartBroadcastId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.cancel(pendingIntent);

	}

	public static void setEventStarAlarm(Context context, EventDetail eDetail){		
		try {		

			DateFormat writeFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss");
			//DateFormat writeFormat = new SimpleDateFormat( "EEE, dd MMM yyyy hh:mm a");
			Date startDate;
			startDate = writeFormat.parse(eDetail.getStartTime());
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);		
			Intent intentAlarm = new Intent(context, EventTrackerAlarmReceiverService.class);				
			intentAlarm.putExtra("AlarmType", Veranstaltung.EVENT_START);
			intentAlarm.putExtra("EventId", eDetail.getEventId());
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			//set the alarm for particular time
			alarmManager.set(AlarmManager.RTC_WAKEUP,startDate.getTime(), PendingIntent.getBroadcast(context,Constants.EventStartBroadcastId,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}

	public static void setEventReminder(Context context, EventDetail eDetail){		
		try {		

			DateFormat writeFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss");
			//DateFormat writeFormat = new SimpleDateFormat( "EEE, dd MMM yyyy hh:mm a");

			Date startDate = writeFormat.parse(eDetail.getStartTime());			
			Calendar cal = Calendar.getInstance();

			cal.setTime(startDate);			
			cal.add(Calendar.MINUTE, Integer.parseInt(eDetail.getReminderOffset())*-1);					
			Date reminderDate = cal.getTime();
			//if(reminderDate.getTime() > currentDate.getTime()){

			Intent intentAlarm = new Intent(context, EventTrackerAlarmReceiverService.class);				
			intentAlarm.putExtra("AlarmType", Constants.EVENT_REMINDER);
			intentAlarm.putExtra("ReminderType", eDetail.getReminderType());
			intentAlarm.putExtra("EventId", eDetail.getEventId());
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			//set the alarm for particular time
			alarmManager.set(AlarmManager.RTC_WAKEUP,reminderDate.getTime(), PendingIntent.getBroadcast(context,Constants.ReminderBroadcastId,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
			//}

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}

	public static void setTracking(Context context, EventDetail eDetail){		
		try {	

			long trackingAlarmOffset = 0;

			DateFormat writeFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss");

			//tracking start time	
			Date startDate = writeFormat.parse(eDetail.getStartTime());			
			Calendar cal = Calendar.getInstance();

			Date currentDate = cal.getTime();
			cal.setTime(startDate);

			cal.add(Calendar.MINUTE, Integer.parseInt(eDetail.getTrackingStartOffset())*-1);
			Date trackingStartDate = cal.getTime();


			if(trackingStartDate.getTime()< currentDate.getTime()){
				trackingAlarmOffset = currentDate.getTime()+5000;
			}
			else{

				trackingAlarmOffset = trackingStartDate.getTime();
			}
			Intent intentAlarm = new Intent(context, EventTrackerAlarmReceiverService.class);			
			intentAlarm.putExtra("AlarmType", Veranstaltung.TRACKING_STARTED);
			intentAlarm.putExtra("EventId", eDetail.getEventId());
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			//set the alarm for particular time
			alarmManager.set(AlarmManager.RTC_WAKEUP,trackingAlarmOffset, PendingIntent.getBroadcast(context,Constants.TrackingStartBroadcastId,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));						

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}

	public static void upDateEventStatus(List<EventDetail> eventDetailList) {
		try {
			SimpleDateFormat  originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");	 
			Date startDate = null;
			Calendar cal = null;
			for (EventDetail ed :  eventDetailList){
				cal = Calendar.getInstance();
				startDate =  originalformat.parse(ed.getStartTime());
				cal.setTime(startDate);
				cal.add(Calendar.MINUTE, Integer.parseInt(ed.getTrackingStartOffset())*-1);
				Date currentDate =  Calendar.getInstance().getTime();
				if(cal.getTime().getTime() - currentDate.getTime()<0){
					ed.setState(Constants.TRACKING_ON);
				}
				else
				{
					ed.setState(Constants.EVENT_OPEN);
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void RemovePastEvents(Context context,
			List<EventDetail> eventDetailList) {
		List<EventDetail> tobeRemoved = new  ArrayList<EventDetail>();
		for(EventDetail event : eventDetailList){
			if(isEventPast(context,event)){
				tobeRemoved.add(event);
			}
		}
		eventDetailList.removeAll(tobeRemoved);
	}

	public static Boolean isEventPast(Context context, EventDetail ev)
	{

		try {
			Calendar cal = Calendar.getInstance(); 
			Date currentDate = cal.getTime();
			DateFormat writeFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss");
			//using this logic as end date is not coming properly
			cal.setTime(writeFormat.parse(ev.getStartTime()));
			cal.add(Calendar.MINUTE, Integer.parseInt(ev.getDuration()));
			Date endDate = cal.getTime();				
			if( currentDate.getTime() > endDate.getTime()){
				return true;
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static long getTimeToFinish(String eventEndTime, String format)
	{

		DateFormat writeFormat = new SimpleDateFormat( format);			
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

	public static long pendingEventTime(String eventEndTime)
	{

		DateFormat writeFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss");			
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

	public static void pokeParticipant(final String userId, String userName, final String eventId, final Context context){
		try {
			String lastPokedTime = PreffManager.getPref(userId, context);
			if(lastPokedTime != null){
				SimpleDateFormat  originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Calendar lastCal = Calendar.getInstance();
				Date lastpokeDate = originalformat.parse(lastPokedTime);				
				lastCal.setTime(lastpokeDate);		
				long diff = (Calendar.getInstance().getTimeInMillis()- lastCal.getTimeInMillis())/60000;
				long pendingfrPoke = Constants.POKE_INTERVAL- diff;
				if(diff>= Constants.POKE_INTERVAL){				
					pokeAlert(userId,userName, eventId, context);
				}else {
					Toast.makeText(context,								
							context.getResources().getString(R.string.message_runningEvent_pokeInterval)+ pendingfrPoke + " minutes.",
							Toast.LENGTH_LONG).show();
					((BaseActivity)context).actionCancelled(Action.POKEPARTICIPANT);
				}					
			}else {
				pokeAlert(userId,userName, eventId, context);
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void pokeAlert(final String userId, String userName, final String eventId, final Context context) {
		AlertDialog.Builder adb = null;
		adb = new AlertDialog.Builder(context);				

		adb.setTitle("Poke");
		adb.setMessage("Do you want to poke " + userName + "?" +"\n"+ "You can poke again only after 15 minutes.");					
		adb.setIcon(android.R.drawable.ic_dialog_alert);

		adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {				
				//Call Poke API				
				pokeParticipants(userId, eventId, context);
			} });

		adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {							
				dialog.dismiss();						
				((BaseActivity)context).actionCancelled(Action.POKEPARTICIPANT);
			} });
		adb.show();
	}

	private  static void pokeParticipants(final String userId, String eventId, final Context context) {
		JSONObject jobj = new JSONObject();						
		String[] userList = {userId};		
		JSONArray mJSONArray = new JSONArray(Arrays.asList(userList));		
		EventDetail ed = InternalCaching.getEventFromCache(eventId, context);
		try {
			((BaseActivity)context).showProgressBar(context.getString(R.string.message_general_progressDialog));
			jobj.put("RequestorId", AppContext.getInstance().loginId);
			jobj.put("RequestorName", AppUtility.getPref(Constants.LOGIN_NAME, context));
			jobj.put("UserIdsForRemind", mJSONArray);
			jobj.put("EventName", ed.getName());
			jobj.put("EventId", ed.getEventId());

			EventManager.pokeParticipants(context,jobj, new OnActionCompleteListner() {

				@Override
				public void actionComplete(Action action) {
					SimpleDateFormat  originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");						 
					Date currentdate = Calendar.getInstance().getTime();
					String currentTimestamp = originalformat.format(currentdate);
					PreffManager.setPref(userId, currentTimestamp, context);
					((BaseActivity)context).actionComplete(Action.POKEPARTICIPANT);
				}
			}, new OnActionFailedListner() {

				@Override
				public void actionFailed(String msg, Action action) {					
					action = Action.POKEPARTICIPANT;
					((BaseActivity)context).actionFailed(msg, action);
				}
			});

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			((BaseActivity)context).actionFailed(null, Action.POKEPARTICIPANT);
		}
	}

	public static void removeLocationServiceCheckAlarm(Context context){
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent intentAlarm = new Intent(context, EventTrackerAlarmReceiverService.class);
		intentAlarm.putExtra("AlarmType", Constants.CHECK_LOCATION_SERVICE);			


		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				Constants.EventStartBroadcastId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.cancel(pendingIntent);
	}

	public static void setLocationServiceCheckAlarm(Context context){		
		try {			

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, 5);

			Intent intentAlarm = new Intent(context, EventTrackerAlarmReceiverService.class);				
			intentAlarm.putExtra("AlarmType", Constants.CHECK_LOCATION_SERVICE);			

			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);			
			//remove existing alarm
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					Constants.EventStartBroadcastId, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

			alarmManager.cancel(pendingIntent);

			//set new  alarm 
			alarmManager.set(AlarmManager.RTC_WAKEUP,cal.getTime().getTime(), PendingIntent.getBroadcast(context,Constants.LocationServiceCheckBroadcastId,  intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}

	public static boolean IsEventTrackBuddyEventForCurrentuser(EventDetail mEvent) {
		int eventTypeId = Integer.parseInt(mEvent.getEventTypeId());
		boolean isCurrentUserInitiator = EventParticipant.isCurrentUserInitiator(mEvent.getInitiatorId());

		if((isCurrentUserInitiator && eventTypeId==200) ||
				(!isCurrentUserInitiator && eventTypeId==100)){
			return true;
		}
		return false;
	}

	public static boolean IsEventShareMyLocationEventForCurrentuser(EventDetail mEvent) {
		int eventTypeId = Integer.parseInt(mEvent.getEventTypeId());
		boolean isCurrentUserInitiator = EventParticipant.isCurrentUserInitiator(mEvent.getInitiatorId());

		if((isCurrentUserInitiator && eventTypeId==100) ||
				(!isCurrentUserInitiator && eventTypeId==200)){
			return true;
		}
		return false;
	}
}
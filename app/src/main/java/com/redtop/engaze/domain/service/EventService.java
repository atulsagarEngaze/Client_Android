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

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.AppService;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.EventState;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.domain.EventDetail;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.service.EventTrackerAlarmReceiverService;

import org.json.JSONArray;
import org.json.JSONObject;

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
			intentAlarm.putExtra("AlarmType", Veranstaltung.EVENT_REMINDER);
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
					ed.setState(EventState.TRACKING_ON);
				}
				else
				{
					ed.setState(EventState.EVENT_OPEN);
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

	public static boolean isEventTrackBuddyEventForCurrentuser(EventDetail mEvent) {
		int eventTypeId = Integer.parseInt(mEvent.getEventTypeId());
		boolean isCurrentUserInitiator = EventParticipant.isCurrentUserInitiator(mEvent.getInitiatorId());

		if((isCurrentUserInitiator && eventTypeId==200) ||
				(!isCurrentUserInitiator && eventTypeId==100)){
			return true;
		}
		return false;
	}

	public static boolean isEventShareMyLocationEventForCurrentuser(EventDetail mEvent) {
		int eventTypeId = Integer.parseInt(mEvent.getEventTypeId());
		boolean isCurrentUserInitiator = EventParticipant.isCurrentUserInitiator(mEvent.getInitiatorId());

		if((isCurrentUserInitiator && eventTypeId==100) ||
				(!isCurrentUserInitiator && eventTypeId==200)){
			return true;
		}
		return false;
	}

	public static Boolean isAnyEventInState(Context context, String state, Boolean checkOnlyWhenEventAccepted){
		List<EventDetail> events = InternalCaching.getEventListFromCache(context);
		if(events==null){
			return false;
		}
		for(EventDetail ed : events){
			if(ed.getState().equals(state)){
				if(checkOnlyWhenEventAccepted)
				{

					if(ed.getCurrentParticipant().getAcceptanceStatus()== AcceptanceStatus.ACCEPTED
					){
						return true;
					}
				}
				else
				{
					return true;
				}
			}
		}
		return false;

	}

	public static Boolean shouldShareLocation(Context context){
		List<EventDetail> events = InternalCaching.getEventListFromCache(context);
		List<EventDetail> trackingEvents = InternalCaching. getTrackEventListFromCache(context);
		if(events==null){
			return false;
		}
		for(EventDetail ed : events){
			if(ed.getCurrentParticipant().getAcceptanceStatus()==AcceptanceStatus.ACCEPTED
					&& ed.getState().equals(EventState.TRACKING_ON)
			){
				return true;
			}
		}
		if(trackingEvents==null){
			return false;
		}
		for(EventDetail ed : trackingEvents){
			if(isEventShareMyLocationEventForCurrentuser(ed)){
				return true;
			}
		}
		return false;
	}

	public static List<EventDetail> parseEventDetailList(JSONArray jsonStr, Context context) {
		JSONArray eventDetailJsonArray = jsonStr;
		List<EventDetail> eventDetailList = new ArrayList<EventDetail>();
		String loginUser = AppContext.getInstance().loginId;
		try {
			for (int i = 0; i < eventDetailJsonArray.length(); i++) {
				JSONObject c = eventDetailJsonArray.getJSONObject(i);
				EventDetail dt = new EventDetail(
						ParticipantService.parseMemberList(context, c.getJSONArray("UserList")),
						AppService.convertNullToEmptyString(c.getString("EventId")),
						AppService.convertNullToEmptyString(c.getString("Name")),
						AppService.convertNullToEmptyString(c.getString("EventTypeId")),
						AppService.convertNullToEmptyString(c.getString("Description")),
						AppService.convertNullToEmptyString(DateUtil.convertUtcToLocalDateTime(c.getString("StartTime"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))),
						AppService.convertNullToEmptyString(DateUtil.convertUtcToLocalDateTime(c.getString("EndTime"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))),
						AppService.convertNullToEmptyString(c.getString("Duration")),
						AppService.convertNullToEmptyString(c.getString("InitiatorId")),
						AppService.convertNullToEmptyString(c.getString("InitiatorName")),
						AppService.convertNullToEmptyString(c.getString("EventStateId")),
						AppService.convertNullToEmptyString(c.getString("TrackingStateId")),
						c.getString("DestinationLatitude"),
						c.getString("DestinationLongitude"),
						c.getString("DestinationName"),
						c.getString("DestinationAddress"),
						c.getString("IsTrackingRequired"),
						c.getString("ReminderOffset"),
						//AppUtility.checkNull(c.getString("IsTrackingRequired")),
						//AppUtility.checkNull(c.getString("ReminderOffset")),
						//c.getString("ReminderType"),
						"notification",
						c.getString("TrackingStartOffset"),
						c.getString("IsQuickEvent"));
				dt.setCurrentParticipant(dt.getMember(loginUser));
				dt.setIsRecurrence(c.getString("IsRecurring"));
				if(c.getString("IsRecurring").equals("true")){
					dt.setNumberOfOccurencesLeft(c.getString("RecurrenceRemaining"));
					dt.setNumberOfOccurences(c.getString("RecurrenceCount"));
					dt.setFrequencyOfOcuurence(c.getString("RecurrenceFrequency"));
					dt.setRecurrenceType(c.getString("RecurrenceFrequencyTypeId"));

					if(c.getString("RecurrenceFrequencyTypeId").equals("2")){

						ArrayList<String>strRecurrencedays = new ArrayList<String>(Arrays.asList(c.getString("RecurrenceDaysOfWeek")
								.split(",")));
						ArrayList<Integer>recurrencedays = new ArrayList<Integer>();
						for (String strDay : strRecurrencedays){
							recurrencedays.add(Integer.parseInt(strDay));
						}
						dt.setRecurrenceDays(recurrencedays);
					}
					dt.setRecurrenceActualStartTime(DateUtil.convertUtcToLocalDateTime(c.getString("StartTime"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")));
				}

				eventDetailList.add(dt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return eventDetailList;
	}
}
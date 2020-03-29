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

import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.R;
import com.redtop.engaze.common.AppService;
import com.redtop.engaze.common.ContactAndGroupListManager;
import com.redtop.engaze.common.PreffManager;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.constant.Constants;
import com.redtop.engaze.constant.Veranstaltung;
import com.redtop.engaze.domain.EventDetail;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.TrackLocationMember;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.manager.EventNotificationManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


@SuppressLint("SimpleDateFormat")
public class EventManager {
	private final static String TAG = EventManager.class.getName();

	public static List<EventDetail> getRunningEventList(Context context){
		List<EventDetail> list = InternalCaching.getEventListFromCache(context);
		//list = removePastEvents(context, list);
		List<EventDetail> runningList =  new ArrayList<EventDetail>();
		if(list!=null){			
			for(EventDetail e : list){
				if(e.getCurrentParticipant().getAcceptanceStatus()== AcceptanceStatus.ACCEPTED
						&& e.getState().equals(Constants.TRACKING_ON)){
					runningList.add(e);
				}
			}
			EventService.SortListByStartDate(runningList);
		}
		return runningList;
	}

	public static List<EventDetail> getPendingEventList(Context context){
		List<EventDetail> pendingList =  new ArrayList<EventDetail>();
		List<EventDetail> list = InternalCaching.getEventListFromCache(context);
		list.addAll(InternalCaching.getTrackEventListFromCache(context));
		if(list!=null){
			//list = removePastEvents(context, list);
			if(list!=null){			
				for(EventDetail e : list){
					if(e.getCurrentParticipant().getAcceptanceStatus()!= AcceptanceStatus.ACCEPTED &&
							e.getCurrentParticipant().getAcceptanceStatus()!= AcceptanceStatus.DECLINED){
						pendingList.add(e);
					}
				}
				EventService.SortListByStartDate(pendingList);
			}			
		}
		return pendingList;
	}

	public static void startEvent(Context context, String eventid){
		EventDetail event = InternalCaching.getEventFromCache(eventid, context);
		if(event==null){

			String message = context.getResources().getString(R.string.message_general_event_null_error);
			Toast.makeText(context,message, Toast.LENGTH_SHORT).show();
			Log.d(TAG, message );
			return;
		}

		event.setState(Constants.TRACKING_ON);
		InternalCaching.saveEventToCache(event, context);
		EventTrackerLocationService.peroformSartStop(context);

	}

	public static void eventTrackingStart(Context context, String eventid){
		EventDetail event = InternalCaching.getEventFromCache(eventid, context);
		if(event==null){
			String message = context.getResources().getString(R.string.message_general_event_null_error);
			Toast.makeText(context,message, Toast.LENGTH_SHORT).show();
			Log.d(TAG, message );
			return;
		}

		event.setState(Constants.TRACKING_ON);
		InternalCaching.saveEventToCache(event, context);
		EventTrackerLocationService.peroformSartStop(context);

	}

	public static void eventOver(Context context, String eventid){
		EventDetail event = InternalCaching.getEventFromCache(eventid, context);
		if(event==null){
			String message = context.getResources().getString(R.string.message_general_event_null_error);
			Toast.makeText(context,message, Toast.LENGTH_SHORT).show();
			Log.d(TAG, message );
			return;
		}	
		event.setState(Constants.EVENT_END);	
		EventNotificationManager.cancelAllNotifications(context, event);
		EventTrackerLocationService.peroformSartStop(context);
		InternalCaching.removeEventFromCache(eventid, context);
		checkForReccurrence(context, event);		
	}

	public static void saveEvent(final Context context, final JSONObject mEventJobj,final Boolean isMeetNow, final Reminder reminder, final OnEventSaveCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure){

		if(!AppService.isNetworkAvailable(context))
		{
			String message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.SAVEEVENT);
			return ;
		}
		APICaller.CreateEvent(context, mEventJobj, new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {
				Log.d(TAG, "EventResponse:" + response.toString());

				try {								
					String Status = (String)response.getString("Status");

					if (Status == "true")
					{
						EventDetail eventDetailData =  new JsonParser().parseEventDetailList(response.getJSONArray("ListOfEvents"), context).get(0);						
						int eventTypeId = Integer.parseInt(eventDetailData.getEventTypeId());
						EventService.setEndEventAlarm(context, eventDetailData);
						if(isMeetNow){
							eventDetailData.setState(Constants.TRACKING_ON);
							eventDetailData.isQuickEvent="true";
						}
						else if(eventTypeId==100 || eventTypeId==200){
						}
						else{
							EventService.setTracking(context, eventDetailData);
							EventService.setEventStarAlarm(context, eventDetailData);
							if(reminder!=null)
							{
								EventService.setEventReminder(context, eventDetailData);

							}
							EventService.setEventReminder(context, eventDetailData);
							eventDetailData.setState(Constants.EVENT_OPEN);
							eventDetailData.isQuickEvent="false";
						}					
						EventNotificationManager.cancelNotification(context, eventDetailData);
						InternalCaching.saveEventToCache(eventDetailData, context);
						EventTrackerLocationService.peroformSartStop(context);
						listnerOnSuccess.eventSaveComplete(eventDetailData);
					}
					else{
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

	public static void saveUserResponse(final AcceptanceStatus userAcceptanceResponse, final Context context, final String eventid,  final OnActionCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure){

		String message ="";
		if(!AppUtility.isNetworkAvailable(context))
		{
			message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.SAVEUSERRESPONSE);
			return ;
		}
		final EventDetail event = InternalCaching.getEventFromCache(eventid, context);
		if(event==null){

			message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.SAVEUSERRESPONSE);
			return ;
		}


		APICaller.saveUserResponse(userAcceptanceResponse,context, eventid, new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {
				Log.d(TAG, "EventResponse:" + response.toString());

				try {								
					String Status = (String)response.getString("Status");

					if (Status == "true")
					{

						if(userAcceptanceResponse == AcceptanceStatus.ACCEPTED){
							event.getCurrentMember().
							setAcceptanceStatus(Constants.AcceptanceStatus.ACCEPTED);
							SimpleDateFormat  originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

							Date startDate = originalformat.parse(event.getStartTime());
							Date currentDate =  Calendar.getInstance().getTime();
							if(currentDate.getTime() >= startDate.getTime())
							{ //quick event
								event.setState(Constants.TRACKING_ON);
							}
							else
							{
								EventService.setEventStarAlarm(context, event);
								EventService.setEventReminder(context, event);
								EventService.setTracking(context, event);
							}
						}

						else{
							event.getCurrentParticipant().
							setAcceptanceStatus(AcceptanceStatus.DECLINED);
						}
						EventNotificationManager.cancelNotification(context, event);
						InternalCaching.saveEventToCache(event, context);
						EventTrackerLocationService.peroformSartStop(context);											
						listnerOnSuccess.actionComplete(Action.SAVEUSERRESPONSE);
					}
					else{
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
				if(response!=null){
					Log.d(TAG, "EventResponse:" + response.toString());
				}
				listnerOnFailure.actionFailed(null, Action.SAVEUSERRESPONSE);				
			}
		});
	}

	public static void getEventDataFromServer(final Context context, final String eventid,final OnActionCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure){
		String message ="";
		if(!AppService.isNetworkAvailable(context))
		{
			message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.GETEVENTDATAFROMSERVER);
			return ;
		}
		APICaller.getEventDetail(context, eventid, new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {
				Log.d(TAG, "EventResponse:" + response.toString());				
				try{

					String Status = (String)response.getString("Status");
					if (Status == "true")
					{				
						List<EventDetail> eventDetailList =  new JsonParser().parseEventDetailList(response.getJSONArray("ListOfEvents"), context);	
						EventDetail event = eventDetailList.get(0);
						if(EventService.IsEventShareMyLocationEventForCurrentuser(event)){
							event.setState(Constants.TRACKING_ON);
						}
						InternalCaching.saveEventToCache(event, context);
						EventService.setEndEventAlarm(context,event);
						EventNotificationManager.showEventInviteNotification(context, event);
						listnerOnSuccess.actionComplete(Action.GETEVENTDATAFROMSERVER);
					}
					else
					{						
						listnerOnFailure.actionFailed(null, Action.GETEVENTDATAFROMSERVER);									
					}
				}
				catch(Exception ex){
					Log.d(TAG, ex.toString());
					ex.printStackTrace();
					listnerOnFailure.actionFailed(null, Action.GETEVENTDATAFROMSERVER);			
				}

			}
		}, new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {
				if(response!=null){
					Log.d(TAG, "EventResponse:" + response.toString());
				}
				listnerOnFailure.actionFailed(null, Action.GETEVENTDATAFROMSERVER);

			}
		});
	}

	public static void leaveEvent(final Context context, final EventDetail event,  final OnActionCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure){

		String message ="";
		if(!AppService.isNetworkAvailable(context))
		{
			message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.LEAVEEVENT);
			return ;
		}
		
		if(event==null){

			message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.LEAVEEVENT);
			return ;
		}
		
		final String eventid = event.getEventId();

		APICaller.leaveEvent(context, eventid, new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {
				Log.d(TAG, "EventResponse:" + response.toString());

				try {								
					String Status = (String)response.getString("Status");

					if (Status == "true")
					{
						event.getCurrentParticipant().
						setAcceptanceStatus(AcceptanceStatus.DECLINED);

						EventNotificationManager.cancelNotification(context, event);
						InternalCaching.saveEventToCache(event, context);
						EventTrackerLocationService.peroformSartStop(context);											
						listnerOnSuccess.actionComplete(Action.LEAVEEVENT);
					}
					else{
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
				if(response!=null){
					Log.d(TAG, "EventResponse:" + response.toString());
				}
				listnerOnFailure.actionFailed(null, Action.LEAVEEVENT);				
			}
		});
	}

	public static void endEvent(final Context context, final EventDetail event,  final OnActionCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure){

		String message ="";
		if(!AppService.isNetworkAvailable(context))
		{
			message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.ENDEVENT);
			return ;
		}
		
		if(event==null){			
			message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.ENDEVENT);
			return ;
		}
		final String eventid = event.getEventId();

		APICaller.endEvent(context, event.getEventId(), new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {	
				Log.d(TAG, "EventResponse:" + response.toString());				
				try{

					String Status = (String)response.getString("Status");
					if (Status == "true")
					{					
						EventNotificationManager.cancelAllNotifications(context, event);
						EventService.RemoveEndEventAlarm(context, eventid);
						EventTrackerLocationService.peroformSartStop(context);				
						InternalCaching.removeEventFromCache(eventid, context);
						
						// Remove the event related items from preferences
						PreffManager.removePref(eventid, context);
						for (EventParticipant i : event.getParticipants()) {
							PreffManager.removePref(i.getUserId(), context);
						}

						checkForReccurrence(context, event);
					}
					else
					{						
						listnerOnFailure.actionFailed(null, Action.ENDEVENT);									
					}
					
					listnerOnSuccess.actionComplete(Action.ENDEVENT);
				}
				catch(Exception ex){
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

	public static void deleteEvent(final Context context, final EventDetail event, final OnActionCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure) {
		String message ="";
		if(!AppService.isNetworkAvailable(context))
		{
			message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.DELETEEVENT);
			return ;
		}		
		if(event==null){			
			message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.DELETEEVENT);
			return ;
		}
		final String eventid =  event.getEventId();

		APICaller.endEvent(context, event.getEventId(), new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {	
				Log.d(TAG, "EventResponse:" + response.toString());				
				try{

					String Status = (String)response.getString("Status");
					if (Status == "true")
					{
						EventService.RemoveEndEventAlarm(context, eventid);
						InternalCaching.removeEventFromCache(eventid, context);	
						//LocalBroadCast
						Intent intent = new Intent(Constants.EVENT_DELETE_BY_INITIATOR);
						intent.putExtra("eventId", event.getEventId());
						LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
						listnerOnSuccess.actionComplete(Action.DELETEEVENT);
					}
					else
					{						
						listnerOnFailure.actionFailed(null, Action.DELETEEVENT);									
					}
				}
				catch(Exception ex){
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

	public static void addRemoveParticipants(JSONObject addRemoveContactsJSON, final Context context, final OnActionCompleteListner listenerOnSuccess, final OnActionFailedListner listenerOnFailure) {
		String message ="";
		if(!AppService.isNetworkAvailable(context))
		{
			message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
			Log.d(TAG, message);
			listenerOnFailure.actionFailed(message, Action.ADDREMOVEPARTICIPANTS);
			return ;
		}

		APICaller.addRemoveParticipants(addRemoveContactsJSON, context, new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {	
				Log.d(TAG, "EventResponse:" + response.toString());				
				try{

					String Status = (String)response.getString("Status");
					if (Status == "true")
					{
						List<EventDetail> eventDetailList =  new JsonParser().parseEventDetailList(response.getJSONArray("ListOfEvents"), context);	
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

	public static void changeDestination(final EventPlace destinationPlace, final Context context,final EventDetail event, final OnActionCompleteListner listenerOnSuccess, final OnActionFailedListner listnerOnFailure) {
		String message ="";
		if(!AppService.isNetworkAvailable(context))
		{
			message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.CHANGEDESTINATION);
			return ;
		}

		if(event==null){			
			message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.CHANGEDESTINATION);
			return ;
		}
		final String eventId = event.getEventId();

		APICaller.changeDestination(destinationPlace, context, eventId, new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {								
				try {
					String Status = (String)response.getString("Status");
					if (Status == "true")
					{
						event.setDestinationLatitude(String.valueOf(destinationPlace.getLatitude()));
						event.setDestinationLongitude(String.valueOf(destinationPlace.getLongitude()));
						event.setDestinationName(destinationPlace.getName());
						event.setDestinationAddress(destinationPlace.getAddress());
						InternalCaching.saveEventToCache(event, context);
						listenerOnSuccess.actionComplete(Action.CHANGEDESTINATION);
					}
					else
					{						
						listnerOnFailure.actionFailed(null, Action.CHANGEDESTINATION);
					}
				} catch(Exception ex){
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
		String message ="";
		if(!AppService.isNetworkAvailable(context))
		{
			message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.EXTENDEVENTENDTIME);
			return ;
		}

		if(event==null){			
			message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.EXTENDEVENTENDTIME);
			return ;
		}
		final String eventid = event.getEventId();

		APICaller.extendEventEndTime(i,context, eventid, new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {								
				try {
					String Status = (String)response.getString("Status");
					if (Status == "true")
					{
						DateFormat writeFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss");

						Date endTime = writeFormat.parse(event.getEndTime());			
						Calendar cal = Calendar.getInstance();

						cal.setTime(endTime);
						cal.add(Calendar.MINUTE, i);

						String newEndTime = writeFormat.format(cal.getTime());
						event.setEndTime(newEndTime);

						EventService.RemoveEndEventAlarm(context, eventid);
						EventService.setEndEventAlarm(context, event);
						InternalCaching.saveEventToCache(event, context);	
						listenerOnSuccess.actionComplete(Action.EXTENDEVENTENDTIME);
					}
					else
					{						
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

	public static void updateEventWithParticipantResponse(Context context, String eventid, String userId, String userName,int eventAcceptanceStateId, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure){
		EventDetail event = InternalCaching.getEventFromCache(eventid, context);
		if(event==null){

			String message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.UPDATEEVENTWITHPARTICIPANTRESPONSE);
			return ;
		}
		try{
			for( EventParticipant em :  event.getParticipants())
			{
				if(em.getUserId().toLowerCase().equals(userId.toLowerCase())){							
					em.setAcceptanceStatus(AcceptanceStatus.getStatus(eventAcceptanceStateId));
				}
			}
			InternalCaching.saveEventToCache(event, context);
			if(AppUtility.isNotifyUser(event) && EventParticipant.isCurrentUserInitiator(event.getInitiatorId())){
				EventNotificationManager.showEventResponseNotification(context, event,userName, eventAcceptanceStateId );
			}
			listnerOnSuccess.actionComplete(Action.UPDATEEVENTWITHPARTICIPANTRESPONSE);
		}

		catch(Exception ex){
			Log.d(TAG, ex.toString());
			ex.printStackTrace();
			listnerOnFailure.actionFailed(null, Action.UPDATEEVENTWITHPARTICIPANTRESPONSE);
		}

	}

	public static void updateEventWithParticipantLeft(Context context, String eventid, String userId, String userName,OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure){
		EventDetail event = InternalCaching.getEventFromCache(eventid, context);
		if(event==null){

			String message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.UPDATEEVENTWITHPARTICIPANTLEFT);
			return ;
		}
		try{
			for( EventParticipant em :  event.getParticipants())
			{
				if(em.getUserId().toLowerCase().equals(userId.toLowerCase())){							
					em.setAcceptanceStatus(AcceptanceStatus.DECLINED);
				}
			}
			InternalCaching.saveEventToCache(event, context);
			if(AppUtility.isNotifyUser(event)){
				EventNotificationManager.showEventLeftNotification(context, event,userName );
			}
			listnerOnSuccess.actionComplete(Action.UPDATEEVENTWITHPARTICIPANTLEFT);
		}

		catch(Exception ex){
			Log.d(TAG, ex.toString());
			ex.printStackTrace();
			listnerOnFailure.actionFailed(null, Action.UPDATEEVENTWITHPARTICIPANTLEFT);
		}

	}

	public static void eventEndedByInitiator(final Context context, final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure){
		EventDetail event = InternalCaching.getEventFromCache(eventid, context);
		if(event==null){

			String message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.EVENTEXTENDEDBYINITIATOR);
			return ;
		}
		try{
			event.setState(Constants.EVENT_END);			
			// Remove Event End Alarm and the entire event from cache
			EventService.RemoveEndEventAlarm(context, eventid);
			EventNotificationManager.cancelAllNotifications(context, event);
			if(AppUtility.isNotifyUser(event)){
				EventNotificationManager.showEventEndNotification(context, event);
			}
			EventTrackerLocationService.peroformSartStop(context);			
			InternalCaching.removeEventFromCache(eventid, context);
			listnerOnSuccess.actionComplete(Action.EVENTEXTENDEDBYINITIATOR);
			checkForReccurrence(context, event);
		}
		catch(Exception ex){
			Log.d(TAG, ex.toString());
			ex.printStackTrace();
			listnerOnFailure.actionFailed(null, Action.EVENTEXTENDEDBYINITIATOR);
		}
	}

	public static void eventExtendedByInitiator(final Context context, final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure){
		EventDetail event = InternalCaching.getEventFromCache(eventid, context);
		if(event==null){

			String message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.EVENTEXTENDEDBYINITIATOR);
			return ;
		}
		try{
			if(AppUtility.isNotifyUser(event)){
				EventNotificationManager.showEventExtendedNotification(context, event);	
			}
			//Remove old End Event Alarm and set new one
			EventService.RemoveEndEventAlarm(context, eventid);
			EventService.setEndEventAlarm(context, event);
			EventTrackerLocationService.peroformSartStop(context);

			listnerOnSuccess.actionComplete(Action.EVENTEXTENDEDBYINITIATOR);
		}
		catch(Exception ex){
			Log.d(TAG, ex.toString());
			ex.printStackTrace();
			listnerOnFailure.actionFailed(null, Action.EVENTEXTENDEDBYINITIATOR);
		}
	}

	public static void participantsUpdatedByInitiator(final Context context, final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure){
		EventDetail event = InternalCaching.getEventFromCache(eventid, context);
		if(event==null){

			String message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.PARTICIPANTSUPDATEDBYINITIATOR);
			return ;
		}
		try{
			if(AppUtility.isNotifyUser(event)){
				EventNotificationManager.showParticipantsUpdatedNotification(context, event);
			}
			listnerOnSuccess.actionComplete(Action.PARTICIPANTSUPDATEDBYINITIATOR);
		}
		catch(Exception ex){
			Log.d(TAG, ex.toString());
			ex.printStackTrace();
			listnerOnFailure.actionFailed(null, Action.PARTICIPANTSUPDATEDBYINITIATOR);
		}
	}

	public static void eventDeletedByInitiator(final Context context, final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure){
		EventDetail event = InternalCaching.getEventFromCache(eventid, context);
		if(event==null){

			String message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.EVENTDELETEDBYINITIATOR);
			return ;
		}
		try{
			EventNotificationManager.cancelAllNotifications(context, event);
			if(AppUtility.isNotifyUser(event)){
				EventNotificationManager.showEventDeleteNotification(context, event);
			}
			EventService.RemoveEndEventAlarm(context, eventid);
			EventTrackerLocationService.peroformSartStop(context);
			InternalCaching.removeEventFromCache(eventid, context);
			listnerOnSuccess.actionComplete(Action.EVENTDELETEDBYINITIATOR);
		}
		catch(Exception ex){
			Log.d(TAG, ex.toString());
			ex.printStackTrace();
			listnerOnFailure.actionFailed(null, Action.EVENTDELETEDBYINITIATOR);
		}
	}

	public static void eventDestinationChangedByInitiator(final Context context, final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure){
		EventDetail event = InternalCaching.getEventFromCache(eventid, context);
		if(event==null){

			String message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.EVENTDESTINATIONCHANGEDBYINITIATOR);
			return ;
		}
		try{
			if(AppUtility.isNotifyUser(event)){
				EventNotificationManager.showDestinationChangedNotification(context, event);
			}
			listnerOnSuccess.actionComplete(Action.EVENTDESTINATIONCHANGEDBYINITIATOR);
		}
		catch(Exception ex){
			Log.d(TAG, ex.toString());
			ex.printStackTrace();
			listnerOnFailure.actionFailed(null, Action.EVENTDESTINATIONCHANGEDBYINITIATOR);
		}
	}

	public static void currentparticipantRemovedByInitiator(final Context context, final String eventid, OnActionCompleteListner listnerOnSuccess, OnActionFailedListner listnerOnFailure){
		EventDetail event = InternalCaching.getEventFromCache(eventid, context);
		if(event==null){

			String message = context.getResources().getString(R.string.message_general_event_null_error);
			Log.d(TAG, message);
			listnerOnFailure.actionFailed(message, Action.CURRENTPARTICIPANTREMOVEDBYINITIATOR);
			return ;
		}
		try{
			EventNotificationManager.cancelNotification(context, event);
			if(AppUtility.isNotifyUser(event)){
				EventNotificationManager.showRemovedFromEventNotification(context, event);
			}
			EventService.RemoveEndEventAlarm(context, eventid);
			EventTrackerLocationService.peroformSartStop(context);
			InternalCaching.removeEventFromCache(eventid, context);
			listnerOnSuccess.actionComplete(Action.CURRENTPARTICIPANTREMOVEDBYINITIATOR);
		}
		catch(Exception ex){
			Log.d(TAG, ex.toString());
			ex.printStackTrace();
			listnerOnFailure.actionFailed(null, Action.CURRENTPARTICIPANTREMOVEDBYINITIATOR);
		}
	}

	public static void refreshEventList(final Context context, final OnRefreshEventListCompleteListner listnerOnSuccess, final OnActionFailedListner listnerOnFailure){

		String message ="";
		if(!AppService.isNetworkAvailable(context))
		{
			message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
			Log.d(TAG, message);
			if(listnerOnFailure!=null){
				listnerOnFailure.actionFailed(message, Action.REFRESHEVENTLIST);
			}
			return ;
		}

		APICaller.RefreshEventListFromServer(context, new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {

				try {								
					String Status = (String)response.getString("Status");
					Log.d(TAG, "EventResponse status:" + Status);
					if (Status == "true")
					{
						List<EventDetail> eventDetailList =  new JsonParser().parseEventDetailList(response.getJSONArray("ListOfEvents"), context);
						EventService.RemovePastEvents(context, eventDetailList);
						EventService.upDateEventStatus(eventDetailList);
						InternalCaching.saveEventListToCache(eventDetailList, context);	
						EventTrackerLocationService.peroformSartStop(context.getApplicationContext());
						if(listnerOnSuccess!=null){
							listnerOnSuccess.RefreshEventListComplete(eventDetailList);
						}
					}
					else{
						if(listnerOnFailure!=null){
							listnerOnFailure.actionFailed(null, Action.REFRESHEVENTLIST);
						}
					}

				} catch (Exception ex) {
					Log.d(TAG, ex.toString());				
					ex.printStackTrace();
					if(listnerOnFailure!=null){
						listnerOnFailure.actionFailed(null, Action.REFRESHEVENTLIST);
					}
				}		

			}
		}, new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {
				if(response!=null){
					Log.d(TAG, "EventResponse:" + response.toString());
				}
				if(listnerOnFailure!=null){
					listnerOnFailure.actionFailed(null, Action.REFRESHEVENTLIST);
				}				
			}
		});
	}

	public static void saveUsersLocationDetailList(Context context, EventDetail event,
			ArrayList<UsersLocationDetail> usersLocationDetailList) {
		if(event!=null && event.getCurrentParticipant().getAcceptanceStatus()!= AcceptanceStatus.DECLINED
				&&usersLocationDetailList!=null && usersLocationDetailList.size()>0){
			event.setUsersLocationDetailList(usersLocationDetailList);
			InternalCaching.saveEventToCache(event, context);
		}

	}

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

		APICaller.pokeParticipants(context, pokeParticipantsJSON, new OnAPICallCompleteListner() {

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

	private static void checkForReccurrence(final Context context, EventDetail event){
		String strIsReccurrence = event.getIsRecurrence();
		if(strIsReccurrence!=null && strIsReccurrence.equals("true")){
			refreshEventList(context, new OnRefreshEventListCompleteListner() {

				@Override
				public void RefreshEventListComplete(List<EventDetail> eventDetailList) {
					Intent eventRefreshed = new Intent(Veranstaltung.EVENTS_REFRESHED);
					LocalBroadcastManager.getInstance(context).sendBroadcast(eventRefreshed);

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
		ArrayList<TrackLocationMember>  slist = new ArrayList<TrackLocationMember>();
		List<EventDetail> list = getTrackingEventList(context);
		int eventTypeId;
		ArrayList<EventParticipant>members;

		switch(inorOut){		
		case "LocationsOut":			
			for(EventDetail e : list){
				members = e.getParticipants();
				ContactAndGroupListManager.assignContactsToEventMembers(members, context);
				eventTypeId = Integer.parseInt(e.getEventTypeId());
				//Out going locations - 100 - Share my location - current user is initiator - add all members except me
				if(eventTypeId == 100 && EventParticipant.isCurrentUserInitiator(e.getInitiatorId())){
					members.remove(e.getCurrentParticipant());
					for(EventParticipant mem : members){
						slist.add(new TrackLocationMember(e, mem, mem.getAcceptanceStatus()));
					}					
				}
				//Out going locations 200 - Track Buddy - Current user is not Initiator - add only initiator but only if I have accepted earlier else it will be in my pending items
				else if(eventTypeId ==200 && !EventParticipant.isCurrentUserInitiator(e.getInitiatorId()) && e.getCurrentParticipant().getAcceptanceStatus() == AcceptanceStatus.ACCEPTED){
					slist.add(new TrackLocationMember(e, e.getMember(e.getInitiatorId()), AcceptanceStatus.ACCEPTED));
				}
			}
			break;
		case "locationsIn":
			for(EventDetail e : list){
				members = e.getParticipants();
				ContactAndGroupListManager.assignContactsToEventMembers(members, context);
				eventTypeId = Integer.parseInt(e.getEventTypeId());
				//In coming locations - 100 - Share my location - Current user is not Initiator - add only initiator but only if I have accepted earlier else it will be in my pending items
				if(eventTypeId == 100 && !EventParticipant.isCurrentUserInitiator(e.getInitiatorId())&& e.getCurrentParticipant().getAcceptanceStatus() == AcceptanceStatus.ACCEPTED){
					slist.add(new TrackLocationMember(e, e.getMember(e.getInitiatorId()), AcceptanceStatus.ACCEPTED));				
				}
				//In coming locations - 200 - track buddy - Current user is initiator - add all members except me
				else if(eventTypeId ==200 && EventParticipant.isCurrentUserInitiator(e.getInitiatorId())){
					e.getParticipants().remove(e.getCurrentParticipant());
					for(EventParticipant mem : members){
						slist.add(new TrackLocationMember(e, mem, mem.getAcceptanceStatus()));
					}		
				}
			}
			break;		
		}
		return slist;
	}

	public static List<EventDetail> getTrackingEventList(Context context) {
		List<EventDetail> list = InternalCaching.getTrackEventListFromCache(context);		
		//removePastEvents(context, list);		
		return list;
	}

	public static void removeBuddyFromSharing(Context mContext, String userId,
			OnActionCompleteListner onActionCompleteListner,
			OnActionFailedListner onActionFailedListner) {
	}	
}



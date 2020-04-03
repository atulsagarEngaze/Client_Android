package com.redtop.engaze.service;

import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.manager.EventNotificationManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class EventTrackerAlarmReceiverService extends BroadcastReceiver
{	private static final String TAG = EventTrackerAlarmReceiverService.class.getName();
	@Override
	public void onReceive(Context context, Intent intent)
	{		
		final String eventId   = intent.getStringExtra("EventId");

		switch(intent.getStringExtra("AlarmType"))
		{
		case Veranstaltung.EVENT_START:
			EventManager.startEvent(eventId);
			break;
		case Veranstaltung.EVENT_OVER:
			EventManager.eventOver(eventId);
			Intent eventRemoved = new Intent(Veranstaltung.EVENT_OVER);
			eventRemoved.putExtra("eventId", eventId);						
			LocalBroadcastManager.getInstance(context).sendBroadcast(eventRemoved);

			break;
		case Veranstaltung.EVENT_REMINDER:
			Event eventData = InternalCaching.getEventFromCache(eventId);
			if(eventData !=null){
				String reminderType = intent.getStringExtra("ReminderType");
				if(reminderType.equals("alarm")){				
					EventNotificationManager.ringAlarm();
				}
				else if(reminderType.equals("notification")){
					EventNotificationManager.showReminderNotification(eventData);
				}
			}
			break;
		case Veranstaltung.TRACKING_STARTED:
			EventManager.eventTrackingStart(eventId);
			Intent trackingStarted = new Intent(Veranstaltung.TRACKING_STARTED);
			trackingStarted.putExtra("eventId", eventId);
			LocalBroadcastManager.getInstance(context).sendBroadcast(trackingStarted);
			break;
		case Constants.CHECK_LOCATION_SERVICE:
			 Log.d(TAG, "Alarm received to check location service");
			EventTrackerLocationService.peroformSartStop();
			EventService.setLocationServiceCheckAlarm();
			break;
		default :
			break;
		}
	}
}
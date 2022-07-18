package com.redtop.engaze.service;

import java.util.ArrayList;
import java.util.List;

import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.manager.EventManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		BackgroundLocationService.start(context);
		EventManager.setLocationServiceCheckAlarm();
		startAlarms(context);
	}
	
	private void startAlarms(Context context){
		List<Event> events = InternalCaching.getEventListFromCache();
		for(Event ed : events){
			ArrayList<EventParticipant> alertMems = ed.ReminderEnabledMembers;
			if(alertMems!=null && alertMems.size()>0){
				for(EventParticipant mem : alertMems){
					Intent eventDistanceReminderServiceIntent = new Intent(context, EventDistanceReminderService.class);
					eventDistanceReminderServiceIntent.putExtra("EventId", ed.eventId);
					eventDistanceReminderServiceIntent.putExtra("MemberId", mem.userId);
					context.startService(eventDistanceReminderServiceIntent);
				}
			}
		}
	}
}

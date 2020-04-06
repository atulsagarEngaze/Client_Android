package com.redtop.engaze.utils;

import java.util.ArrayList;
import java.util.List;

import com.redtop.engaze.entity.EventDetail;
import com.redtop.engaze.entity.EventMember;
import com.redtop.engaze.service.EventDistanceReminderService;
import com.redtop.engaze.service.EventTrackerLocationService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) { 	
		//context.startService(new Intent(context, EventTrackerBackgroundService.class));		
		EventTrackerLocationService.peroformSartStop(context);
		EventHelper.setLocationServiceCheckAlarm(context);
		startAlarms(context);
	}
	
	private void startAlarms(Context context){
		List<EventDetail> events = InternalCaching.getEventListFromCache(context);
		for(EventDetail ed : events){
			ArrayList<EventMember> alertMems = ed.getReminderEnabledMembers();
			if(alertMems!=null && alertMems.size()>0){
				for(EventMember mem : alertMems){
					Intent eventDistanceReminderServiceIntent = new Intent(context, EventDistanceReminderService.class);
					eventDistanceReminderServiceIntent.putExtra("EventId", ed.getEventId());
					eventDistanceReminderServiceIntent.putExtra("MemberId", mem.getUserId());
					context.startService(eventDistanceReminderServiceIntent);
				}
			}
		}
	}
}

package com.redtop.engaze.receiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.redtop.engaze.HomeActivity;
import com.redtop.engaze.common.constant.Veranstaltung;

public class HomeBroadcastReceiver extends LocalBroadcastReceiver {

	public HomeActivity activity;

	public HomeBroadcastReceiver(Context context) {
		super(context);
		activity = (HomeActivity)mContext;		
		initializeFilter();
	}

	private void initializeFilter() {
		mFilter = new IntentFilter();
		mFilter.addAction(Veranstaltung.TRACKING_STARTED);
		mFilter.addAction(Veranstaltung.EVENT_OVER);
		mFilter.addAction(Veranstaltung.EVENT_ENDED);
		mFilter.addAction(Veranstaltung.EVENT_ENDED_BY_INITIATOR);
		mFilter.addAction(Veranstaltung.REMOVED_FROM_EVENT_BY_INITIATOR);
		mFilter.addAction(Veranstaltung.EVENT_LEFT);
		mFilter.addAction(Veranstaltung.EVENT_RECEIVED);
		mFilter.addAction(Veranstaltung.EVENT_USER_RESPONSE);
		mFilter.addAction(Veranstaltung.EVENTS_REFRESHED);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		switch (intent.getAction()) {
		case Veranstaltung.EVENT_USER_RESPONSE:
			activity.refreshShareMyLocationList();
			activity.refreshTrackBuddyList();
			activity.refreshRunningEventList();
			activity.refreshPendingEventList();
			break;
		
		case Veranstaltung.TRACKING_STARTED:
			activity.refreshRunningEventList();			
			break;
		case Veranstaltung.EVENT_OVER:
			activity.refreshRunningEventList();
			activity.refreshPendingEventList();
			activity.refreshShareMyLocationList();
			activity.refreshTrackBuddyList();
			break;
		case Veranstaltung.EVENT_ENDED:
			activity.refreshRunningEventList();
			activity.refreshShareMyLocationList();
			activity.refreshTrackBuddyList();
			break;
		case Veranstaltung.EVENT_ENDED_BY_INITIATOR:
			activity.refreshPendingEventList();
			activity.refreshRunningEventList();
			activity.refreshShareMyLocationList();
			activity.refreshTrackBuddyList();
			break;
		case Veranstaltung.REMOVED_FROM_EVENT_BY_INITIATOR:
			activity.refreshPendingEventList();
			activity.refreshRunningEventList();
			activity.refreshShareMyLocationList();
			activity.refreshTrackBuddyList();
			break;
		case Veranstaltung.EVENTS_REFRESHED:
			activity.refreshPendingEventList();
			activity.refreshRunningEventList();
			activity.refreshShareMyLocationList();
			activity.refreshTrackBuddyList();
			break;	
		case Veranstaltung.EVENT_RECEIVED:
			activity.refreshPendingEventList();
			activity.refreshRunningEventList();
			
			break;
			

		default:
			break;
		}		
	}
}

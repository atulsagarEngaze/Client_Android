package com.redtop.engaze.localbroadcastmanager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.common.constant.Veranstaltung;

// this can be a person from contact list or can be a group which will be resolved to actual contact at server
public class RunningEventBroadcastManager  extends LocalBroadcastManager{

	public RunningEventActivity activity;
	public IntentFilter mFilterEventNotExist;

	public RunningEventBroadcastManager(Context context) {
		super(context);
		activity = (RunningEventActivity)mContext;		
		initializeFilter();
	}

	private void initializeFilter() {
		mFilter = new IntentFilter();
		mFilter.addAction(Veranstaltung.PARTICIPANT_LEFT_EVENT);
		mFilter.addAction(Veranstaltung.EVENT_USER_RESPONSE);
		mFilter.addAction(Veranstaltung.EVENT_OVER);
		mFilter.addAction(Veranstaltung.EVENT_ENDED_BY_INITIATOR);
		mFilter.addAction(Veranstaltung.EVENT_DESTINATION_UPDATED_BY_INITIATOR);
		mFilter.addAction(Veranstaltung.EVENT_PARTICIPANTS_UPDATED_BY_INITIATOR);
		mFilter.addAction(Veranstaltung.EVENT_DESTINATION_UPDATED);
		mFilter.addAction(Veranstaltung.REMOVED_FROM_EVENT_BY_INITIATOR);
		mFilter.addAction(Veranstaltung.EVENT_EXTENDED_BY_INITIATOR);

		mFilterEventNotExist = new IntentFilter();
		mFilterEventNotExist.addAction(Veranstaltung.EVENT_OVER);
		mFilterEventNotExist.addAction(Veranstaltung.EVENT_ENDED_BY_INITIATOR);
		mFilterEventNotExist.addAction(Veranstaltung.REMOVED_FROM_EVENT_BY_INITIATOR);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String receivedEventId = intent.getStringExtra("eventId");
		if(!(receivedEventId!=null && activity.mEvent!=null && receivedEventId.equals(activity.mEvent.EventId))){
			return;
		}
		switch (intent.getAction()){

		case Veranstaltung.PARTICIPANT_LEFT_EVENT:
			String eventResponderName  = intent.getStringExtra("EventResponderName");
			activity.onParticipantLeft(eventResponderName);
			break;
		case Veranstaltung.EVENT_USER_RESPONSE:
			int eventAcceptanceStateId = intent.getIntExtra("eventAcceptanceStateId", -1); 
			String EventResponderName  = intent.getStringExtra("EventResponderName"); 
			activity.onUserResponse(eventAcceptanceStateId, EventResponderName);

			break;

		case Veranstaltung.EVENT_EXTENDED_BY_INITIATOR:
			String ExtendEventDuration  = intent.getStringExtra("com.redtop.engaze.service.ExtendEventDuration");
			activity.onEventExtendedByInitiator(ExtendEventDuration);
			break;	

		case Veranstaltung.EVENT_OVER:
			activity.onEventOver();	

			break;
		case Veranstaltung.EVENT_ENDED_BY_INITIATOR:
			activity.onEventEndedByInitiator();			
			break;

		case Veranstaltung.EVENT_DESTINATION_UPDATED_BY_INITIATOR:
			String changedDestination  = intent.getStringExtra("com.redtop.engaze.service.UpdatedDestination");											
			activity.onEventDestinationUpdatedByInitiator(changedDestination);
			break;

		case Veranstaltung.REMOVED_FROM_EVENT_BY_INITIATOR:
			activity.onUserRemovedFromEventByInitiator();

			break;
		case Veranstaltung.EVENT_PARTICIPANTS_UPDATED_BY_INITIATOR:
			activity.onEventParticipantUpdatedByInitiator();
			break;
		}				

	}
	public IntentFilter getFilter(){
		return mFilter;
	}
}

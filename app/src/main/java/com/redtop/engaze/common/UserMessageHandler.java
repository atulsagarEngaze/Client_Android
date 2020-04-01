package com.redtop.engaze.common;
import android.content.Context;
import android.content.res.Resources;

import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.Action;

public class UserMessageHandler {
	
	public static String getFailureMessage(Action action){
		String msg="";
		Resources res = AppContext.context.getResources();
		switch (action) {
		case POKEALL://poke all
			msg = res.getString(R.string.message_runningEvent_pokeAllFail);
			break;
		case LEAVEEVENT:
			msg = res.getString(R.string.message_runningEvent_eventLeaveFail);
			break;
		case ENDEVENT:
			msg = res.getString(R.string.message_runningEvent_eventEndFail);
			break;
		case EXTENDEVENTENDTIME:
			msg = res.getString(R.string.message_runningEvent_eventExtendFail);
			break;
		case ADDREMOVEPARTICIPANTS:
			msg = res.getString(R.string.message_runningEvent_updateParticipantsFailure);
			break;
		case CHANGEDESTINATION:
			msg = res.getString(R.string.message_runningEvent_updateDestinationFailure);
			break;
		case SAVEUSERRESPONSE:
			msg = res.getString(R.string.save_fail);
			break;
		case DELETEEVENT:
			msg = res.getString(R.string.message_mySchedule_deleteFailue);
			break;
		case SAVEEVENT:
			msg = res.getString(R.string.label_createEvent_errorMessage);
			break;
		case SAVEEVENTSHAREMYLOCATION:
			msg = res.getString(R.string.label_sharemylocation_errorMessage);
			break;
		case SAVEEVENTTRACKBUDDY:
			msg = res.getString(R.string.label_track_my_buddy_errorMessage);
			break;
		case SAVEPROFILE:
			msg = res.getString(R.string.message_userReg_errorSaving);
			break;
		case REFRESHEVENTLIST:
			msg = res.getString(R.string.f_failed_retrieve_events);
			break;
		case UPDATEEVENTWITHPARTICIPANTRESPONSE:							
		case UPDATEEVENTWITHPARTICIPANTLEFT:				
		case EVENTENDEDBYINITIATOR:				
		case EVENTEXTENDEDBYINITIATOR:
		case EVENTDELETEDBYINITIATOR:
		case PARTICIPANTSUPDATEDBYINITIATOR:
		case CURRENTPARTICIPANTREMOVEDBYINITIATOR:
		case EVENTDESTINATIONCHANGEDBYINITIATOR:
		case REMOVEBUDDYFROMSHARING:
			msg = res.getString(R.string.message_general_response_failed);
			break;
		case GETEVENTDATAFROMSERVER:
			msg = res.getString(R.string.message_general_event_retreive_failed);
			break;
		case POKEPARTICIPANT:
			msg = res.getString(R.string.message_runningEvent_pokeFail);
			break;
		default:
			msg = res.getString(R.string.message_general_response_failed);
			break;
		}
		return msg;
	}
	
	public static String getSuccessMessage(Action action){
		String msg="";
		Resources res = AppContext.context.getResources();
		switch (action) {
		case POKEALL://poke all
			msg = res.getString(R.string.message_runningEvent_pokedAllSuccessfully);
			break;
		case LEAVEEVENT:
			msg = res.getString(R.string.message_runningEvent_eventLeftSuccessfully);
			break;
		case ENDEVENT:
			msg = res.getString(R.string.message_general_event_end_success);
			break;
		case EXTENDEVENTENDTIME:
			msg = res.getString(R.string.message_runningEvent_eventExtendSuccess);
			break;
		case ADDREMOVEPARTICIPANTS:
			msg = res.getString(R.string.message_runningEvent_updateParticipantsSuccess);
			break;
		case CHANGEDESTINATION:
			msg = res.getString(R.string.message_runningEvent_updateDestinationSuccess);
			break;
		case SAVEUSERRESPONSE:
			msg = res.getString(R.string.save_success);
			break;
		case DELETEEVENT:
			msg = res.getString(R.string.message_mySchedule_deleteSuccess);
			break;
		case SAVEEVENT:
			msg = res.getString(R.string.label_createEvent_errorMessage);
			break;
		case SAVEEVENTSHAREMYLOCATION:
			msg = res.getString(R.string.label_sharemylocation_errorMessage);
			break;
		case SAVEEVENTTRACKBUDDY:
			msg = res.getString(R.string.label_track_my_buddy_errorMessage);
			break;
		case SAVEPROFILE:
			msg = res.getString(R.string.message_userReg_errorSaving);
			break;
		case REFRESHEVENTLIST:
			msg = res.getString(R.string.f_failed_retrieve_events);
			break;
		case UPDATEEVENTWITHPARTICIPANTRESPONSE:							
		case UPDATEEVENTWITHPARTICIPANTLEFT:				
		case EVENTENDEDBYINITIATOR:				
		case EVENTEXTENDEDBYINITIATOR:
		case EVENTDELETEDBYINITIATOR:
		case PARTICIPANTSUPDATEDBYINITIATOR:
		case CURRENTPARTICIPANTREMOVEDBYINITIATOR:
		case EVENTDESTINATIONCHANGEDBYINITIATOR:
		case REMOVEBUDDYFROMSHARING:
			msg = res.getString(R.string.message_general_response_failed);
			break;
		case GETEVENTDATAFROMSERVER:
			msg = res.getString(R.string.message_general_event_retreive_failed);
			break;
		case POKEPARTICIPANT:
			msg = res.getString(R.string.message_runningEvent_pokedSuccessfully);
			break;
		default:
			msg = res.getString(R.string.message_general_response_failed);
			break;
		}
		return msg;
	}
}

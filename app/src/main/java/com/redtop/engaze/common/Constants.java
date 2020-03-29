package com.redtop.engaze.common;

public class Constants {	
	public static boolean DEBUG = false;
	public static final int MAX_DESTINATION_CACHE_COUNT = 5;
	public static final int MAX_SMS_MESSAGE_LENGTH = 20;
	public static final int VALID_MOBILE_NUMBER = 21;
	public static final int INVALID_MOBILE_NUMBER = 22;
	public static final int OPERATION_TIME_OUT = 23;
	public static final int ERROR_VERIFY_MOBILE_NUMBER = 24;
	public static final String SMS_PORT = "8901";
	public static final int DEFAULT_SHORT_TIME_TIMEOUT = 20000;//millisecond
	public static final int DEFAULT_MEDIUM_TIME_TIMEOUT = 40000;//millisecond
	public static final int DEFAULT_LONG_TIME_TIMEOUT = 120000;//millisecond
	public static final int LOCATION_RETRIVAL_INTERVAL = 25000;//millisecond
	public static final int EVENTS_REFRESH_INTERVAL = 60000;
	public static final int RUNNING_EVENT_CHECK_INTERVAL = 120000;

	public static final int SMS_TIMEOUT_PERIOD = 120000;
	public static final int SMS_INTERVAL_MILISECOND = 1000;
	public static final int LOCATION_REFRESH_INTERVAL_FAST= 15000;//milliseconds
	public static final int LOCATION_REFRESH_INTERVAL_NORMAL= 20000;//milliseconds
	public static final int LOCATION_REFRESH_SLOWER_NORMAL= 60000;//milliseconds

	public static final int SNOOZING_REQUEST_CODE = 1;
	//public static final String MAP_API_URL = "http://watchus-001-site1.smarterasp.net/Api/";
	//public static final String MAP_API_URL = "http://watchus-001-site1.htempurl.com/Api/";


	public static final String TAG_ID_COUNTRY = "Code";
	public static final String TAG_NAME = "Name";
	public static final String MOBILE_NUMBER = "MobileNumber";
	public static final String DEVICE_ID = "DeviceId";
	public static final String LOGIN_ID = "LoginID";
	public static final String LOGIN_NAME = "LoginName";
	public static final String COUNTRY_CODE = "countryCode";

	// preference file key	
	public static final String USER_AUTH_TOKEN = "user_auth_token";

	public static final String SUBFOLDER_JARVIS = "/eventtracker";
	public static final int READ_DATA_MODEL_OBJECT_FROM_FILE = 111;
	public static final int WRITE_DATA_MODEL_OBJECT_TO_FILE = 112;

	public static final String FILE_HEADER_EVENT_DETAIL = "event_detail";


	public static final String REGISTRATION_COMPLETE = "registrationComplete";
	public static final String NETWORK_STATUS_UPDATE = "networkStatusUpdate";
	public static final String GCM_REGISTRATION_TOKEN = "gcmregistrationToken";
	public static final String CURRENT_LATITUDE ="latitude";
	public static final String CURRENT_LONGITUDE ="longitude";
	public static final int ReminderBroadcastId = 1;
	public static final int TrackingStartBroadcastId = 2;
	public static final int TrackingStopBroadcastId = 3;
	public static final int EventEndBroadcastId = 4;
	public static final int EventStartBroadcastId = 5;
	public static final int LocationServiceCheckBroadcastId = 6;

	public static final String EVENT_LIST_UPDATE = "eventlistupdate";
	public static final String CONTACT_LIST_UPDATE = "contactlistupdate";	
	public static final String IS_REGISTERED_CONTACT_LIST_INITIALIZED= "isrcli";
	public static final String IS_CONTACT_LIST_INITIALIZED = "iscli";
	public static final String IS_CONTACT_LIST_INITIALIZATION_PROCESS_OVER = "isclipo";
	public static final String EVENT_LIST_LATEST = "eventListLatest";
	public static final String EVENT_RECEIVED ="eventReceived";
	public static final String EVENT_REMOVED= "eventRemoved";
	public static final String EVENT_EXTENDED= "eventExtended";	
	public static final String EVENT_UPDATED= "eventUpdated";	
	public static final String EVENT_USER_RESPONSE = "eventUserResponse";
	public static final String EVENT_OVER = "eventOver";
	public static final String EVENT_ENDED = "eventEnded";	
	public static final String EVENT_LEFT = "eventLeft";
	public static final String EVENT_DELETE = "eventDeleted";	
	public static final String EVENT_PARTICIPANTS_UPDATED= "eventParticipantsUpdated";	
	public static final String EVENT_REMINDER = "eventReminder";
	public static final String EVENT_START = "eventStart";
	public static final String EVENTS_REFRESHED = "eventsRefreshed";
	public static final String TRACKING_STARTED = "trackingStarted"; 
	public static final String CHECK_LOCATION_SERVICE = "checkLocationService";
	public static final String EVENT_DESTINATION_UPDATED = "eventDestinationUpdated"; 
	public static final String EVENT_DESTINATION_UPDATED_BY_INITIATOR = "eventDestinationUpdatedByInitiator";
	public static final String EVENT_PARTICIPANTS_UPDATED_BY_INITIATOR = "eventParticipantsUpdatedByInitiator";
	public static final String REMOVED_FROM_EVENT_BY_INITIATOR = "removedFromEventByInitiator";
	public static final String EVENT_ENDED_BY_INITIATOR = "eventEndedByInitiator";
	public static final String EVENT_DELETE_BY_INITIATOR = "eventDeletedByInitiator";
	public static final String EVENT_UPDATED_BY_INITIATOR= "eventUpdatedByInitiator";
	public static final String EVENT_EXTENDED_BY_INITIATOR= "eventExtendedByInitiator";
	public static final String PARTICIPANT_LEFT_EVENT = "participantLeftEvent";

	public static final String PARTICIPANTS_LOCATION_UPDATE_INTENT_ACTION = "com.redtop.engaze.PARTICIPANTS_LOCATION_UPDATE";

	public static final String CACHE_EVENTS ="events";
	public static final String CACHE_TRACK_EVENTS ="trackevents";
	public static final String CACHE_CONTACTS ="contacts";
	public static final String CACHE_REGISTERED_CONTACTS ="registeredcontacts";
	public static final String CACHE_DESTINATIONS ="destinations";

	public static final int HOME_ACTIVITY_LOCATION_TEXT_LENGTH = 44;
	public static final int PICK_LOCATION_ACTIVITY_LOCATION_TEXT_LENGTH = 36;
	public static final int EVENTS_ACTIVITY_LOCATION_TEXT_LENGTH = 24;
	public static final int EDIT_ACTIVITY_LOCATION_TEXT_LENGTH = 34;
	public static final int MEMBER_NAME_TEXT_LENGTH = 20;
	public static final int ZOOM_VALUE= 16;
	public static final String TRACKING_ON = "1";
	public static final String TRACKING_OFF = "2";
	public static final String EVENT_END = "3";	
	public static final String EVENT_OPEN = "4";
	public static final String EMAIL_ACCOUNT = "emailAccount";
	public static final float DESTINATION_RADIUS = 30;
	public static final long POKE_INTERVAL = 15;
}



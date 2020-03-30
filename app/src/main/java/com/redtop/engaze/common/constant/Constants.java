package com.redtop.engaze.common.constant;

public class Constants {	
	public static boolean DEBUG = false;
	public static final int MAX_DESTINATION_CACHE_COUNT = 5;
	public static final int MAX_SMS_MESSAGE_LENGTH = 20;
	public static final int VALID_MOBILE_NUMBER = 21;
	public static final int INVALID_MOBILE_NUMBER = 22;
	public static final int OPERATION_TIME_OUT = 23;
	public static final int ERROR_VERIFY_MOBILE_NUMBER = 24;


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

	public static final String CHECK_LOCATION_SERVICE = "checkLocationService";

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


	public static final String PARTICIPANTS_LOCATION_UPDATE_INTENT_ACTION = "com.redtop.engaze.PARTICIPANTS_LOCATION_UPDATE";



	public static final int HOME_ACTIVITY_LOCATION_TEXT_LENGTH = 44;
	public static final int PICK_LOCATION_ACTIVITY_LOCATION_TEXT_LENGTH = 36;
	public static final int EVENTS_ACTIVITY_LOCATION_TEXT_LENGTH = 24;
	public static final int EDIT_ACTIVITY_LOCATION_TEXT_LENGTH = 34;
	public static final int MEMBER_NAME_TEXT_LENGTH = 20;
	public static final int ZOOM_VALUE= 16;

	public static final String EMAIL_ACCOUNT = "emailAccount";
	public static final float DESTINATION_RADIUS = 30;
	public static final long POKE_INTERVAL = 15;
}



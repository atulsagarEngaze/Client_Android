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

    public static final String NETWORK_STATUS_UPDATE = "networkStatusUpdate";
    public static final String GCM_REGISTRATION_TOKEN = "gcmregistrationToken";
    public static final String CURRENT_LATITUDE = "latitude";
    public static final String CURRENT_LONGITUDE = "longitude";


    public static final int ReminderBroadcastId = 1;
    public static final int TrackingStartBroadcastId = 2;
    public static final int TrackingStopBroadcastId = 3;
    public static final int EventEndBroadcastId = 4;
    public static final int EventStartBroadcastId = 5;
    public static final int LocationServiceCheckBroadcastId = 6;

    public static final String LAST_CONTACT_LIST_REFRESH_STATUS = "lclrs";
    public static final String LAST_REGISTERED_CONTACT_LIST_REFRESH_STATUS = "lrclrs";
    public static final String REFRESH_ONLY_REGISTERED_CONTACTS = "rorc";

    public static String SUCCESS = "success";
    public static String FAILED = "failed";


    public static final String PARTICIPANTS_LOCATION_UPDATE_INTENT_ACTION = "com.redtop.engaze.PARTICIPANTS_LOCATION_UPDATE";


    public static final int HOME_ACTIVITY_LOCATION_TEXT_LENGTH = 44;
    public static final int EVENTS_ACTIVITY_LOCATION_TEXT_LENGTH = 24;
    public static final int EDIT_ACTIVITY_LOCATION_TEXT_LENGTH = 34;
    public static final int MEMBER_NAME_TEXT_LENGTH = 20;
    public static final int ZOOM_VALUE = 16;

    public static final String EMAIL_ACCOUNT = "emailAccount";
    public static final float DESTINATION_RADIUS = 30;
    public static final long POKE_INTERVAL = 15;

    public static final int ROUTE_END_POINT_REQUEST_CODE = 9;

    //Default values, can be overwritten from event default settings activity
    public static final int REMINDER_DEFAULT_INTERVAL = 30;
    public static final String REMINDER_DEFAULT_PERIOD = "minute";
    public static final String REMINDER_DEFAULT_NOTIFICATION = "notification";
    public static final boolean TRACKING_DEFAULT_ENABLED = true;
    public static final int TRACKING_DEFAULT_INTERVAL = 30;
    public static final String TRACKING_DEFAULT_PERIOD = "minute";
    public static final int EVENT_DEFAULT_DURATION = 1;
    public static final String EVENT_DEFAULT_PERIOD = "hour";

    public static final String DEFAULT_REMINDER_PREF_KEY = "defltRmndr";
    public static final String DEFAULT_TRACKING_PREF_KEY = "defltrck";
    public static final String DEFAULT_DURATION_PREF_KEY = "defdurtn";

    public static final String LOCATION_UNKNOWN = "Unknown";


}



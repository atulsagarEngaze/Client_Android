package com.redtop.engaze.app;

public class Config {

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
    public static final int  MIN_DISTANCE_IN_METER_LOCATION_UPDATE = 20;

    //APIs
    public static final String RegisterBaseURL ="https://yvunorg2k3.execute-api.us-east-2.amazonaws.com/PreProduction/";
    public static final String EventBaseURL="http://redtopdev.in/";
    public static final String LocationBaseURL="http://redtopdev.in/";


}

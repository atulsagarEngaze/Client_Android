package com.redtop.engaze.webservice;

import com.redtop.engaze.app.Config;

public class ApiUrl {

    public static final String SMS_GATEWAY = "Contacts/SendSMSOTP";
    public static final String EMAIL_EXCEPTION_URL = "http://redtopdev.com/server.php/";
    public static final String REGISTERED_CONTACTS = Config.RegisterBaseURL + "users/registered";
    public static final String ACCOUNT_REGISTER = Config.RegisterBaseURL + "users/register";

    public static final String FETCH_USER_LOCATION = Config.LocationBaseURL + "location/{eventId}/{requesterId}";
    public static final String UPLOAD_USER_LOCATION = Config.LocationBaseURL + "location/{userId}";


    public static final String COUNTRY_CODES = "CountryCodes";
    public static final String EVENT_DETAIL = Config.EventBaseURL + "events/user/{userId}";


    public static final String RESPOND_INVITE = Config.EventBaseURL + "Event/RespondToInvite";
    public static final String POKEALL_CONTACTS = Config.EventBaseURL + "Contacts/RemindContact";
    public static final String CREATE_EVENT = Config.EventBaseURL + "evento";
    public static final String UPDATE_EVENT = Config.EventBaseURL + "Event/Update";
    public static final String EXTEND_EVENT = Config.EventBaseURL + "evento/{eventId}/extend/{endTime}";
    public static final String END_EVENT = Config.EventBaseURL + "evento/{eventId}/end";
    public static final String LEAVE_EVENT = Config.EventBaseURL + "evento/{eventId}/participant/{participantId}/leave";
    public static final String DELETE_EVENT = Config.EventBaseURL + "evento/{eventId}";
    public static final String UPDATE_PARTICIPANTS = Config.EventBaseURL + "evento/{eventId}/participants";
    public static final String UPDATE_DESTINATION = Config.EventBaseURL + "Event/UpdateEventLocation";
    public static final String SAVE_FEEDBACK = Config.EventBaseURL + "Account/SaveFeedback";
}

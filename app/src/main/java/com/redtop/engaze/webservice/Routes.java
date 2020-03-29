package com.redtop.engaze.webservice;

public class Routes {

    public static final String SMS_GATEWAY = "Contacts/SendSMSOTP";
    public static final String EMAIL_EXCEPTION_URL = "http://redtopdev.com/server.php/";
    public static final String GET_REGISTERED_CONTACTS = "Contacts/GetRegisteredContacts";
    public static final String COUNTRY_CODES = "CountryCodes";
    public static final String EVENT_DETAIL = "Event/Get";
    public static final String USER_LOCATION = "Location/Get";
    public static final String USER_LOCATION_UPLOAD="Location/Upload";
    public static final String ACCOUNT_REGISTER = "Account/Register";
    public static final String RESPOND_INVITE = "Event/RespondToInvite";
    public static final String POKEALL_CONTACTS = "Contacts/RemindContact";
    public static final String CREATE_EVENT = "Event/CreateEvent";
    public static final String UPDATE_EVENT = "Event/Update";
    public static final String EXTEND_EVENT = "Event/ExtendEvent";
    public static final String END_EVENT = "Event/EndEvent";
    public static final String LEAVE_EVENT = "Event/LeaveEvent";
    public static final String DELETE_EVENT = "Event/DeleteEvent";
    public static final String UPDATE_PARTICIPANTS = "Event/UpdateParticipants";
    public static final String UPDATE_DESTINATION = "Event/UpdateEventLocation";
    public static final String SAVE_FEEDBACK = "Account/SaveFeedback";
}

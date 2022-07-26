package com.redtop.engaze.domain;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.annotations.Expose;
import com.redtop.engaze.Interface.DataModel;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.EventState;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.common.enums.RecurrenceType;
import com.redtop.engaze.manager.ParticipantManager;

public class Event implements DataModel {

    /**
     *
     */
    public static final long serialVersionUID = 1602715454105775832L;
    @Expose
    public String eventId;

    @Expose
    public String name;

    @Expose
    public EventType eventType;

    @Expose
    public String description;

    @Expose
    public String startTime;

    @Expose
    public String endTime;

    @Expose
    public Duration duration;

    @Expose
    public Duration tracking;

    @Expose
    public EventState trackingState;

    @Expose
    public String initiatorId;

    @Expose
    public String initiatorName;

    @Expose
    public EventState state;

    @Expose
    public EventPlace destination;

    @Expose
    public Reminder reminder;

    @Expose
    public ArrayList<EventParticipant> participants = new ArrayList<>();

    @Expose
    private EventParticipant currentParticipant;

    public Date startTimeInDateFormat;
    public Date endTimeInDateFormat;
    public Boolean isTrackingRequired;


    public ArrayList<EventParticipant> ReminderEnabledMembers;

    public ArrayList<ContactOrGroup> ContactOrGroups = new ArrayList<>();
    public ArrayList<UsersLocationDetail> UsersLocationDetailList;
    public ArrayList<Integer> NotificationIds;
    public int SnoozeNotificationId = 0;
    public int AcceptNotificationId = 0;
    public Boolean IsMute = false;
    public Boolean IsDistanceReminderSet = false;
    public Boolean IsRecurrence = false;
    public RecurrenceType RecurrenceType;
    public Integer NumberOfOccurences;
    public Integer NumberOfOccurencesLeft;
    public Integer FrequencyOfOccurence;
    public ArrayList<Integer> RecurrenceDays;
    public String RecurrenceActualStartTime;
    public String[] AdminList;


    public Event(String eventId, String name, EventType eventType,
                 String description, String startTime, String endTime,
                 Duration duration, String initiatorId, String initiatorName,
                 EventState state, String trackingStateId,
                 EventPlace destination, Boolean isTrackingRequired,
                 Integer reminderOffset, String reminderType, Integer trackingStartOffset, ArrayList<ContactOrGroup> contactOrGroups,
                 Boolean isQuickEvent) {
        super();
        this.eventId = eventId;
        this.name = name;
        this.eventType = eventType;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.initiatorId = initiatorId;
        this.initiatorName = initiatorName;
        this.state = state;

        this.isTrackingRequired = isTrackingRequired;
        this.ContactOrGroups = contactOrGroups;
        this.NotificationIds = new ArrayList<Integer>();
    }

    public Event(ArrayList<EventParticipant> members, String eventId, String name, EventType eventType,
                 String description, String startTime, String endTime,
                 Duration duration, String initiatorId, String initiatorName,
                 EventState state, String trackingStateId,
                 EventPlace destination, Boolean isTrackingRequired,
                 Integer reminderOffset, String reminderType, Integer trackingStartOffset,
                 Boolean isQuickEvent) {
        super();
        this.eventId = eventId;
        this.name = name;
        this.eventType = eventType;
        this.description = description;
        this.destination = destination;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.initiatorId = initiatorId;
        this.initiatorName = initiatorName;
        this.state = state;
        this.isTrackingRequired = isTrackingRequired;
        this.participants = members;
        this.NotificationIds = new ArrayList<Integer>();
    }


    public Event() {
        // TODO Auto-generated constructor stub
        this.NotificationIds = new ArrayList<Integer>();
    }

    public void setCurrentParticipant(EventParticipant participant){
        this.currentParticipant = participant;
    }

    public  EventParticipant getCurrentParticipant() {
        if (currentParticipant == null) {
            for (EventParticipant participant : this.participants) {
                if (participant.userId == AppContext.context.loginId) {
                    this.currentParticipant = participant;
                }
            }
        }
        return this.currentParticipant;
    }

    public EventParticipant getParticipant(String userId) {

        EventParticipant member = null;
        if (this.participants != null && this.participants.size() > 0) {
            for (EventParticipant mem : this.participants) {
                if (mem.userId!=null && mem.userId.equalsIgnoreCase(userId)) {
                    member = mem;
                    break;
                }
            }
        }
        return member;
    }

    @SuppressWarnings("null")
    public ArrayList<EventParticipant> getParticipantsbyStatus(AcceptanceStatus acceptanceStatus) {

        ArrayList<EventParticipant> memStatus = new ArrayList<EventParticipant>();

        if (this.participants != null && this.participants.size() > 0) {
            for (EventParticipant mem : this.participants) {
                if (mem.acceptanceStatus.name().equals(acceptanceStatus.toString())) {
                    memStatus.add(mem);
                }
            }
        }
        return memStatus;
    }

    @SuppressWarnings("null")


    public int getParticipantCount() {
        if (this.participants != null) {
            return this.participants.size();
        } else {
            return 0;
        }
    }

    public boolean isEventTrackBuddyEventForCurrentUser() {

        boolean isCurrentUserInitiator = ParticipantManager.isCurrentUserInitiator(this.initiatorId);

        if ((isCurrentUserInitiator && this.eventType == EventType.TRACKBUDDY) ||
                (!isCurrentUserInitiator && this.eventType == EventType.SHAREMYLOACTION)) {
            return true;
        }
        return false;
    }

    public boolean isEventShareMyLocationEventForCurrentUser() {

        boolean isCurrentUserInitiator = ParticipantManager.isCurrentUserInitiator(this.initiatorId);

        if ((isCurrentUserInitiator && this.eventType == EventType.SHAREMYLOACTION) ||
                (!isCurrentUserInitiator && this.eventType == EventType.TRACKBUDDY)) {
            return true;
        }
        return false;
    }

    public Boolean isEventPast() {

        try {
            Calendar cal = Calendar.getInstance();
            Date currentDate = cal.getTime();
            DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            cal.setTime(writeFormat.parse(this.endTime));
            Date endDate = cal.getTime();
            if (currentDate.getTime() > endDate.getTime()) {
                return true;
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}

package com.redtop.engaze.domain;

import java.util.ArrayList;
import java.util.Date;

import com.redtop.engaze.Interface.DataModel;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.EventState;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.common.enums.RecurrenceType;

public class Event implements DataModel {

    /**
     *
     */
    public static final long serialVersionUID = 1602715454105775832L;
    public String EventId;
    public String Name;
    public EventType EventType;
    public String Description;
    public String StartTime;
    public String EndTime;
    public Duration Duration;
    public Duration Tracking;
    public EventState TrackingState;
    public String InitiatorId;
    public String InitiatorName;
    public String[] AdminList;
    public EventState State;
    public String TrackingStateId;
    public Date TrackingStopTime;
    public EventPlace Destination;
    public Boolean IsTrackingRequired;
    public Integer ReminderOffset;
    public String ReminderType;
    public Integer TrackingStartOffset;
    public Reminder Reminder;
    public EventParticipant CurrentParticipant;
    public ArrayList<EventParticipant> Participants;
    public ArrayList<EventParticipant> ReminderEnabledMembers;

    public ArrayList<ContactOrGroup> ContactOrGroups;
    public ArrayList<UsersLocationDetail> UsersLocationDetailList;
    public ArrayList<Integer> NotificationIds;
    public int SnoozeNotificationId = 0;
    public int AcceptNotificationId = 0;
    public Boolean IsQuickEvent;
    public Boolean IsMute = false;
    public Boolean IsDistanceReminderSet = false;
    public Boolean IsRecurrence = false;
    public RecurrenceType RecurrenceType;
    public Integer NumberOfOccurences;
    public Integer NumberOfOccurencesLeft;
    public Integer FrequencyOfOccurence;
    public ArrayList<Integer> RecurrenceDays;
    public String RecurrenceActualStartTime;


    public Event(String eventId, String name, EventType eventType,
                 String description, String startTime, String endTime,
                 Duration duration, String initiatorId, String initiatorName,
                 EventState state, String trackingStateId,
                 EventPlace destination, Boolean isTrackingRequired,
                 Integer reminderOffset, String reminderType, Integer trackingStartOffset, ArrayList<ContactOrGroup> contactOrGroups,
                 Boolean isQuickEvent) {
        super();
        this.EventId = eventId;
        this.Name = name;
        this.EventType = eventType;
        this.Description = description;
        this.StartTime = startTime;
        this.EndTime = endTime;
        this.Duration = duration;
        this.InitiatorId = initiatorId;
        this.InitiatorName = initiatorName;
        this.State = state;
        this.TrackingStateId = trackingStateId;

        this.IsTrackingRequired = isTrackingRequired;
        this.ReminderOffset = reminderOffset;
        this.ReminderType = reminderType;
        this.TrackingStartOffset = trackingStartOffset;
        this.ContactOrGroups = contactOrGroups;
        this.IsQuickEvent = isQuickEvent;
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
        this.EventId = eventId;
        this.Name = name;
        this.EventType = eventType;
        this.Description = description;
        this.Destination = destination;
        this.StartTime = startTime;
        this.EndTime = endTime;
        this.Duration = duration;
        this.InitiatorId = initiatorId;
        this.InitiatorName = initiatorName;
        this.State = state;
        this.TrackingStateId = trackingStateId;

        this.IsTrackingRequired = isTrackingRequired;
        this.ReminderOffset = reminderOffset;
        this.ReminderType = reminderType;
        this.TrackingStartOffset = trackingStartOffset;
        this.Participants = members;
        this.IsQuickEvent = isQuickEvent;
        this.NotificationIds = new ArrayList<Integer>();
    }

    public Event() {
        // TODO Auto-generated constructor stub
        this.NotificationIds = new ArrayList<Integer>();
    }

    public EventParticipant getMember(String userId) {

        EventParticipant member = null;
        if (this.Participants != null && this.Participants.size() > 0) {
            for (EventParticipant mem : this.Participants) {
                if (mem.getUserId().equalsIgnoreCase(userId.toLowerCase())) {
                    member = mem;
                    break;
                }
            }
        }
        return member;
    }

    @SuppressWarnings("null")
    public ArrayList<EventParticipant> getMembersbyStatus(AcceptanceStatus acceptanceStatus) {

        ArrayList<EventParticipant> memStatus = new ArrayList<EventParticipant>();

        if (this.Participants != null && this.Participants.size() > 0) {
            for (EventParticipant mem : this.Participants) {
                if (mem.getAcceptanceStatus().name().equals(acceptanceStatus.toString())) {
                    memStatus.add(mem);
                }
            }
        }
        return memStatus;
    }

    @SuppressWarnings("null")


    public EventParticipant getCurrentParticipant() {
        return this.CurrentParticipant;
    }

    public void setCurrentParticipant(EventParticipant currentMem) {
        this.CurrentParticipant = currentMem;
    }

    public int getMemberCount() {
        if (this.Participants != null) {
            return this.Participants.size();
        } else {
            return 0;
        }
    }
}

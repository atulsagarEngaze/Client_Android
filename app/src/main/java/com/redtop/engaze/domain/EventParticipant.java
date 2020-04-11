/**
 *
 */
package com.redtop.engaze.domain;

import android.content.Context;

import com.google.gson.annotations.Expose;
import com.redtop.engaze.Interface.DataModel;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.ReminderFrom;
import com.redtop.engaze.domain.service.ParticipantService;

import java.util.ArrayList;

/**
 * @author Vijay.kumar
 *
 *         05-Aug-2015 10:55:06 pm
 */
public class EventParticipant implements DataModel {
    /**
     *
     */
    private static final long serialVersionUID = -8550528718275826735L;
    @Expose
    private String eventId;
    @Expose
    private String userId;
    @Expose
    private String profileName;
    @Expose
    private String contactName;
    @Expose
    private String mobileNumber;
    @Expose
    private String gCMClientId;

    @Expose
    private Boolean isTrackingAccepted;
    @Expose
    private String trackingStartTime;
    @Expose
    private String trackingEndTime;
    @Expose
    private String trackingEndReason;
    @Expose
    private String isTrackingActive;
    @Expose
    private String userEventEndTime;
    @Expose
    private AcceptanceStatus acceptanceStatus;
    @Expose
    private int distanceReminderDistance;
    @Expose
    private String distanceReminderId;
    @Expose
    private ReminderFrom reminderFrom;
    @Expose
    public Boolean isUserLocationShared;
    public ContactOrGroup contactOrGroup;

    public EventParticipant(String userId, String profileName, int distanceReminder, ReminderFrom distanceReminderFrom) {
        this.userId = userId;
        this.profileName = profileName;
        this.distanceReminderDistance = distanceReminder;
        this.reminderFrom = distanceReminderFrom;//0 destination and 1 from current user
    }

    public EventParticipant(String userId, String profileName, String mobileNumber,
                            AcceptanceStatus eventAcceptanceState) {
        this.userId = userId;
        this.profileName = profileName;
        this.mobileNumber = mobileNumber;
        this.acceptanceStatus = eventAcceptanceState;
    }

    public EventParticipant(){
        this.isUserLocationShared = false;
        this.isTrackingAccepted = false;
        this.acceptanceStatus = AcceptanceStatus.PENDING;
    }


    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String cName) {
        this.contactName = cName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getgCMClientId() {
        return gCMClientId;
    }

    public void setgCMClientId(String gCMClientId) {
        this.gCMClientId = gCMClientId;
    }

    public Boolean getIsTrackingAccepted() {
        return isTrackingAccepted;
    }

    public void setIsTrackingAccepted(Boolean isTrackingAccepted) {
        this.isTrackingAccepted = isTrackingAccepted;
    }

    public String getTrackingStartTime() {
        return trackingStartTime;
    }

    public void setTrackingStartTime(String trackingStartTime) {
        this.trackingStartTime = trackingStartTime;
    }

    public String getTrackingEndTime() {
        return trackingEndTime;
    }

    public void setTrackingEndTime(String trackingEndTime) {
        this.trackingEndTime = trackingEndTime;
    }

    public String getTrackingEndReason() {
        return trackingEndReason;
    }

    public void setTrackingEndReason(String trackingEndReason) {
        this.trackingEndReason = trackingEndReason;
    }

    public String getIsTrackingActive() {
        return isTrackingActive;
    }

    public void setIsTrackingActive(String isTrackingActive) {
        this.isTrackingActive = isTrackingActive;
    }

    public String getUserEventEndTime() {
        return userEventEndTime;
    }

    public void setUserEventEndTime(String userEventEndTime) {
        this.userEventEndTime = userEventEndTime;
    }

    public AcceptanceStatus getAcceptanceStatus() {
        return acceptanceStatus;
    }

    public void setAcceptanceStatus(AcceptanceStatus acceptanceStatus) {
        this.acceptanceStatus = acceptanceStatus;
    }

    public String getDistanceReminderId() {
        return distanceReminderId;
    }

    public void setDistanceReminderId(String distanceReminderId) {
        this.distanceReminderId = distanceReminderId;
    }

    public int getDistanceReminderDistance() {
        return distanceReminderDistance;
    }

    public void setDistanceReminderDistance(int distanceReminderDistance) {
        this.distanceReminderDistance = distanceReminderDistance;
    }

    public ReminderFrom getReminderFrom() {
        return reminderFrom;
    }

    public void setReminderFrom(ReminderFrom distanceReminderFrom) {
        this.reminderFrom = distanceReminderFrom;
    }

    public void setContact(ContactOrGroup cg) {
        this.contactOrGroup = cg;
    }

    public ContactOrGroup getContact() {
        return this.contactOrGroup;
    }

    @Override
    public String toString() {
        return "UserList [eventId=" + eventId + ", userId=" + userId
                + ", mobileNumber=" + mobileNumber + ", gCMClientId="
                + gCMClientId
                + ", isTrackingAccepted=" + isTrackingAccepted
                + ", trackingStartTime=" + trackingStartTime
                + ", trackingEndTime=" + trackingEndTime
                + ", trackingEndReason=" + trackingEndReason
                + ", isTrackingActive=" + isTrackingActive
                + ", userEventEndTime=" + userEventEndTime + "]";
    }


}

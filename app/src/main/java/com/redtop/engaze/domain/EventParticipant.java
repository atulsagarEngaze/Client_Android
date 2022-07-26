/**
 *
 */
package com.redtop.engaze.domain;

import com.google.gson.annotations.Expose;
import com.redtop.engaze.Interface.DataModel;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.ReminderFrom;
import com.redtop.engaze.manager.ContactAndGroupListManager;
import com.redtop.engaze.manager.ParticipantManager;

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
    public String eventId;
    @Expose
    public String userId;
    @Expose
    public String profileName;
    @Expose
    public String contactName;
    @Expose
    public String gCMClientId;

    @Expose
    public Boolean isTrackingAccepted;
    @Expose
    public String trackingStartTime;
    @Expose
    public String trackingEndTime;
    @Expose
    public String trackingEndReason;
    @Expose
    public String isTrackingActive;
    @Expose
    public String userEventEndTime;
    @Expose
    public AcceptanceStatus acceptanceStatus;
    @Expose
    public int distanceReminderDistance;
    @Expose
    public String distanceReminderId;
    @Expose
    public ReminderFrom reminderFrom;
    @Expose
    public Boolean isUserLocationShared;
    public ContactOrGroup contactOrGroup;


    public EventParticipant(String userId, String profileName, int distanceReminder, ReminderFrom distanceReminderFrom) {
        this.userId = userId;
        this.profileName = profileName;
        this.distanceReminderDistance = distanceReminder;
        this.reminderFrom = distanceReminderFrom;//0 destination and 1 from current user
    }

    public EventParticipant(String userId, String profileName,
                            AcceptanceStatus eventAcceptanceState) {
        this.userId = userId;
        this.profileName = profileName;
        this.acceptanceStatus = eventAcceptanceState;
    }

    public EventParticipant() {
        this.isUserLocationShared = false;
        this.isTrackingAccepted = false;
        this.acceptanceStatus = AcceptanceStatus.Pending;
    }

    @Override
    public String toString() {
        return "UserList [eventId=" + eventId + ", userId=" + userId
                + ", mobileNumber=" + contactOrGroup.getRegisteredMobileNumber() + ", gCMClientId="
                + gCMClientId
                + ", isTrackingAccepted=" + isTrackingAccepted
                + ", trackingStartTime=" + trackingStartTime
                + ", trackingEndTime=" + trackingEndTime
                + ", trackingEndReason=" + trackingEndReason
                + ", isTrackingActive=" + isTrackingActive
                + ", userEventEndTime=" + userEventEndTime + "]";
    }

    public void setProfileName() {
        if (ParticipantManager.isParticipantCurrentUser(userId)) {
            profileName = AppContext.context.loginName;
            return;
        }
        ContactOrGroup cg = contactOrGroup;
        if (contactOrGroup == null) {
            cg = ContactAndGroupListManager.getContact(userId);
        }
        if (cg != null) {
            profileName = cg.getName();
            return;
        }
        if (profileName == null || profileName == "") {
            profileName = userId.substring(0, 5);
        }
        profileName = "~" + profileName;
    }

}

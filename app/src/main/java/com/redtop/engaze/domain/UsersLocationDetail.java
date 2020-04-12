package com.redtop.engaze.domain;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.BitMapHelper;
import com.redtop.engaze.common.utility.MaterialColor;
import com.redtop.engaze.domain.service.ParticipantService;

public class UsersLocationDetail implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 648767313217529110L;
    @Expose
    public String userId;
    @Expose
    public Double latitude;
    @Expose
    public Double longitude;
    @Expose
    public String eta = "";
    @Expose
    public String arrivalStatus;

    public String isDeleted;
    public String createdOn;
    public ContactOrGroup contactOrGroup;
    public String distance = "";
    public String userName;
    public String currentKnownPlace;
    public String currentAddress;
    public String currentDisplayAddress;
    public AcceptanceStatus acceptanceStatus;
    public Boolean showLocationOnMap = true;
    public int imageID;
    public String dataText;
    //private String distanceReminder;

    public UsersLocationDetail(String userId, Double latitude, Double longitude, String eta,
                               String arrivalStatus) {
        super();
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.eta = eta;
        this.arrivalStatus = arrivalStatus;
    }

    public UsersLocationDetail(String userId, Double latitude,
                               Double longitude, String isDeleted, String createdOn, String eta,
                               String arrivalStatus, String userName) {
        super();
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isDeleted = isDeleted;
        this.createdOn = createdOn;
        this.eta = eta;
        this.arrivalStatus = arrivalStatus;
        this.userName = userName;
    }


    public UsersLocationDetail(int imageID, String dataText, AcceptanceStatus acceptanceStatus) {
        this.imageID = imageID;
        this.dataText = dataText;
        this.acceptanceStatus = acceptanceStatus;
    }


    @Override
    public String toString() {
        return "UsersLocationDetail [userId=" + userId + ", latitude="
                + latitude + ", longitude=" + longitude + ", isDeleted="
                + isDeleted + ", createdOn=" + createdOn + ", eta=" + eta
                + ", arrivalStatus=" + arrivalStatus + ", userName=" + userName
                + "]";
    }

    public static List<UsersLocationDetail> createUserLocationListFromEventMembers(Event event, Context context) {
        ArrayList<EventParticipant> memberList = event.Participants;
        ArrayList<UsersLocationDetail> usersLocationDetailList = new ArrayList<UsersLocationDetail>();
        UsersLocationDetail uld = null;
        for (EventParticipant mem : memberList) {
            if (ParticipantService.isValidForLocationSharing(event, mem)) {
                uld = createUserLocationListFromEventMember(event, mem);
                usersLocationDetailList.add(uld);
            }
        }
        return usersLocationDetailList;
    }

    public static UsersLocationDetail createUserLocationListFromEventMember(Event event, EventParticipant mem) {

        UsersLocationDetail uld = new UsersLocationDetail(mem.getUserId(), null, null, "false", "", "location unavailable", "", mem.getProfileName());
        ContactOrGroup cg = ContactAndGroupListManager.getContact(uld.userId);
        Boolean isParticipantCurrentUser = ParticipantService.isParticipantCurrentUser(mem.getUserId());
        if (cg == null) {
            cg = new ContactOrGroup();
            cg.setIconImageBitmap(ContactOrGroup.getAppUserIconBitmap());
            if (isParticipantCurrentUser || uld.userName.startsWith("~")) {
                cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(MaterialColor.getColor(uld.userName), 40, uld.userName.substring(1, 2).toUpperCase()));
            } else {
                cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(MaterialColor.getColor(uld.userName), 40, uld.userName.substring(0, 1).toUpperCase()));
            }
        } else {
            uld.userName = cg.getName();
        }
        uld.contactOrGroup = cg;
        uld.acceptanceStatus = mem.getAcceptanceStatus();
        if (isParticipantCurrentUser) {
            uld.userName = "You";
        }

        return uld;
    }
}

package com.redtop.engaze.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.manager.ParticipantManager;

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
    @Expose
    public String createdOn;
    @Expose
    public String address;
    @Expose
    public String name;

    @Expose
    public String userName;

    public ContactOrGroup contactOrGroup;
    public String distance = "";
    public String currentKnownPlace;


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

    public static List<UsersLocationDetail> createUserLocationListFromEventMembers(Event event) {
        ArrayList<EventParticipant> memberList = event.participants;
        ArrayList<UsersLocationDetail> usersLocationDetailList = new ArrayList<UsersLocationDetail>();
        UsersLocationDetail uld;
        for (EventParticipant mem : memberList) {
            if (ParticipantManager.isValidForLocationSharing(event, mem)) {
                uld = createUserLocationListFromEventMember(mem);
                usersLocationDetailList.add(uld);
            }
        }
        return usersLocationDetailList;
    }

    public static UsersLocationDetail createUserLocationListFromEventMember(EventParticipant mem) {

        UsersLocationDetail uld = new UsersLocationDetail(mem.userId, null, null, "false", "", "location unavailable", "", mem.profileName);
        uld.userName = mem.profileName;
        uld.contactOrGroup = mem.contactOrGroup;
        uld.acceptanceStatus = mem.acceptanceStatus;
        if (ParticipantManager.isParticipantCurrentUser(mem.userId)) {
            uld.userName = "You";
        }
        return uld;
    }
}

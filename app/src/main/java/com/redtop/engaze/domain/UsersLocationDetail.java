package com.redtop.engaze.domain;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.redtop.engaze.common.ContactAndGroupListManager;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.BitMapService;
import com.redtop.engaze.common.utility.MaterialColor;

public class UsersLocationDetail implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 648767313217529110L;
	private String userId;
	private String latitude;
	private String longitude;
	private String isDeleted;
	private String createdOn;
	private ContactOrGroup cg;
	private String eta ="";
	private String distance ="";
	private String arrivalStatus;
	private String userName;
	public String currentKnownPlace;
	public String currentAddress;
	private String currentDisplayAddress;
	private AcceptanceStatus acceptanceStatus;
	public Boolean showLocationOnMap = true;
	//private String distanceReminder;

	public UsersLocationDetail(String userId, String latitude,
			String longitude, String isDeleted, String createdOn, String eta,
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

	int imageID;
	String dataText;
	
	public UsersLocationDetail(int imageID, String dataText, AcceptanceStatus acceptanceStatus ) {
		this.imageID = imageID;
		this.dataText = dataText;
		this.acceptanceStatus = acceptanceStatus;		
	}	
	
	public String getdataText() {
		return dataText;
	}
	
	public void setdataText(String dataText) {
		this.dataText = dataText;
	}
	public int getimageID() {
		return imageID;
	}
	
	public void setimageID(int imageID) {
		this.imageID = imageID;
	}
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getEta() {
		return eta;
	}

	public void setEta(String eta) {
		this.eta = eta;
	}
	
	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}
	
	public String getCurrentAddress() {
		return currentAddress;
	}

	public void setCurrentAddress(String ca) {
		this.currentAddress = ca;
	}
	
	public String getCurrentDisplayAddress() {
		return currentDisplayAddress;
	}

	public void setCurrentDisplayAddress(String ca) {
		this.currentDisplayAddress = ca;
	}

	public String getArrivalStatus() {
		return arrivalStatus;
	}

	public void setArrivalStatus(String arrivalStatus) {
		this.arrivalStatus = arrivalStatus;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public AcceptanceStatus getAcceptanceStatus() {
		return acceptanceStatus;
	}

	public void setAcceptanceStatus(AcceptanceStatus acceptanceStatus) {
		this.acceptanceStatus = acceptanceStatus;
	}
	
	public ContactOrGroup getContactOrGroup() {
		return cg;
	}
	
	public void setContactOrGroup(ContactOrGroup c) {
		this.cg = c;
	} 
//	public String getDistanceReminder() {
//		return distanceReminder;
//	}
//
//	public void setDistanceReminder(String distanceReminder) {
//		this.distanceReminder = distanceReminder;
//	}
	
	@Override
	public String toString() {
		return "UsersLocationDetail [userId=" + userId + ", latitude="
				+ latitude + ", longitude=" + longitude + ", isDeleted="
				+ isDeleted + ", createdOn=" + createdOn + ", eta=" + eta
				+ ", arrivalStatus=" + arrivalStatus + ", userName=" + userName
				+ "]";
	}

	public static List<UsersLocationDetail> createUserLocationListFromEventMembers(EventDetail event, Context context) {
		ArrayList<EventParticipant> memberList = event.getParticipants();
		ArrayList<UsersLocationDetail> usersLocationDetailList = new ArrayList<UsersLocationDetail>();
		UsersLocationDetail uld = null;
		for (EventParticipant mem : memberList) {
			if (EventParticipant.isValidForLocationSharing(event, mem, context)) {
				uld = createUserLocationListFromEventMember(event, mem, context);
				usersLocationDetailList.add(uld);
			}
		}
		return usersLocationDetailList;
	}

	private static  UsersLocationDetail createUserLocationListFromEventMember(EventDetail event, EventParticipant mem, Context context) {

		UsersLocationDetail uld = new UsersLocationDetail(mem.getUserId(), "", "", "false", "", "location unavailable", "", mem.getProfileName());
		ContactOrGroup cg = ContactAndGroupListManager.getContact(context, uld.getUserId());
		Boolean isParticipantCurrentUser = EventParticipant.isParticipantCurrentUser(mem.getUserId());
		if (cg == null) {
			cg = new ContactOrGroup();
			cg.setIconImageBitmap(ContactOrGroup.getAppUserIconBitmap(context));
			if (isParticipantCurrentUser || uld.getUserName().startsWith("~")) {
				cg.setImageBitmap(BitMapService.generateCircleBitmapForText(context, MaterialColor.getColor(uld.getUserName()), 40, uld.getUserName().substring(1, 2).toUpperCase()));
			} else {
				cg.setImageBitmap(BitMapService.generateCircleBitmapForText(context, MaterialColor.getColor(uld.getUserName()), 40, uld.getUserName().substring(0, 1).toUpperCase()));
			}
		} else {
			uld.setUserName(cg.getName());
		}
		uld.setContactOrGroup(cg);
		uld.setAcceptanceStatus(mem.getAcceptanceStatus());
		if (isParticipantCurrentUser) {
			uld.setUserName("You");
		}

		return uld;
	}
}

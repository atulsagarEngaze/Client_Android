package com.redtop.engaze.domain;

import java.util.ArrayList;

import android.content.Context;

import com.redtop.engaze.common.enums.AcceptanceStatus;

public class EventDetail implements DataModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1602715454105775832L;
	private String eventId;
	private String name;
	private String eventTypeId;
	private String description;
	private String startTime;
	private String endTime;
	private String duration;
	private String initiatorId;	
	private String initiatorName;
	private String[] adminList;
	private String stateId;
	private String trackingStateId;
	private String trackingStopTime;
	private String destinationLatitude;
	private String destinationLongitude;
	private String destinationName;
	private String destinationAddress;
	private String isTrackingRequired;
	private String reminderOffset;
	private String reminderType;
	private String trackingStartOffset;
	private EventParticipant currentParticipant;
	private ArrayList<EventParticipant> participants;
	private ArrayList<EventParticipant>ReminderEnabledMembers;

	private ArrayList<ContactOrGroup>contactOrGroup;
	private ArrayList<UsersLocationDetail> usersLocationDetailList ;
	public ArrayList<Integer> notificationIds;
	public int snoozeNotificationId =0;
	public int acceptNotificationid =0;
	public String isQuickEvent;
	public Boolean isMute = false;
	public Boolean isDistanceReminderSet = false;
	protected String mIsRecurrence="false";
	protected String mRecurrenceType;
	protected String mNumberOfOccurences;
	protected String mNumberOfOccurencesLeft;
	protected String mFrequencyOfOcuurence;
	protected ArrayList<Integer>mRecurrencedays;
	protected String mRecurrenceActualStartTime;


	public EventDetail(String eventId, String name, String eventTypeId,
			String description, String startTime, String endTime,
			String duration, String initiatorId, String initiatorName, 
			String stateId, String trackingStateId, 
			String destinationLatitude, String destinationLongitude,
			String destinationName,String destinationAddress,  String isTrackingRequired,
			String reminderOffset, String reminderType, String trackingStartOffset, ArrayList<ContactOrGroup> contactOrGroups,
			String isQuickEvent) {
		super();
		this.eventId = eventId;
		this.name = name;
		this.eventTypeId = eventTypeId;
		this.description = description;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.initiatorId = initiatorId;
		this.initiatorName = initiatorName;
		this.stateId = stateId;
		this.trackingStateId = trackingStateId;		
		this.destinationLatitude = destinationLatitude;
		this.destinationLongitude = destinationLongitude;
		this.destinationName = destinationName;
		this.destinationAddress = destinationAddress;
		this.isTrackingRequired = isTrackingRequired;
		this.reminderOffset = reminderOffset;
		this.reminderType = reminderType;
		this.trackingStartOffset = trackingStartOffset;
		this.contactOrGroup = contactOrGroups;
		this.isQuickEvent = isQuickEvent;
		this.notificationIds = new ArrayList<Integer>();
	}

	public EventDetail(ArrayList<EventParticipant> members,String eventId, String name, String eventTypeId,
			String description, String startTime, String endTime,
			String duration, String initiatorId, String initiatorName, 
			String stateId, String trackingStateId, 
			String destinationLatitude, String destinationLongitude,
			String destinationName,String destinationAddress, String isTrackingRequired,
			String reminderOffset, String reminderType, String trackingStartOffset,
			String isQuickEvent) {
		super();
		this.eventId = eventId;
		this.name = name;
		this.eventTypeId = eventTypeId;
		this.description = description;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.initiatorId = initiatorId;
		this.initiatorName = initiatorName;
		this.stateId = stateId;
		this.trackingStateId = trackingStateId;		
		this.destinationLatitude = destinationLatitude;
		this.destinationLongitude = destinationLongitude;
		this.destinationName = destinationName;
		this.destinationAddress = destinationAddress;
		this.isTrackingRequired = isTrackingRequired;
		this.reminderOffset = reminderOffset;
		this.reminderType = reminderType;
		this.trackingStartOffset = trackingStartOffset;
		this.participants = members;
		this.isQuickEvent = isQuickEvent;
		this.notificationIds = new ArrayList<Integer>();
	}

	public EventDetail() {
		// TODO Auto-generated constructor stub
		this.notificationIds = new ArrayList<Integer>();
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getIsQuickEvent() {
		return isQuickEvent;
	}

	public void setIsQuickEvent(String isQuickEvent) {
		this.isQuickEvent = isQuickEvent;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEventTypeId() {
		return eventTypeId;
	}

	public void setEventTypeId(String eventTypeId) {
		this.eventTypeId = eventTypeId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getInitiatorId() {
		return initiatorId;
	}

	public void setInitiatorId(String initiatorId) {
		this.initiatorId = initiatorId;
	}

	public String GetInitiatorName(){
		return initiatorName;
	}

	public void SetInitiatorName(String profileName){
		this.initiatorName = profileName;
	}

	public String[] getAdminList() {
		return adminList;
	}

	public void setAdminList(String[] adminList) {
		this.adminList = adminList;
	}

	public String getState() {
		return stateId;
	}

	public void setState(String stateId) {
		this.stateId = stateId;
	}

	public String getTrackingState() {
		return trackingStateId;
	}

	public void setTrackingState(String trackingStateId) {
		this.trackingStateId = trackingStateId;
	}

	public String getTrackingStopTime() {
		return trackingStopTime;
	}

	public void setTrackingStopTime(String trackingStopTime) {
		this.trackingStopTime = trackingStopTime;
	}

	public String getDestinationLatitude() {
		return destinationLatitude;
	}

	public void setDestinationLatitude(String destinationLatitude) {
		this.destinationLatitude = destinationLatitude;
	}

	public String getDestinationLongitude() {
		return destinationLongitude;
	}

	public void setDestinationLongitude(String destinationLongitude) {
		this.destinationLongitude = destinationLongitude;
	}

	public String getDestinationName() {
		return destinationName;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public String getIsTrackingRequired() {
		return isTrackingRequired;
	}

	public void setIsTrackingRequired(String isTrackingRequired) {
		this.isTrackingRequired = isTrackingRequired;
	}

	public String getReminderOffset() {
		return reminderOffset;
	}

	public void setReminderOffset(String reminderOffset) {
		this.reminderOffset = reminderOffset;
	}

	public String getReminderType() {
		return reminderType;
	}

	public void setReminderType(String reminderType) {
		this.reminderType = reminderType;
	}

	public String getTrackingStartOffset() {
		return trackingStartOffset;
	}

	public void setTrackingStartOffset(String trackingStartOffset) {
		this.trackingStartOffset = trackingStartOffset;
	}

	public ArrayList<EventParticipant> getParticipants(){
		return this.participants;
	}

	public void setParticipants(ArrayList<EventParticipant> participants){
		this.participants = participants;
	}

	public ArrayList<EventParticipant> getReminderEnabledMembers(){
		return this.ReminderEnabledMembers;
	}

	public void setReminderEnabledMembers(ArrayList<EventParticipant> ReminderEnabledMembers){
		this.ReminderEnabledMembers =  ReminderEnabledMembers;
	}

	public ArrayList<ContactOrGroup> getContactOrGroup(){
		return this.contactOrGroup;
	}

	public void setContactOrGroup(ArrayList<ContactOrGroup> contactOrGroup){
		this.contactOrGroup =  contactOrGroup;
	}

	public ArrayList<UsersLocationDetail> getUsersLocationDetailList(){
		return this.usersLocationDetailList;
	}

	public void setUsersLocationDetailList(ArrayList<UsersLocationDetail> usersLocationDetailList){
		this.usersLocationDetailList =  usersLocationDetailList;
	}

	///recursive information
	public ArrayList<Integer> getRecurrenceDays(){
		return this.mRecurrencedays;
	}

	public void setRecurrenceDays(ArrayList<Integer> recurrencedays){
		this.mRecurrencedays =  recurrencedays;
	}

	public String getIsRecurrence() {
		return mIsRecurrence;
	}

	public void setIsRecurrence(String isRecurrence) {
		this.mIsRecurrence = isRecurrence;
	}

	public String getRecurrenceType() {
		return mRecurrenceType;
	}

	public void setRecurrenceType(String recurrenceType) {
		this.mRecurrenceType = recurrenceType;
	}

	public String getNumberOfOccurences() {
		return mNumberOfOccurences;
	}

	public void setNumberOfOccurences(String numberOfOccurences) {
		this.mNumberOfOccurences = numberOfOccurences;
	}	

	public String getNumberOfOccurencesLeft() {
		return mNumberOfOccurencesLeft;
	}

	public void setNumberOfOccurencesLeft(String numberOfOccurencesLeft) {
		this.mNumberOfOccurencesLeft = numberOfOccurencesLeft;
	}	

	public String getFrequencyOfOccurence() {
		return mFrequencyOfOcuurence;
	}

	public void setFrequencyOfOcuurence(String frequencyOfOcurrence) {
		this.mFrequencyOfOcuurence = frequencyOfOcurrence;
	}

	public String getRecurrenceActualStartTime() {
		return mRecurrenceActualStartTime;
	}

	public void setRecurrenceActualStartTime(String recurrenceActualStartTime) {
		this.mRecurrenceActualStartTime = recurrenceActualStartTime;
	}

	public EventParticipant getMember(String userId){

		EventParticipant member = null;
		if (this.participants !=null && this.participants.size()>0)
		{
			for (EventParticipant mem : this.participants) {
				if(mem.getUserId().equalsIgnoreCase(userId.toLowerCase()))	{
					member = mem;
					break;
				}
			}
		}
		return member;		
	}

	@SuppressWarnings("null")
	public ArrayList<EventParticipant> getMembersbyStatus(AcceptanceStatus acceptanceStatus){

		ArrayList<EventParticipant> memStatus = new ArrayList<EventParticipant>();

		if (this.participants !=null && this.participants.size()>0)
		{
			for (EventParticipant mem : this.participants) {
				if(mem.getAcceptanceStatus().name().equals(acceptanceStatus.toString()))	{
					memStatus.add(mem);
				}
			}		
		}
		return memStatus;	
	}

	@SuppressWarnings("null")


	public EventParticipant getCurrentParticipant() {
		return this.currentParticipant;
	}

	public void setCurrentParticipant(EventParticipant currentMem) {
		this.currentParticipant = currentMem;
	}

	public int getMemberCount(){
		if(this.participants !=null)
		{
			return this.participants.size();
		}
		else{
			return 0;
		}
	}	
}

package com.redtop.engaze.common.enums;

public enum Action {
    POKEALL(1), LEAVEEVENT(2),
    ENDEVENT(3)	, EXTENDEVENTENDTIME(4),
    ADDREMOVEPARTICIPANTS(5), CHANGEDESTINATION(6),
    SAVEUSERRESPONSE(7), REFRESHEVENTLIST(8),
    DELETEEVENT(9),	SAVEEVENT(10),
    SAVEPROFILE(11), SAVEEVENTSHAREMYLOCATION(12),
    SAVEEVENTTRACKBUDDY(13), UPDATEEVENTWITHPARTICIPANTRESPONSE(14),
    UPDATEEVENTWITHPARTICIPANTLEFT(15),	EVENTENDEDBYINITIATOR(16),
    EVENTEXTENDEDBYINITIATOR(17),EVENTDELETEDBYINITIATOR(18),
    PARTICIPANTSUPDATEDBYINITIATOR(19), CURRENTPARTICIPANTREMOVEDBYINITIATOR(20),
    EVENTDESTINATIONCHANGEDBYINITIATOR(21), GETEVENTDATAFROMSERVER(22),
    REMOVEBUDDYFROMSHARING(23), POKEPARTICIPANT(24), SETTIMEBASEDALERT(25);
    private final int actionId;
    private Action(int actionId){
        this.actionId = actionId;
    }

    public int getAction(){
        return this.actionId;
    }

    public static Action getAction(int actionId){
        switch(actionId){
            case 1:
                return Action.POKEALL;
            case 2:
                return Action.LEAVEEVENT;
            case 3:
                return Action.ENDEVENT;
            case 4:
                return Action.EXTENDEVENTENDTIME;
            case 5:
                return Action.ADDREMOVEPARTICIPANTS;
            case 6:
                return Action.CHANGEDESTINATION;
            case 7:
                return Action.SAVEUSERRESPONSE;
            case 8:
                return Action.REFRESHEVENTLIST;
            case 9:
                return Action.DELETEEVENT;
            case 10:
                return Action.SAVEEVENT;
            case 11:
                return Action.SAVEPROFILE;
            case 12:
                return Action.SAVEEVENTSHAREMYLOCATION;
            case 13:
                return Action.SAVEEVENTTRACKBUDDY;
            case 14:
                return Action.UPDATEEVENTWITHPARTICIPANTRESPONSE;
            case 15:
                return Action.UPDATEEVENTWITHPARTICIPANTLEFT;
            case 16:
                return Action.EVENTENDEDBYINITIATOR;
            case 17:
                return Action.EVENTEXTENDEDBYINITIATOR;
            case 18:
                return Action.EVENTDELETEDBYINITIATOR;
            case 19:
                return Action.PARTICIPANTSUPDATEDBYINITIATOR;
            case 20:
                return Action.CURRENTPARTICIPANTREMOVEDBYINITIATOR;
            case 21:
                return Action.EVENTDESTINATIONCHANGEDBYINITIATOR;
            case 22:
                return Action.GETEVENTDATAFROMSERVER;
            case 23:
                return Action.REMOVEBUDDYFROMSHARING;
            case 24:
                return Action.POKEPARTICIPANT;
            case 25:
                return Action.SETTIMEBASEDALERT;
            default :
                return Action.POKEALL;
        }
    }
}

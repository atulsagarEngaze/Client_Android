package com.redtop.engaze.domain.service;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.redtop.engaze.Interface.IActionHandler;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.ProgressBar;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.manager.ParticipantManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ParticipantService {

    private final static String TAG = ParticipantService.class.getName();


    public static void pokeParticipant(final String userId, String userName, final String eventId, IActionHandler actionHadler) {
        try {
            String lastPokedTime = PreffManager.getPref(userId);
            if (lastPokedTime != null) {
                SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Calendar lastCal = Calendar.getInstance();
                Date lastpokeDate = originalformat.parse(lastPokedTime);
                lastCal.setTime(lastpokeDate);
                long diff = (Calendar.getInstance().getTimeInMillis() - lastCal.getTimeInMillis()) / 60000;
                long pendingfrPoke = Constants.POKE_INTERVAL - diff;
                if (diff >= Constants.POKE_INTERVAL) {
                    pokeAlert(userId, userName, eventId, actionHadler);
                } else {
                    Toast.makeText(AppContext.context,
                            AppContext.context.getResources().getString(R.string.message_runningEvent_pokeInterval) + pendingfrPoke + " minutes.",
                            Toast.LENGTH_LONG).show();
                    actionHadler.actionCancelled(Action.POKEPARTICIPANT);
                }
            } else {
                pokeAlert(userId, userName, eventId, actionHadler);
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void pokeAlert(final String userId, String userName, final String eventId, final IActionHandler actionHadler) {
        AlertDialog.Builder adb = null;
        adb = new AlertDialog.Builder(AppContext.context);

        adb.setTitle("Poke");
        adb.setMessage("Do you want to poke " + userName + "?" + "\n" + "You can poke again only after 15 minutes.");
        adb.setIcon(android.R.drawable.ic_dialog_alert);

        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Call Poke API
                pokeParticipants(userId, eventId, actionHadler);
            }
        });

        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                actionHadler.actionCancelled(Action.POKEPARTICIPANT);
            }
        });
        adb.show();
    }

    public static void pokeParticipants(final String userId, String eventId, final IActionHandler actionHadler) {
        JSONObject jobj = new JSONObject();
        String[] userList = {userId};
        JSONArray mJSONArray = new JSONArray(Arrays.asList(userList));
        Event ed = InternalCaching.getEventFromCache(eventId);
        try {
            ProgressBar.showProgressBar(AppContext.context.getString(R.string.message_general_progressDialog));
            jobj.put("RequestorId", AppContext.context.loginId);
            jobj.put("RequestorName", AppContext.context.loginId);
            jobj.put("UserIdsForRemind", mJSONArray);
            jobj.put("EventName", ed.name);
            jobj.put("EventId", ed.eventId);

            ParticipantManager.pokeParticipants(jobj, new OnActionCompleteListner() {

                @Override
                public void actionComplete(Action action) {
                    SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date currentdate = Calendar.getInstance().getTime();
                    String currentTimestamp = originalformat.format(currentdate);
                    PreffManager.setPref(userId, currentTimestamp);
                    actionHadler.actionComplete(Action.POKEPARTICIPANT);
                }
            }, new OnActionFailedListner() {

                @Override
                public void actionFailed(String msg, Action action) {
                    actionHadler.actionFailed(msg, Action.POKEPARTICIPANT);
                }
            });

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            actionHadler.actionFailed(null, Action.POKEPARTICIPANT);
        }
    }

    public static Boolean isNotifyUser(Event event) {
        if (event != null && event.getCurrentParticipant().getAcceptanceStatus() == AcceptanceStatus.DECLINED) {
            return false;
        }
        return true;
    }

    public static ArrayList<EventParticipant> parseMemberList(JSONArray jsonStr) {
        EventParticipant mem = null;
        ArrayList<EventParticipant> list = new ArrayList<EventParticipant>();
        try {
            for (int i = 0; i < jsonStr.length(); i++) {
                JSONObject c = jsonStr.getJSONObject(i);

                mem = new EventParticipant(
                        AppUtility.convertNullToEmptyString(c.getString("UserId")),
                        AppUtility.convertNullToEmptyString(c.getString("ProfileName")),
                        AppUtility.convertNullToEmptyString(c.getString("MobileNumber")),
                        AcceptanceStatus.getStatus(c.getInt("EventAcceptanceStateId"))
                );
                ContactOrGroup cg = ContactAndGroupListManager.getContact(c.getString("UserId"));
                if (cg != null) {
                    mem.setProfileName(cg.getName());
                    mem.setContact(cg);
                } else {
                    mem.setProfileName("~" + mem.getProfileName());
                }
                mem.isUserLocationShared = c.getBoolean("IsUserLocationShared");
                list.add(mem);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;

    }

    public static boolean isCurrentUserInitiator(String initiatorId) {
        if (AppContext.context.loginId.equalsIgnoreCase(initiatorId)) {
            return true;
        }
        return false;
    }

    public static boolean isParticipantCurrentUser(String userId) {
        if (AppContext.context.loginId.equalsIgnoreCase(userId)) {
            return true;
        }
        // TODO Auto-generated method stub
        return false;
    }

    public static boolean setCurrentParticipant(Event event) {
        for (EventParticipant participant : event.participants) {
            if (participant.getUserId() == AppContext.context.loginId) {
                event.setCurrentParticipant(participant);
                return true;
            }
        }
        return false;
    }

    public static void attacheContactGroupToParticipants(Event event) {
        for (EventParticipant participant : event.participants) {
            ContactOrGroup cg = ContactAndGroupListManager.getContact(participant.getUserId());
            if (cg != null) {
                participant.setProfileName(cg.getName());
                participant.setContact(cg);
            } else {
                participant.setProfileName("~" + participant.getProfileName());
            }
        }
    }

    public static ArrayList<EventParticipant> getMembersbyStatusForLocationSharing(Event event, AcceptanceStatus acceptanceStatus) {

        ArrayList<EventParticipant> memStatus = new ArrayList<EventParticipant>();
        ArrayList<EventParticipant> participants = event.participants;
        if (participants != null && participants.size() > 0) {
            for (EventParticipant mem : participants) {
                if (isValidForLocationSharing(event, mem)) {
                    if (mem.getAcceptanceStatus().name().equals(acceptanceStatus.toString())) {
                        memStatus.add(mem);
                    }
                }
            }
        }
        return memStatus;
    }

    public static Boolean isValidForLocationSharing(Event event, EventParticipant mem) {
        Boolean isValid = true;

        Boolean isCurrentUserInitiator = ParticipantService.isCurrentUserInitiator(event.initiatorId);
        if (event.eventType == EventType.TRACKBUDDY &&
                isCurrentUserInitiator &&
                isParticipantCurrentUser(mem.getUserId())
        ) {
            isValid = false;
        }

        if (event.eventType == EventType.SHAREMYLOACTION &&
                !isCurrentUserInitiator &&
                !mem.getUserId().equalsIgnoreCase(event.initiatorId)) {
            isValid = false;
        }
        return isValid;
    }

    public static JSONObject createUpdateParticipantsJSON(ArrayList<ContactOrGroup> contactsAndgroups, String eventId) {
        JSONObject userListJobj;
        JSONObject jobj = new JSONObject();
        JSONArray jsonarr = new JSONArray();

        String userId;
        try {
            if (contactsAndgroups != null) {
                for (ContactOrGroup cg : contactsAndgroups) {
                    userId = cg.getUserId();
                    userListJobj = new JSONObject();
                    if (userId != null && !userId.isEmpty()) {
                        userListJobj.put("UserId", userId);

                    } else {
                        userListJobj.put("MobileNumber", cg.getNumbers().get(0));
                    }
                    jsonarr.put(userListJobj);
                }
            }

            jobj.put("EventId", eventId);
            jobj.put("UserList", jsonarr);
            jobj.put("RequestorId", AppContext.context.loginId);
        } catch (Exception e) {
            // TODO: handle exception
        }

        return jobj;
    }

    public static void updateUserListWithLocation(ArrayList<UsersLocationDetail> userLocationsFromServer, List<UsersLocationDetail> userLocationList, LatLng destinationLatLang) {
        try {
            Location destLoc = null;
            if (destinationLatLang != null) {
                destLoc = new Location("");
                destLoc.setLatitude(destinationLatLang.latitude);//your coords of course
                destLoc.setLongitude(destinationLatLang.longitude);
            }

            for (UsersLocationDetail userLocation : userLocationsFromServer)

                for (UsersLocationDetail ud : userLocationList) {
                    //for testing
                    ud.acceptanceStatus = AcceptanceStatus.ACCEPTED;
                    if (ud.userId.equalsIgnoreCase(userLocation.userId)) {
                        ud.latitude = userLocation.latitude;
                        ud.longitude = userLocation.longitude;
                        ud.createdOn = DateUtil.convertUtcToLocalDateTime(userLocation.createdOn, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
                        ud.eta = userLocation.eta;
                        ud.arrivalStatus = userLocation.arrivalStatus;
                        if (userLocation.currentAddress != null && userLocation.currentAddress != "") {
                            ud.currentAddress = userLocation.currentAddress;
                            ud.currentDisplayAddress = buildCurrentDisplayAddress(userLocation.currentAddress);
                            if (destLoc != null) {
                                Location loc = new Location("");//provider name is unecessary
                                loc.setLatitude(userLocation.latitude);//your coords of course
                                loc.setLongitude(userLocation.longitude);
                                if (loc.distanceTo(destLoc) <= Constants.DESTINATION_RADIUS) {
                                    ud.currentAddress = "at destination";
                                    ud.currentDisplayAddress = "at destination";
                                }
                            }
                        }
                        break;
                    }


                }
        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    private static String buildCurrentDisplayAddress(String currentAddress) {

        String currentDisplayAddress = "";
        if (currentAddress == null || currentAddress.equals("")) {
            return currentDisplayAddress;
        }

        String[] arrAddress = currentAddress.split(",");

        if (arrAddress.length > 1) {

            List<String> addressLines = new ArrayList<String>(Arrays.asList(arrAddress));
            addressLines.remove(0);
            StringBuilder builder = new StringBuilder();
            builder.append(addressLines.get(0));
            addressLines.remove(0);
            for (String addressLine : addressLines) {
                builder.append(", " + addressLine);
            }
            currentDisplayAddress = builder.toString();
        } else {
            currentDisplayAddress = currentAddress;
        }

        return currentDisplayAddress;
    }

}

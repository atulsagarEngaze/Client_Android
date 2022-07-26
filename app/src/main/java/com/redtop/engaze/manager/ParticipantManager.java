package com.redtop.engaze.manager;

import android.app.AlertDialog;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.redtop.engaze.Interface.IActionHandler;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.common.utility.BitMapHelper;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.common.utility.MaterialColor;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.utility.ProgressBar;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.restApi.ParticipantApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ParticipantManager {
    private final static String TAG = ParticipantManager.class.getName();

    private final static ParticipantApi participantApi = new ParticipantApi();

    public static void pokeParticipants(JSONObject pokeParticipantsJSON,
                                        final OnActionCompleteListner onActionCompleteListner,
                                        final OnActionFailedListner onActionFailedListner) {
        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            onActionFailedListner.actionFailed(message, Action.POKEALL);
            return;
        }

        participantApi.pokeParticipants(pokeParticipantsJSON, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                Log.d(TAG, "PokeAllResponse:" + response.toString());

                try {

                    onActionCompleteListner.actionComplete(Action.POKEALL);

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    onActionFailedListner.actionFailed(null, Action.POKEALL);
                }

            }

            @Override
            public void apiCallFailure() {
                onActionFailedListner.actionFailed(null, Action.POKEALL);
            }
        });
    }

    public static void addRemoveParticipants(final ArrayList<ContactOrGroup>contactorGroupList, final Event event, final OnActionCompleteListner listenerOnSuccess, final OnActionFailedListner listenerOnFailure) {
        String message = "";
        if (!AppContext.context.isInternetEnabled) {
            message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listenerOnFailure.actionFailed(message, Action.ADDREMOVEPARTICIPANTS);
            return;
        }
        JSONArray newParticipantJArry = createUpdateParticipantsJSON(contactorGroupList);
        newParticipantJArry.put(event.getCurrentParticipant().userId);

        participantApi.addRemoveParticipants(newParticipantJArry,  event.eventId, new OnAPICallCompleteListener<String>() {

            @Override
            public void apiCallSuccess(String response) {
                Log.d(TAG, "EventResponse:" + response);
                try {
                    ArrayList<ContactOrGroup> tempCGList = new ArrayList<>(contactorGroupList);
                    event.ContactOrGroups = contactorGroupList;
                    ArrayList<EventParticipant> existingParticipantList = new ArrayList<>();

                    for (EventParticipant participant : event.participants) {
                        for (ContactOrGroup cg : event.ContactOrGroups) {
                            if(cg.userId.equals(participant.userId)){
                                existingParticipantList.add(participant);
                                tempCGList.remove(cg);
                                break;
                            }
                        }
                    }
                    existingParticipantList.add(event.getCurrentParticipant());
                    event.participants = existingParticipantList;
                    ArrayList<EventParticipant> newParticipantList =
                            CreateParticipantListFromContactGroupLst(tempCGList);
                    for(EventParticipant newParticipant :newParticipantList ){
                        newParticipant.acceptanceStatus = AcceptanceStatus.Pending;
                        event.participants.add(newParticipant);
                    }
                    InternalCaching.saveEventToCache(event);
                    listenerOnSuccess.actionComplete(Action.ADDREMOVEPARTICIPANTS);

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listenerOnFailure.actionFailed(null, Action.ADDREMOVEPARTICIPANTS);
                }

            }

            @Override
            public void apiCallFailure() {
                listenerOnFailure.actionFailed(null, Action.ADDREMOVEPARTICIPANTS);

            }
        });

    }

    public static ArrayList<EventParticipant>CreateParticipantListFromContactGroupLst(ArrayList<ContactOrGroup> cgList){
        ArrayList<EventParticipant> participants =  new ArrayList<>();
        EventParticipant participant;
        for (ContactOrGroup cg : cgList) {
            participant = new EventParticipant();
            participant.userId = cg.getUserId();
            participant.profileName = cg.getName();
            participant.contactOrGroup = cg;
            participants.add(participant);

        }

        return  participants;
    }

    public static void setContactsGroup(ArrayList<EventParticipant> eventMembers) {
        HashMap<String, ContactOrGroup> registeredList = InternalCaching.getRegisteredContactListFromCache();

        for (EventParticipant mem : eventMembers) {
            if (mem.contactOrGroup == null) {
                mem.contactOrGroup = registeredList.get(mem.userId);
            }
            if (mem.contactOrGroup == null) {
                ContactOrGroup cg = new ContactOrGroup();
                cg.setIconImageBitmap(ContactOrGroup.getAppUserIconBitmap());
                if (isParticipantCurrentUser(mem.userId) || mem.profileName.startsWith("~")) {
                    cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(MaterialColor.getColor(mem.profileName), 40, mem.profileName.substring(1, 2).toUpperCase()));
                } else {
                    cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(MaterialColor.getColor(mem.profileName), 40, mem.profileName.substring(0, 1).toUpperCase()));
                }

                mem.contactOrGroup = cg;
            }
        }
    }

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

    public static void pokeAlert( final String userId, String userName, final String eventId, final IActionHandler actionHadler) {
        AlertDialog.Builder adb = null;
        adb = new AlertDialog.Builder(AppContext.context.currentActivity);

        adb.setTitle("Poke");
        adb.setMessage("Do you want to poke " + userName + "?" + "\n" + "You can poke again only after 15 minutes.");
        adb.setIcon(android.R.drawable.ic_dialog_alert);

        adb.setPositiveButton("OK", (dialog, which) -> {
            //Call Poke API
            pokeParticipants(userId, eventId, actionHadler);
        });

        adb.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            actionHadler.actionCancelled(Action.POKEPARTICIPANT);
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

            pokeParticipants(jobj, action -> {
                SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date currentdate = Calendar.getInstance().getTime();
                String currentTimestamp = originalformat.format(currentdate);
                PreffManager.setPref(userId, currentTimestamp);
                actionHadler.actionComplete(Action.POKEPARTICIPANT);
            }, (msg, action) -> actionHadler.actionFailed(msg, Action.POKEPARTICIPANT));

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            actionHadler.actionFailed(null, Action.POKEPARTICIPANT);
        }
    }

    public static Boolean isNotifyUser(Event event) {
        if (event != null && event.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.Rejected) {
            return false;
        }
        return true;
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
            if (participant.userId.equals(AppContext.context.loginId)) {
                event.setCurrentParticipant(participant);
                return true;
            }
        }
        return false;
    }

    public static ArrayList<EventParticipant> getMembersbyStatusForLocationSharing(Event event, AcceptanceStatus acceptanceStatus) {

        ArrayList<EventParticipant> memStatus = new ArrayList<EventParticipant>();
        ArrayList<EventParticipant> participants = event.participants;
        if (participants != null && participants.size() > 0) {
            for (EventParticipant mem : participants) {
                if (isValidForLocationSharing(event, mem)) {
                    if (mem.acceptanceStatus.name().equals(acceptanceStatus.toString())) {
                        memStatus.add(mem);
                    }
                }
            }
        }
        return memStatus;
    }

    public static Boolean isValidForLocationSharing(Event event, EventParticipant mem) {
        Boolean isValid = true;

        Boolean isCurrentUserInitiator = isCurrentUserInitiator(event.initiatorId);
        if (event.eventType == EventType.TRACKBUDDY &&
                isCurrentUserInitiator &&
                isParticipantCurrentUser(mem.userId)
        ) {
            isValid = false;
        }

        if (event.eventType == EventType.SHAREMYLOACTION &&
                !isCurrentUserInitiator &&
                !event.initiatorId.equalsIgnoreCase(mem.userId)) {
            isValid = false;
        }
        return isValid;
    }

    public static JSONArray createUpdateParticipantsJSON(ArrayList<ContactOrGroup> contactsAndgroups) {

        JSONArray jsonarr = new JSONArray();

        String userId;
        try {
            if (contactsAndgroups != null) {
                for (ContactOrGroup cg : contactsAndgroups) {
                    userId = cg.getUserId();

                    if (userId != null && !userId.isEmpty()) {
                        jsonarr.put(userId);

                    } else {
                        jsonarr.put(cg.getNumbers().get(0));
                    }
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
        }

        return jsonarr;
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
                    if (ud.userId != null && ud.userId.equalsIgnoreCase(userLocation.userId)) {
                        ud.latitude = userLocation.latitude;
                        ud.longitude = userLocation.longitude;
                        ud.createdOn = DateUtil.convertUtcToLocalDateTime(userLocation.createdOn, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
                        ud.eta = userLocation.eta;
                        ud.arrivalStatus = userLocation.arrivalStatus;
                        if (userLocation.address != null && userLocation.address != "") {
                            ud.address = userLocation.address;
                            ud.name = buildCurrentDisplayAddress(userLocation.address);
                            if (destLoc != null) {
                                Location loc = new Location("");//provider name is unecessary
                                loc.setLatitude(userLocation.latitude);//your coords of course
                                loc.setLongitude(userLocation.longitude);
                                if (loc.distanceTo(destLoc) <= Constants.DESTINATION_RADIUS) {
                                    ud.address = "at destination";
                                    ud.name = "at destination";
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

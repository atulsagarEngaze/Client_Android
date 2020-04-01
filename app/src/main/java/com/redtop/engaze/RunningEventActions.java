package com.redtop.engaze;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.maps.model.LatLng;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;
import com.redtop.engaze.common.PreffManager;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.constant.DurationConstants;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.domain.manager.ParticipantManager;
import com.redtop.engaze.domain.service.EventParser;
import com.redtop.engaze.domain.service.EventService;

import androidx.appcompat.app.AlertDialog;

@SuppressLint({"ResourceAsColor", "SimpleDateFormat"})
public class RunningEventActions extends RunningEventActivityResults {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        super.initialize(savedInstanceState);
    }

    public void onEventParticipantUpdatedByInitiator() {
        mEvent = InternalCaching.getEventFromCache(mEventId);
        ContactAndGroupListManager.assignContactsToEventMembers(mEvent.getParticipants());
        updateRecyclerViews();
        if (EventService.isEventTrackBuddyEventForCurrentuser(mEvent)) {
            runningEventAlertDialog("Tracking Updated!", mEvent.GetInitiatorName() + " has updated the participants list.", true);
        } else {
            runningEventAlertDialog("Event Updated!", mEvent.getName() + ": " + mEvent.GetInitiatorName() + " has updated the participants list.", true);
        }
    }

    public void onUserRemovedFromEventByInitiator() {
        if (isActivityRunning && !((Activity) mContext).isFinishing()) {
            if (EventService.isEventTrackBuddyEventForCurrentuser(mEvent)) {
                runningEventAlertDialog("Removed from Tracking!", mEvent.GetInitiatorName() + " has removed you from this tracking event.", false);
            } else {
                runningEventAlertDialog("Removed from Event!", mEvent.getName() + ": " + mEvent.GetInitiatorName() + " has removed you from this event.", false);
            }
        }
        locationhandler.removeCallbacks(locationRunnable);
        mEvent = null;
    }

    public void onEventDestinationUpdatedByInitiator(String changedDestination) {
        mEvent = InternalCaching.getEventFromCache(mEventId);
        ContactAndGroupListManager.assignContactsToEventMembers(mEvent.getParticipants());
        if (!mEvent.getDestinationLatitude().equals("null")) {
            mDestinationlatlang = new LatLng(Double.parseDouble(mEvent.getDestinationLatitude()), Double.parseDouble(mEvent.getDestinationLongitude()));
        }
        removeRoute();
        createDestinationMarker();
        mEnableAutoCameraAdjust = true;
        showAllMarkers();
        if (EventService.isEventTrackBuddyEventForCurrentuser(mEvent)) {
            runningEventAlertDialog("Tracking Destination Changed!", mEvent.GetInitiatorName() + " has changed tracking Destination to " + changedDestination, true);
        } else {
            runningEventAlertDialog("Event Destination Changed!", mEvent.getName() + ": " + mEvent.GetInitiatorName() + " has changed this events Destination to " + changedDestination, true);
        }

    }

    public void onEventEndedByInitiator() {
        if (isActivityRunning && !((Activity) mContext).isFinishing()) {
            if (EventService.isEventTrackBuddyEventForCurrentuser(mEvent)) {
                runningEventAlertDialog("Tracking ended!", mEvent.GetInitiatorName() + " has stopped sharing location", false);
            } else {

                runningEventAlertDialog("Event ended!", mEvent.getName() + " ended by " + mEvent.GetInitiatorName(), false);
            }
        }
        locationhandler.removeCallbacks(locationRunnable);
        mEvent = null;
    }

    public void onEventOver() {
        if (isActivityRunning && !((Activity) mContext).isFinishing()) {
            if (EventService.isEventTrackBuddyEventForCurrentuser(mEvent)) {
                runningEventAlertDialog("Tracking Over!", "Tracking ended at " + DateUtil.getTimeInHHMMa(mEvent.getEndTime(), "yyyy-MM-dd'T'HH:mm:ss"), false);
            } else {
                runningEventAlertDialog("Event Over!", mEvent.getName() + " finished at " + DateUtil.getTimeInHHMMa(mEvent.getEndTime(), "yyyy-MM-dd'T'HH:mm:ss"), false);
            }
        }
        locationhandler.removeCallbacks(locationRunnable);
        mEvent = null;
    }

    public void onParticipantLeft(String EventResponderName) {
        mEvent = InternalCaching.getEventFromCache(mEventId);
        ContactAndGroupListManager.assignContactsToEventMembers(mEvent.getParticipants());
        if (mEvent != null) {//incase event is already over
            updateRecyclerViews();
            arrangeListinAvailabilityOrder();
            String alertmsg = "";
            if (EventService.isEventTrackBuddyEventForCurrentuser(mEvent)) {
                alertmsg = EventResponderName + " has left stopped sharing location";
            } else {
                alertmsg = EventResponderName + " has left " + mEvent.getName();
            }
            runningEventAlertDialog("Response Received!", alertmsg, true);
        }
    }

    public void onUserResponse(int eventAcceptanceStateId, String eventResponderName) {
        mEvent = InternalCaching.getEventFromCache(mEventId);
        ContactAndGroupListManager.assignContactsToEventMembers(mEvent.getParticipants());
        if (mEvent != null) {//incase event is already over
            updateRecyclerViews();
            arrangeListinAvailabilityOrder();
            String alertmsg = "";

            if (eventAcceptanceStateId != -1) {
                if (AcceptanceStatus.getStatus(eventAcceptanceStateId) == AcceptanceStatus.ACCEPTED) {
                    if (EventService.isEventTrackBuddyEventForCurrentuser(mEvent)) {
                        alertmsg = eventResponderName + " has accepted your tracking request";
                    } else {
                        alertmsg = eventResponderName + " has accepted " + mEvent.getName();
                    }
                } else {
                    if (EventService.isEventTrackBuddyEventForCurrentuser(mEvent)) {
                        alertmsg = eventResponderName + " has rejected your tracking request";
                    } else {
                        alertmsg = eventResponderName + " has rejected " + mEvent.getName();
                    }
                }
                runningEventAlertDialog("Response Received!", alertmsg, true);
            }
        }
    }

    public void onEventExtendedByInitiator(String extendEventDuration) {
        mEvent = InternalCaching.getEventFromCache(mEventId);
        ContactAndGroupListManager.assignContactsToEventMembers(mEvent.getParticipants());
        UpdateTimeLeftItemOfRunningEventDetailsDataSet();
        mEventDetailAdapter.notifyDataSetChanged();

        if (EventService.isEventTrackBuddyEventForCurrentuser(mEvent)) {
            runningEventAlertDialog("Tracking Extended!", mEvent.GetInitiatorName() + " has extended tracking by " + extendEventDuration + " minutes.", true);
        } else {
            runningEventAlertDialog("Event Extended!", mEvent.getName() + ": " + mEvent.GetInitiatorName() + " has extended this event by " + extendEventDuration + " minutes.", true);
        }
    }

    public void onEventEndClicked() {
        AlertDialog.Builder adb;
        locationhandler.removeCallbacks(locationRunnable);
        adb = new AlertDialog.Builder(this);
        // adb.setView(alertDialogView);

        adb.setTitle("End Event");
        adb.setMessage(getResources().getString(R.string.message_runningEvent_eventEndConfirmation));
        adb.setIcon(android.R.drawable.ic_dialog_alert);

        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                endEventActions();
            }
        });

        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                locationhandler.postDelayed(locationRunnable, DurationConstants.LOCATION_RETRIVAL_INTERVAL);
            }
        });
        adb.show();
    }

    public void onShareOnFaceBookClicked() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
    }

    public void onPokeAllParticipantsClicked() {
        try {
            String lastPokedTime = PreffManager.getPref(mEventId);
            if (lastPokedTime != null) {
                SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Calendar lastCal = Calendar.getInstance();
                Date lastPokeDate = originalformat.parse(lastPokedTime);
                lastCal.setTime(lastPokeDate);
                long diff = (Calendar.getInstance().getTimeInMillis() - lastCal.getTimeInMillis()) / 60000;
                long pendingfrPoke = Constants.POKE_INTERVAL - diff;
                if (diff >= Constants.POKE_INTERVAL) {
                    pokeAll();
                } else {
                    Toast.makeText(mContext,
                            getResources().getString(R.string.message_runningEvent_pokeAllInterval) + pendingfrPoke + " minutes.",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                pokeAll();
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void onLeaveEventClicked() {
        locationhandler.removeCallbacks(locationRunnable);

        EventManager.leaveEvent(mEvent, new OnActionCompleteListner() {

            @Override
            public void actionComplete(Action action) {
                mEvent.getCurrentParticipant().setAcceptanceStatus(AcceptanceStatus.DECLINED);
                ((ActionSuccessFailMessageActivity) mContext).actionComplete(action);
                gotoPreviousPage();

            }
        }, this);
    }

    public void onChangeEventDestinationClicked() {
        Intent intent;
        shouldExecuteOnResume = false;
        if (!(mEvent.getDestinationLatitude().equals("null") || mEvent.getDestinationLatitude() == null || mEvent.getDestinationLatitude().equals(""))) {
            mDestinationPlace = new EventPlace(mEvent.getDestinationName(), mEvent.getDestinationAddress(), new LatLng(Double.parseDouble(mEvent.getDestinationLatitude()), Double.parseDouble(mEvent.getDestinationLongitude())));
            //mLh.displayPlace( mDestinationPlace, mEventLocationTextView );
        }
        intent = new Intent(RunningEventActions.this, PickLocationActivity.class);
        if (mDestinationPlace != null) {
            intent.putExtra("DestinatonLocation", (Parcelable) mDestinationPlace);
        }
        startActivityForResult(intent, UPDATE_LOCATION_REQUEST_CODE);
    }

    public void onEditParticipantsClicked() {
        shouldExecuteOnResume = false;
        ArrayList<ContactOrGroup> contactList = new ArrayList<ContactOrGroup>();
        String currentMemUserId = mEvent.getCurrentParticipant().getUserId();
        ArrayList<EventParticipant> members = mEvent.getParticipants();
        for (EventParticipant mem : members) {
            if (!mem.getUserId().equals(currentMemUserId))
                contactList.add(ContactAndGroupListManager.getContact(mem.getUserId()));
        }

        PreffManager.setPrefArrayList("Invitees", contactList);

        Intent i = new Intent(RunningEventActions.this, ContactsListActivity.class);
        startActivityForResult(i, ADDREMOVE_INVITEES_REQUEST_CODE);
    }

    public void onEventExtendedClicked() {
        Intent intent;
        shouldExecuteOnResume = false;
        intent = new Intent(RunningEventActions.this, SnoozeOffset.class);
        startActivityForResult(intent, SNOOZING_REQUEST_CODE);
    }

    public void onTrafficButtonClicked() {
        if (mIsTrafficOn) {
            mMap.setTrafficEnabled(false);
            mIsTrafficOn = false;
            viewManager.setTrafficButtonOff();
        } else {
            mMap.setTrafficEnabled(true);
            mIsTrafficOn = true;
            viewManager.setTrafficButtonOn();
        }
    }

    public void onEtaDistanceButtonClicked() {
        if (mIsETAOn) {
            mIsETAOn = false;
            viewManager.setEtaButtonOff();
            removeEtaMarkers();
        } else {
            mIsETAOn = true;
            viewManager.setEtaButtonOn();
            createEtaDurationMarkers();
        }
    }

    public void onReCenterButtonClicked() {
        mMap.setPadding(0, AppUtility.dpToPx(mNormalMapTopPadding, mContext), 0, 20);
        mEnableAutoCameraAdjust = true;
        mAutoCameraMoved = true;
        mShowRouteLoadedView = false;
        mRouteStartUd = null;
        mRouteEndUd = null;

        showAllMarkers();
        viewManager.hideReCenterButton();
    }

    public void onNavigationButtonClicked() {
        // "http://maps.google.com/maps?saddr=51.5, 0.125&daddr=51.5, 0.15"
        LatLng currentLatLng = mCurrentMarker.getPosition();

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                "http://maps.google.com/maps?daddr=" + currentLatLng.latitude + "," + currentLatLng.longitude + ""));
        startActivity(intent);
    }

    public void onToolbarBackArrowClicked() {
        gotoPreviousPage();
    }

    private void endEventActions() {
        showProgressBar(getResources().getString(R.string.message_general_progressDialog));
        EventManager.endEvent(mEvent, new OnActionCompleteListner() {
            @Override
            public void actionComplete(Action action) {
                mEvent = null;
                ((ActionSuccessFailMessageActivity) mContext).actionComplete(action);
                gotoPreviousPage();
            }
        }, this);
    }

    private void pokeAll() {
        showProgressBar(getResources().getString(R.string.message_general_progressDialog));
        JSONObject jObj = EventParser.createPokeAllContactsJSON(mEvent);
        ParticipantManager.pokeParticipants(jObj, new OnActionCompleteListner() {

            @Override
            public void actionComplete(Action action) {
                SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date currentdate = Calendar.getInstance().getTime();
                String currentTimestamp = originalformat.format(currentdate);
                PreffManager.setPref(mEventId, currentTimestamp);
                ((ActionSuccessFailMessageActivity) mContext).actionComplete(action);
            }
        } , this);
    }
}

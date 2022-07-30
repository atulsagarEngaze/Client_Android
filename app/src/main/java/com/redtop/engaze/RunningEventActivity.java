package com.redtop.engaze;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowCloseListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.redtop.engaze.Interface.FragmentToActivity;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.adapter.EventDetailsOnMapAdapter;
import com.redtop.engaze.adapter.EventUserLocationAdapter;
import com.redtop.engaze.adapter.NameImageAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.app.Config;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.utility.Comparer;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.common.utility.FBShareHelper;
import com.redtop.engaze.common.utility.GoogleDirection;
import com.redtop.engaze.common.utility.InfoWindowHelper;
import com.redtop.engaze.common.utility.MarkerHelper;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.constant.IntentConstants;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.fragment.ExtendEventFragment;
import com.redtop.engaze.fragment.RunningEventParticipantMenuOptionsFragment;
import com.redtop.engaze.manager.EventManager;
import com.redtop.engaze.manager.LocationManager;
import com.redtop.engaze.manager.ParticipantManager;
import com.redtop.engaze.receiver.RunningEventBroadcastReceiver;
import com.redtop.engaze.viewmanager.RunningEventViewManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONObject;

@SuppressLint({"ResourceAsColor", "SimpleDateFormat"})
public class RunningEventActivity extends MyCurrentLocationHandlerActivity implements GoogleMap.OnMarkerClickListener,OnMapReadyCallback, FragmentToActivity<Duration> {

    //region DataMember Declaration
    private long currentThreadId;
    private ArrayList<Marker> mMarkers;
    private boolean mIsInfoWindowOpen = false;
    private Boolean canRefreshUserLocation=true;
    private RunningEventBroadcastReceiver mRunningEventBroadcastManager = null;
    private RunningEventViewManager viewManager = null;
    protected static final String TAG = RunningEventActivity.class.getName();
    private ArrayList<UsersLocationDetail> mUsersLocationDetailList;
    private List<UsersLocationDetail> mRunningEventDetailList;
    private String mEventId;
    private int mEventTypeId;
    public Event mEvent;

    private ArrayList<Marker> mETADistanceMarkers;
    private LatLngBounds mBounds;
    private static final int SNOOZING_REQUEST_CODE = 1;
    private static final int RUNNING_EVENT_MENU_CODE = 2;
    private static final int ADDREMOVE_INVITEES_REQUEST_CODE =3;
    private static final int UPDATE_LOCATION_REQUEST_CODE = 4;
    private EventDetailsOnMapAdapter mEventDetailAdapter;
    private NameImageAdapter mUserLocationItemMenuAdapter;
    private Boolean mEnableAutoCameraAdjust = true;
    private int mLocationRefreshTime = Config.LOCATION_REFRESH_INTERVAL_FAST;

    private HashMap<Marker, UsersLocationDetail> markerUserLocation = new HashMap<Marker, UsersLocationDetail>();

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private Comparer mComparer;
    private Marker mDestinationMarker;
    private EventUserLocationAdapter mUserLocationDetailAdapter;
    private LatLng mDestinationlatlang =null;
    private Marker mCurrentMarker;

    private UsersLocationDetail currentUld;
    private Polyline mPreviousPolyline = null;

    private GoogleDirection mGd;

    private Boolean mIsTrafficOn = true;

    private Boolean mIsETAOn = true;
    private static Boolean isActivityRunning;

    private String mUserId;
    private String mEventStartTimeForUI;
    private Duration mSnooze;
    private int snoozeFlag = 0;

    private Boolean mAutoCameraMoved = true;

    private boolean shouldExecuteOnResume;
    private ArrayList<ContactOrGroup> mContactsAndgroups;
    private EventPlace mDestinationPlace;
    private Handler locationhandler ;
    private Runnable locationRunnable;
    private FBShareHelper fbHelper;
    public Boolean mIsActivityPauseForDialog = false;
    private Boolean mShowRouteLoadedView;
    private UsersLocationDetail mRouteStartUd;
    private UsersLocationDetail mRouteEndUd;
    private View mClickedUserLocationView;
    private int mNormalMapTopPadding = 8 ;
    private int mMarkerCenterMapTopPadding = 100;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_running_event);
        this.initialize(savedInstanceState);
    }

    private void initialize(Bundle savedInstanceState) {
        mComparer = new Comparer();
        fbHelper = new FBShareHelper(this);
        fbHelper.initializeFacebookInstance();
        mRunningEventBroadcastManager = new RunningEventBroadcastReceiver(mContext);
        mEventId = this.getIntent().getStringExtra("EventId");
        mEventTypeId = this.getIntent().getIntExtra("EventTypeId", 0);
        mEvent = EventManager.getEvent(mEventId, true);
        if(mEvent!=null){
            String eventTitle;
            if(mEvent.isEventTrackBuddyEventForCurrentUser()){
                eventTitle =  mContext.getResources().getString(R.string.title_running_event_track_buddies);
            }
            else{
                eventTitle = mEvent.name;
            }
            viewManager = new RunningEventViewManager(mContext, savedInstanceState, eventTitle);
            mUserId = AppContext.context.loginId;
            mGd = new GoogleDirection(mContext);
            initializeEventStartTimeForUI();
        }
        if(mEvent!=null){
            mMarkers = new ArrayList<Marker>();
            mShowRouteLoadedView = false;
            mETADistanceMarkers = new ArrayList<Marker>();
            if (mEvent.destination !=null) {
                mDestinationlatlang = new LatLng(mEvent.destination.getLatitude(), mEvent.destination.getLongitude());
            }
        }
        createRunningEventDetailList();
        BindUserEventDetails();
        createRunnable();

        shouldExecuteOnResume = false;
        showProgressBar(getResources().getString(R.string.message_general_progressDialog));

        if (mEvent == null) {
            Toast.makeText(mContext,
                    mContext.getResources().getString(R.string.message_general_event_null_error),
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        turnOnOfInternetAvailabilityMessage();
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {

        if (!viewManager.isRecenterButtonHidden()) {
            viewManager.clickRecenterButton();
        } else {
            canRefreshUserLocation = true;
            super.onBackPressed();
        }

        return;
    }

    @Override
    protected void onResume() {
        if (!mIsActivityPauseForDialog) {
            isActivityRunning = true;
            if (mEvent == null) {
                runningEventAlertDialog("Event Over", "This event is already over !!", false);
            } else {
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mRunningEventBroadcastManager);
                LocalBroadcastManager.getInstance(mContext).registerReceiver(mRunningEventBroadcastManager,
                        mRunningEventBroadcastManager.mFilter);
                canRefreshUserLocation = true;
                if (shouldExecuteOnResume && !((mUsersLocationDetailList == null || mUsersLocationDetailList.size() == 0))) {
                    mEvent = EventManager.getEvent(mEventId, true);
                    updateRecyclerViews();
                    locationhandler.post(locationRunnable);
                } else {
                    shouldExecuteOnResume = true;
                }

                if (mEvent.isEventPast()) {
                    EventManager.eventOver(mEventId);
                    Intent eventRemoved = new Intent(IntentConstants.EVENT_OVER);
                    eventRemoved.putExtra("eventId", mEventId);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(eventRemoved);
                }
            }
        } else {
            mIsActivityPauseForDialog = false;
        }

        if (mClickedUserLocationView != null) {
            setBackgroundOfRecycleViewItem((CardView) mClickedUserLocationView, Color.TRANSPARENT);
            mClickedUserLocationView = null;
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (!mIsActivityPauseForDialog) {
            isActivityRunning = false;
            mEvent = EventManager.getEvent(mEventId, true);
            locationhandler.removeCallbacks(locationRunnable);
            EventManager.saveUsersLocationDetailList(mContext, mEvent, mUsersLocationDetailList);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mRunningEventBroadcastManager);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mRunningEventBroadcastManager,
                    mRunningEventBroadcastManager.mFilterEventNotExist);
            if (mMyCoordinates != null) {
                PreffManager.setPrefLong("lat", Double.doubleToLongBits(mMyCoordinates.latitude));
                PreffManager.setPrefLong("long", Double.doubleToLongBits(mMyCoordinates.longitude));
            }
            //hideProgressBar();
        }
        super.onPause();
    }

    @Override
    protected void onMyLocationFound(Location location) {

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setPadding(0, AppUtility.dpToPx(mNormalMapTopPadding, mContext), 0, 20);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowCloseListener(new OnInfoWindowCloseListener() {

            @Override
            public void onInfoWindowClose(Marker arg0) {
                viewManager.hideGoogleNavigatioButton();
            }
        });

        mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition arg0) {
                if (!mAutoCameraMoved) {
                    mEnableAutoCameraAdjust = false;
                    if (mMarkers != null && mMarkers.size() > 0) {
                        viewManager.showReCenterButton();
                    }
                } else {
                    mAutoCameraMoved = false;
                }
            }
        });

        mMap.setPadding(0, AppUtility.dpToPx(64, mContext), 0, 0);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setTrafficEnabled(true);
        mUsersLocationDetailList = mEvent.UsersLocationDetailList;

        if (mUsersLocationDetailList == null || mUsersLocationDetailList.size() == 0) {
            createUserLocationList();
        } else {
            updateUserLocationList();
        }

        BindLocationListToAdapter();
        createDestinationMarker();
        refreshRunningEvent();
        locationhandler.post(locationRunnable);
        hideProgressBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mIsActivityPauseForDialog = true;
        switch (item.getItemId()) {
            case R.id.action_end:
                onEventEndClicked();
                break;

            case R.id.action_extend:
                onEventExtendedClicked();
                break;

            case R.id.action_edit_participants:
                onEditParticipantsClicked();
                break;

            case R.id.action_change_destination:
                onChangeEventDestinationClicked();
                break;

            case R.id.action_leave:
                onLeaveEventClicked();

                break;
            case R.id.action_poke_all:
                onPokeAllParticipantsClicked();
                break;

            case R.id.action_share:
                onShareOnFaceBookClicked();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        if (mEvent != null) {
            if (ParticipantManager.isCurrentUserInitiator(mEvent.initiatorId)) {
                getMenuInflater().inflate(R.menu.menu_running_event_initiator, menu);
                if ((mEvent.getParticipantsbyStatus(AcceptanceStatus.getStatus(1))).size() > 1) {
                    menu.removeItem(R.id.action_poke_all);
                }
            } else {
                getMenuInflater().inflate(R.menu.menu_running_event_participant, menu);
            }
        }
        return true;
    }

    @Override
    public void communicate(Duration duration, Fragment source) {
        showProgressBar(getResources().getString(R.string.message_general_progressDialog));
        mSnooze = duration;
        EventManager.extendEventEndTime(mSnooze.getTimeInterval(), mContext, mEvent, action -> {
            UpdateTimeLeftItemOfRunningEventDetailsDataSet();
            mEventDetailAdapter.notifyDataSetChanged();
            AppContext.actionHandler.actionComplete(action);
            locationhandler.post(locationRunnable);
        }, (msg, action) -> onActionFailed(msg, action));
    }

    // region Event Handlers
    public void onEventParticipantUpdatedByInitiator() {
        mEvent = EventManager.getEvent(mEventId, true);
        updateRecyclerViews();
        if (mEvent.isEventTrackBuddyEventForCurrentUser()) {
            runningEventAlertDialog("Tracking Updated!", mEvent.initiatorName + " has updated the participants list.", true);
        } else {
            runningEventAlertDialog("Event Updated!", mEvent.name + ": " + mEvent.initiatorName + " has updated the participants list.", true);
        }
    }

    public void onUserRemovedFromEventByInitiator() {
        if (isActivityRunning && !((Activity) mContext).isFinishing()) {
            if (mEvent.isEventTrackBuddyEventForCurrentUser()) {
                runningEventAlertDialog("Removed from Tracking!", mEvent.initiatorName + " has removed you from this tracking event.", false);
            } else {
                runningEventAlertDialog("Removed from Event!", mEvent.name + ": " + mEvent.initiatorName + " has removed you from this event.", false);
            }
        }
        locationhandler.removeCallbacks(locationRunnable);
        mEvent = null;
    }

    public void onEventDestinationUpdatedByInitiator(String changedDestination) {
        mEvent = EventManager.getEvent(mEventId, true);
        if (mEvent.destination != null) {
            mDestinationlatlang = new LatLng(mEvent.destination.getLatitude(), mEvent.destination.getLongitude());
        }
        removeRoute();
        createDestinationMarker();
        mEnableAutoCameraAdjust = true;
        showAllMarkers();
        if (mEvent.isEventTrackBuddyEventForCurrentUser()) {
            runningEventAlertDialog("Tracking Destination Changed!", mEvent.initiatorName + " has changed tracking Destination to " + changedDestination, true);
        } else {
            runningEventAlertDialog("Event Destination Changed!", mEvent.name + ": " + mEvent.initiatorName + " has changed this events Destination to " + changedDestination, true);
        }

    }

    public void onEventEndedByInitiator() {
        if (isActivityRunning && !((Activity) mContext).isFinishing()) {
            if (mEvent.isEventTrackBuddyEventForCurrentUser()) {
                runningEventAlertDialog("Tracking ended!", mEvent.initiatorName + " has stopped sharing location", false);
            } else {

                runningEventAlertDialog("Event ended!", mEvent.name + " ended by " + mEvent.initiatorName, false);
            }
        }
        locationhandler.removeCallbacks(locationRunnable);
        mEvent = null;
    }

    public void onEventOver() {
        if (isActivityRunning && !((Activity) mContext).isFinishing()) {
            if (mEvent.isEventTrackBuddyEventForCurrentUser()) {
                runningEventAlertDialog("Tracking Over!", "Tracking ended at " + DateUtil.getTimeInHHMMa(mEvent.endTime, "yyyy-MM-dd'T'HH:mm:ss"), false);
            } else {
                runningEventAlertDialog("Event Over!", mEvent.name + " finished at " + DateUtil.getTimeInHHMMa(mEvent.endTime, "yyyy-MM-dd'T'HH:mm:ss"), false);
            }
        }
        locationhandler.removeCallbacks(locationRunnable);
        mEvent = null;
    }

    public void onParticipantLeft(String EventResponderName) {
        mEvent = EventManager.getEvent(mEventId, true);
        if (mEvent != null) {//incase event is already over
            updateRecyclerViews();
            arrangeListinAvailabilityOrder();
            String alertmsg = "";
            if (mEvent.isEventTrackBuddyEventForCurrentUser()) {
                alertmsg = EventResponderName + " has left stopped sharing location";
            } else {
                alertmsg = EventResponderName + " has left " + mEvent.name;
            }
            runningEventAlertDialog("Response Received!", alertmsg, true);
        }
    }

    public void onUserResponse(int eventAcceptanceStateId, String eventResponderName) {
        mEvent = EventManager.getEvent(mEventId, true);
        if (mEvent != null) {//incase event is already over
            updateRecyclerViews();
            arrangeListinAvailabilityOrder();
            String alertmsg = "";

            if (eventAcceptanceStateId != -1) {
                if (AcceptanceStatus.getStatus(eventAcceptanceStateId) == AcceptanceStatus.Accepted) {
                    if (mEvent.isEventTrackBuddyEventForCurrentUser()) {
                        alertmsg = eventResponderName + " has accepted your tracking request";
                    } else {
                        alertmsg = eventResponderName + " has accepted " + mEvent.name;
                    }
                } else {
                    if (mEvent.isEventTrackBuddyEventForCurrentUser()) {
                        alertmsg = eventResponderName + " has rejected your tracking request";
                    } else {
                        alertmsg = eventResponderName + " has rejected " + mEvent.name;
                    }
                }
                runningEventAlertDialog("Response Received!", alertmsg, true);
            }
        }
    }

    public void onEventExtendedByInitiator(String extendEventDuration) {
        mEvent = EventManager.getEvent(mEventId, true);
        UpdateTimeLeftItemOfRunningEventDetailsDataSet();
        mEventDetailAdapter.notifyDataSetChanged();

        if (mEvent.isEventTrackBuddyEventForCurrentUser()) {
            runningEventAlertDialog("Tracking Extended!", mEvent.initiatorName + " has extended tracking by " + extendEventDuration + " minutes.", true);
        } else {
            runningEventAlertDialog("Event Extended!", mEvent.name + ": " + mEvent.initiatorName + " has extended this event by " + extendEventDuration + " minutes.", true);
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

        adb.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            locationhandler.postDelayed(locationRunnable, Config.LOCATION_RETRIVAL_INTERVAL);
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
                mEvent.getCurrentParticipant().acceptanceStatus = AcceptanceStatus.Rejected;
                AppContext.actionHandler.actionComplete(action);
                gotoPreviousPage();

            }
        }, AppContext.actionHandler);
    }

    public void onChangeEventDestinationClicked() {
        Intent intent;
        shouldExecuteOnResume = false;
        if (mEvent.destination != null) {
            mDestinationPlace = new EventPlace(mEvent.destination.getName(), mEvent.destination.getAddress(), new LatLng(mEvent.destination.getLatitude(), mEvent.destination.getLongitude()));
            //mLh.displayPlace( mDestinationPlace, mEventLocationTextView );
        }
        intent = new Intent(RunningEventActivity.this, PickLocationActivity.class);
        if (mDestinationPlace != null) {
            intent.putExtra(IntentConstants.DESTINATION_LOCATION, (Parcelable) mDestinationPlace);
        }
        startActivityForResult(intent, UPDATE_LOCATION_REQUEST_CODE);
    }

    public void onEditParticipantsClicked() {
        shouldExecuteOnResume = false;
        ArrayList<ContactOrGroup> contactList = new ArrayList<ContactOrGroup>();
        String currentMemUserId = mEvent.getCurrentParticipant().userId;
        ArrayList<EventParticipant> members = mEvent.participants;
        for (EventParticipant mem : members) {
            if (!currentMemUserId.equals(mem.userId))
                contactList.add(mem.contactOrGroup);
        }

        PreffManager.setPrefArrayList("Invitees", contactList);

        Intent i = new Intent(RunningEventActivity.this, AddRemoveParticipantsActivity.class);
        startActivityForResult(i, ADDREMOVE_INVITEES_REQUEST_CODE);
    }

    public void onEventExtendedClicked() {
        shouldExecuteOnResume = false;

        FragmentManager fm = getSupportFragmentManager();
        ExtendEventFragment fragment = ExtendEventFragment.newInstance();
        fragment.show(fm, "Extend");
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
                AppContext.actionHandler.actionComplete(action);
                gotoPreviousPage();
            }
        }, AppContext.actionHandler);
    }

    private void pokeAll() {
        showProgressBar(getResources().getString(R.string.message_general_progressDialog));
        JSONObject jObj = EventManager.createPokeAllContactsJSON(mEvent);
        ParticipantManager.pokeParticipants(jObj, new OnActionCompleteListner() {

            @Override
            public void actionComplete(Action action) {
                SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date currentdate = Calendar.getInstance().getTime();
                String currentTimestamp = originalformat.format(currentdate);
                PreffManager.setPref(mEventId, currentTimestamp);
                AppContext.actionHandler.actionComplete(action);
            }
        }, AppContext.actionHandler);
    }
    //endregion

    //region Activity results
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            showProgressBar(getResources().getString(R.string.message_general_progressDialog));

            switch (requestCode) {

                case Constants.ROUTE_END_POINT_REQUEST_CODE:

                    final LatLng endpoint = data.getParcelableExtra("endpoint");
                    final LatLng startPoint = mCurrentMarker.getPosition();
                    GoogleDirection gd = new GoogleDirection(mContext);
                    gd.setOnDirectionResponseListener((GoogleDirection.OnDirectionResponseListener) (status, doc, gd1) -> {
                        hideProgressBar();
                        if (status.equals("OK")) {
                            mShowRouteLoadedView = true;
                            mRouteStartUd = markerUserLocation.get(mCurrentMarker);
                            mRouteEndUd = markerUserLocation.get(findMarkerByLatLang(endpoint));
                            final String distance = gd1.getTotalDistanceText(doc);
                            final String duration = gd1.getTotalDurationText(doc);
                            //						gd.animateDirection(mMap, gd.getDirection(doc), GoogleDirection.SPEED_VERY_FAST
                            //								, true, true, true, false, null, false, true, new PolylineOptions().width(5).color(mContext.getResources().getColor(R.color.primaryDark)));
                            if (mPreviousPolyline != null) {
                                mPreviousPolyline.remove();
                            }
                            mPreviousPolyline = gd1.loadRoute(mMap, gd1.getDirection(doc), new PolylineOptions().width(15).color(mContext.getResources().getColor(R.color.primaryDark)));

                            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                                @Override
                                public View getInfoWindow(Marker arg0) {
                                    // TODO Auto-generated method stub
                                    return null;
                                }

                                @Override
                                public View getInfoContents(Marker arg0) {
                                    View markerInfoWindow = getLayoutInflater().inflate(R.layout.custom_snippet_with_no_address, null);

                                    markerInfoWindow.setLayoutParams(new RelativeLayout.LayoutParams(300, RelativeLayout.LayoutParams.WRAP_CONTENT));
                                    TextView txtDistance = (TextView) markerInfoWindow.findViewById(R.id.distance_endpoint);
                                    txtDistance.setText("Distance " + distance);

                                    TextView eta = (TextView) markerInfoWindow.findViewById(R.id.eta_endpoint);
                                    eta.setText("ETA " + duration);


                                    return markerInfoWindow;
                                }
                            });

                            Marker endPointMarker = findMarkerByLatLang(endpoint);
                            if (endPointMarker != null) {
                                endPointMarker.showInfoWindow();
                                mEnableAutoCameraAdjust = true;
                                adjustMapForLoadedRoute();
                            }
                        } else {
                            Toast.makeText(mContext,
                                    "No Route Found!!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    gd.request(startPoint, endpoint, GoogleDirection.MODE_DRIVING);
                    break;

                case ADDREMOVE_INVITEES_REQUEST_CODE:
                    showProgressBar(getResources().getString(R.string.message_general_progressDialog));
                    mContactsAndgroups = PreffManager.getPrefArrayList("Invitees");
                    ParticipantManager.addRemoveParticipants(mContactsAndgroups, mEvent, action -> {
                        mEvent = EventManager.getEvent(mEventId, true);
                        updateRecyclerViews();
                        AppContext.actionHandler.actionComplete(action);
                        locationhandler.post(locationRunnable);
                    }, (msg, action) -> onActionFailed(msg, action));

                    break;
                case UPDATE_LOCATION_REQUEST_CODE:
                    showProgressBar(getResources().getString(R.string.message_general_progressDialog));
                    mDestinationPlace = data.getParcelableExtra("DestinatonPlace");

                    EventManager.changeDestination(mDestinationPlace, mContext, mEvent, action -> {
                        if (mEvent.destination != null) {
                            mDestinationlatlang = new LatLng(mEvent.destination.getLatitude(), mEvent.destination.getLongitude());
                        }
                        removeRoute();
                        createDestinationMarker();
                        mEnableAutoCameraAdjust = true;
                        showAllMarkers();
                        actionCompleted(action);
                        locationhandler.post(locationRunnable);
                    }, (msg, action) -> {
                        onActionFailed(msg, action);
                    });
                    break;
            }
        }

        fbHelper.getFBCallbackManager().onActivityResult(requestCode, resultCode, data);
    }

    private void onActionFailed(String msg, Action action) {
        if (action == Action.EXTENDEVENTENDTIME || action == Action.ADDREMOVEPARTICIPANTS
                || action == Action.CHANGEDESTINATION) {
            locationhandler.post(locationRunnable);
        }
        actionFailed(msg, action);
    }

    //endregion

    //region Location Refresh
    public void createRunnable() {
        locationhandler = new Handler();
        locationRunnable = new Runnable() {
            public void run() {

                if (mEventId != null && mEvent != null) {
                    turnOnOfInternetAvailabilityMessage();
                    actBasedOnTimeLeft();
                    //loadMyCoordinates();
                    getCurrentLocationsFromServer();
                    startProgressBar();
                }
            }
        };
    }

    private void getCurrentLocationsFromServer() {
        if (!AppContext.context.isInternetEnabled) {
            Log.d(TAG, "No internet connection. Abortig fetching locations from server.");
            if (isActivityRunning) {
                locationhandler.postDelayed(locationRunnable, mLocationRefreshTime);
            }
            return;
        }

        LocationManager.getLocationsFromServer(mUserId, mEventId, new OnAPICallCompleteListener<JSONArray>() {

            @Override
            public void apiCallSuccess(JSONArray response) {
                if (isActivityRunning) {
                    onSuccessLocationResponse(response);
                }
            }

            @Override
            public void apiCallFailure() {
                if (isActivityRunning) {
                    locationhandler.postDelayed(locationRunnable, mLocationRefreshTime); // 60 seconds here you can give
                }
            }
        });
    }

    private void onSuccessLocationResponse(JSONArray response) {

        //Log.d(TAG, response.toString());
        try {

            new populateLocationListWithAddress().execute(response);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,
                    getResources().getString(R.string.message_general_exception),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void refreshRunningEvent() {
        try {
            if (mMyCoordinates != null) {
                upDateMyLocationDetails();
            }
            if (canRefreshUserLocation) {
                mUserLocationDetailAdapter.items = mUsersLocationDetailList;
                mUserLocationDetailAdapter.notifyDataSetChanged();
            }
            UpdateTimeLeftItemOfRunningEventDetailsDataSet();
            mEventDetailAdapter.notifyDataSetChanged();
            addMarkersOfNewlyAddedUsers();

            if (mMarkers != null && mMarkers.size() > 0) {
                if (mShowRouteLoadedView) {
                    adjustMapForLoadedRoute();
                } else if (mIsInfoWindowOpen && mCurrentMarker != null) {
                    keepMarkerInCenterAndShowInfoWindow();
                } else {
                    showAllMarkers();
                }
                if (mDestinationMarker != null && mIsETAOn) {
                    createEtaDurationMarkers();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class populateLocationListWithAddress extends AsyncTask<JSONArray, Void, String> {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected String doInBackground(JSONArray... jsonObjects) {
            try {
                JSONObject locFromServerObj;
                UsersLocationDetail udFromServer;
                JSONArray userLocationsJsonArray = jsonObjects[0];
                ArrayList<UsersLocationDetail> userLocationsFromServer = new ArrayList<>();
                for (int i = 0; i < userLocationsJsonArray.length(); i++) {
                    locFromServerObj = userLocationsJsonArray.getJSONObject(i);
                    udFromServer = AppContext.jsonParser.deserialize(locFromServerObj.get("location").toString(), UsersLocationDetail.class);
                    udFromServer.userId = locFromServerObj.get("userId").toString();
                    userLocationsFromServer.add(udFromServer);
                }

                ParticipantManager.updateUserListWithLocation(userLocationsFromServer, mUsersLocationDetailList, mDestinationlatlang);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String result) {
            if (mEvent == null) {
                return; //event is ended
            }
            ArrayList<Marker> deletedMarkers = new ArrayList<Marker>();

            for (Marker marker : mMarkers) {
                if (!(marker == mDestinationMarker || marker == mCurrentMarker)) {
                    marker.remove();
                    deletedMarkers.add(marker);
                    markerUserLocation.remove(marker);
                }
            }
            mMarkers.removeAll(deletedMarkers);
            deletedMarkers.clear();
            removeEtaMarkers();
            arrangeListinAvailabilityOrder();
            refreshRunningEvent();
            locationhandler.postDelayed(locationRunnable, mLocationRefreshTime); // 60 seconds here you can give
        }

        @Override
        protected void onPreExecute() {
            for (UsersLocationDetail ud : mUsersLocationDetailList) {
                if (ud != null) {
                    if (ud.address == null || ud.address == "") {
                        ud.address = "fetching location..";
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    protected void UpdateTimeLeftItemOfRunningEventDetailsDataSet() {
        mRunningEventDetailList.set(1, new UsersLocationDetail(R.drawable.ic_hourglass_gray, getTimeLeft(), null));
    }

    protected List<UsersLocationDetail> createRunningEventDetailList() {
        mRunningEventDetailList = new ArrayList<>();
        int size;
        mRunningEventDetailList.add(new UsersLocationDetail(R.drawable.ic_timer_gray, mEventStartTimeForUI, null));
        mRunningEventDetailList.add(new UsersLocationDetail(R.drawable.ic_hourglass_gray, getTimeLeft(), null));
        size = (ParticipantManager.getMembersbyStatusForLocationSharing(mEvent, AcceptanceStatus.Accepted)).size();
        if (size > 0) {
            mRunningEventDetailList.add(new UsersLocationDetail(R.drawable.ic_user_accepted, String.valueOf(size), AcceptanceStatus.getStatus(1))); // 1 is ACCEPTED
        }
        size = (ParticipantManager.getMembersbyStatusForLocationSharing(mEvent, AcceptanceStatus.Pending)).size();
        if (size > 0) {
            mRunningEventDetailList.add(new UsersLocationDetail(R.drawable.ic_user_pending, String.valueOf(size), AcceptanceStatus.getStatus(-1))); // -1 is DECLINED
        }
        size = (ParticipantManager.getMembersbyStatusForLocationSharing(mEvent, AcceptanceStatus.Rejected)).size();
        if (size > 0) {
            mRunningEventDetailList.add(new UsersLocationDetail(R.drawable.ic_user_declined, String.valueOf(size), AcceptanceStatus.getStatus(0))); // 0 is PENDING
        }

        return mRunningEventDetailList;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void createUserLocationList() {
        // TODO Auto-generated method stub
        mUsersLocationDetailList = new ArrayList<UsersLocationDetail>();
        mUsersLocationDetailList.addAll(UsersLocationDetail.createUserLocationListFromEventMembers(mEvent));
        arrangeListinAvailabilityOrder();
    }

    protected void BindLocationListToAdapter() {
        try {

            mUserLocationDetailAdapter = new EventUserLocationAdapter(mUsersLocationDetailList, mContext, mEventId);
            viewManager.bindUserLocationDetailRecyclerView(mUserLocationDetailAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void BindUserEventDetails() {
        mEventDetailAdapter = new EventDetailsOnMapAdapter(mRunningEventDetailList, mContext, mEvent);
        viewManager.bindEventDetailRecyclerViewBind(mEventDetailAdapter);
    }

    public void upDateMyLocationDetails() {
        for (UsersLocationDetail ud : mUsersLocationDetailList) {
            if (ud != null && ParticipantManager.isParticipantCurrentUser(ud.userId)) {
                ud.latitude = mMyCoordinates.latitude;
                ud.longitude = mMyCoordinates.longitude;
                if (ud.name == null || ud.name == "" || ud.name == Constants.LOCATION_UNKNOWN) {
                    ud.name = "fetching..";
                }
                if (ud.address == null || ud.address == "" || ud.address == Constants.LOCATION_UNKNOWN) {
                    ud.address = "fetching..";
                }
                SimpleDateFormat Simpledf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                ud.createdOn = Simpledf.format(Calendar.getInstance().getTime());
            }
        }
    }

    protected void updateUserLocationList() {

        ArrayList<UsersLocationDetail> temUldList = new ArrayList<>();
        UsersLocationDetail tUl = null;
        temUldList.addAll(mUsersLocationDetailList);
        for (EventParticipant em : mEvent.participants) {

            Boolean isExist = false;
            tUl = null;
            for (UsersLocationDetail uld : temUldList) {
                if (uld.userId != null && uld.userId.equalsIgnoreCase(em.userId)) {
                    uld.acceptanceStatus = em.acceptanceStatus;
                    tUl = uld;
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                if (ParticipantManager.isValidForLocationSharing(mEvent, em)) {
                    mUsersLocationDetailList.add(UsersLocationDetail.createUserLocationListFromEventMember(em));
                }
            } else {
                temUldList.remove(tUl);
            }
        }
        if (temUldList.size() > 0) {
            mUsersLocationDetailList.removeAll(temUldList);
        }

        arrangeListinAvailabilityOrder();
    }

    protected void updateRecyclerViews() {
        updateUserLocationList();
        mUserLocationDetailAdapter.items = mUsersLocationDetailList;
        mUserLocationDetailAdapter.notifyDataSetChanged();
        mEventDetailAdapter.items = createRunningEventDetailList();
        mEventDetailAdapter.mEvent = mEvent;
        mEventDetailAdapter.notifyDataSetChanged();
    }

    public void userLocationMenuClicked(View v, UsersLocationDetail uld) {
        mIsActivityPauseForDialog = true;
        FragmentManager fm = ((BaseActivity) mContext).getSupportFragmentManager();
        RunningEventParticipantMenuOptionsFragment fragment = RunningEventParticipantMenuOptionsFragment.newInstance(
                uld.userName, uld.userId, mEvent.eventId, uld.acceptanceStatus.getStatus());
        fragment.show(fm, "RunningEventParticipantMenuOptions");

        fragment.dialogDismissListener = () -> {

            if (mClickedUserLocationView != null) {
                setBackgroundOfRecycleViewItem((CardView) mClickedUserLocationView, Color.TRANSPARENT);
                mClickedUserLocationView = null;
            }
        };
        canRefreshUserLocation = false;
        mClickedUserLocationView = v;
        setBackgroundOfRecycleViewItem((CardView) mClickedUserLocationView, this.getResources().getColor(R.color.primaryLight));
    }

    public void userLocationItemClicked(View v, UsersLocationDetail uld) {
        markerRecenter(uld);
    }


    protected void startProgressBar() {
        final int sleepTime = (mLocationRefreshTime) / 100;
        new Thread(new Runnable() {
            @Override
            public void run() {
                currentThreadId = Thread.currentThread().getId();
                int progressStatus = 0;
                while (progressStatus < 100) {
                    progressStatus += 1;
                    try {
                        Thread.sleep(sleepTime);
                        if (currentThreadId != Thread.currentThread().getId()) {
                            break;
                        }
                        ;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    viewManager.mProgressBar.setProgress(progressStatus);
                }
            }
        }).start(); // Start the operation
    }

    private void setBackgroundOfRecycleViewItem(CardView view, int colorId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            view.setCardBackgroundColor(colorId);
            view.setRadius(0);
            view.setMaxCardElevation(0);
            view.setPreventCornerOverlap(false);
            view.setUseCompatPadding(false);
            view.setContentPadding(-15, -15, -15, -15);
        } else {
            view.setBackgroundColor(colorId);
        }
    }
    //endregion

    //region Marker
    @Override
    public boolean onMarkerClick(Marker marker) {
        bringMarkerOncenterAndShowinfoWindow(marker);
        return true;
    }

    public void removeEtaMarkers() {
        for (Marker marker : mETADistanceMarkers) {
            if(!(marker==mDestinationMarker || marker==mCurrentMarker)){
                marker.remove();
            }
        }
    }

    public void createEtaDurationMarkers() {
        if(!AppContext.context.isInternetEnabled){
            return;
        }
        Handler h = new Handler();
        Runnable r = () -> {
            for (final Marker marker : markerUserLocation.keySet() ){
                final UsersLocationDetail ud = markerUserLocation.get(marker);
                if(ud==null){//for destination
                    continue;
                }
                getETADurationAndCreateMarker(ud, marker);
            }
        };
        h.post(r);
    }

    public boolean isMarkerExistForTheUser(String userId) {
        Boolean isExist = false;
        for (UsersLocationDetail ud :  markerUserLocation.values()){
            if(ud!=null && ud.userId.equalsIgnoreCase(userId)){
                isExist = true;
                break;
            }
        }

        return isExist;
    }

    public void markerRecenter(UsersLocationDetail uld){
        if(mCurrentMarker!=null && mCurrentMarker.isInfoWindowShown()){
            mCurrentMarker.hideInfoWindow();
        }
        String title;
        if(uld==null){
            title = "Dest";
        }
        else
        {
            title = uld.userName;
        }
        for (Marker marker : mMarkers) {
            if(title.equals(marker.getTitle())){
                bringMarkerOncenterAndShowinfoWindow(marker);
                break;
            }
        }
    }

    public void keepMarkerInCenterAndShowInfoWindow(){
        mCurrentMarker.hideInfoWindow();
        UsersLocationDetail ud = markerUserLocation.get(mCurrentMarker);
        mCurrentMarker.setPosition(new LatLng( ud.latitude, ud.longitude));
        InfoWindowHelper.createAndshowInfoWindow(mContext, mCurrentMarker, mMap,
                mEvent,mDestinationlatlang,mGd, markerUserLocation, canEnableMarkerClick() );
        LatLng coordinate = mCurrentMarker.getPosition();
        mAutoCameraMoved = false;

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(coordinate).zoom(Constants.ZOOM_VALUE + 1).bearing(0).tilt(90).build();
        mMap.moveCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    private boolean canEnableMarkerClick(){
        if(mDestinationMarker!=null || mMarkers.size()>1){
            return true;
        }
        return false;
    }

    private void bringMarkerOncenterAndShowinfoWindow(Marker marker){
        if(!AppContext.context.isInternetEnabled){
            Toast.makeText(mContext, getResources().getString(R.string.message_general_no_internet_message), Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        mIsInfoWindowOpen=true;
        viewManager.showGoogleNavigatioButton();
        mCurrentMarker = marker;

        InfoWindowHelper.createAndshowInfoWindow(mContext, marker, mMap,
                mEvent,mDestinationlatlang,mGd, markerUserLocation, canEnableMarkerClick() );
        LatLng coordinate = marker.getPosition();
        mAutoCameraMoved = false;

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(coordinate).zoom(Constants.ZOOM_VALUE + 1).bearing(0).tilt(90).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
        mMap.setPadding(0,AppUtility.dpToPx(mMarkerCenterMapTopPadding, mContext),0,20);

    }

    private void getETADurationAndCreateMarker( UsersLocationDetail userlocationdetail, Marker userLocationMarker){
        final UsersLocationDetail ud = userlocationdetail;
        final Marker marker = userLocationMarker;
        try {
            GoogleDirection gd = new GoogleDirection(mContext);
            gd.setOnDirectionResponseListener((status, doc, gd1) -> {
                try
                {
                    if(status.equalsIgnoreCase("failed") || status.equals("REQUEST_DENIED")){
                        ud.distance = "unable to find";
                        ud.eta = "unable to find";
                        mDistance ="unable to find";

                    }
                    else{
                        ud.distance = gd1.getTotalDistanceText(doc);
                        ud.eta = gd1.getTotalDurationText(doc);
                    }
                }
                catch(Exception ex){
                    ud.distance = "No route";
                    ud.eta ="";
                    mDistance ="No route";
                }
                mETADistanceMarkers.add(MarkerHelper.drawTimeDistanceMarker(marker.getPosition(), ud, mMap,RunningEventActivity.this));
            });


            gd.request(marker.getPosition(), mDestinationlatlang, GoogleDirection.MODE_DRIVING);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void adjustMapForLoadedRoute(){
        ArrayList<LatLng> latLangs = new ArrayList<LatLng>();

        if(mRouteStartUd==null){//its a destination
            latLangs.add(mDestinationlatlang);
            latLangs.add(new LatLng(mRouteEndUd.latitude, mRouteEndUd.longitude));
        }
        else if(mRouteEndUd==null){
            latLangs.add(mDestinationlatlang);
            latLangs.add(new LatLng(mRouteStartUd.latitude, mRouteStartUd.longitude));
        }
        else{
            latLangs.add(new LatLng(mRouteStartUd.latitude, mRouteStartUd.longitude));
            latLangs.add(new LatLng(mRouteEndUd.latitude, mRouteEndUd.longitude));
        }

        adjustMap(latLangs);
    }

    public void showAllMarkers(){
        if(mCurrentMarker!=null){
            mCurrentMarker.hideInfoWindow();
            mCurrentMarker = null;

            mIsInfoWindowOpen = false;
        }
        if(!mEnableAutoCameraAdjust){
            return;
        }
        mAutoCameraMoved = true;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers) {
            //marker.hideInfoWindow();
            builder.include(marker.getPosition());
        }
        adjustMapBoundary(builder);
    }

    public void adjustMap(ArrayList<LatLng> latLangs){
        if(!mEnableAutoCameraAdjust){
            return;
        }
        mAutoCameraMoved = true;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng ltlng : latLangs) {
            //marker.hideInfoWindow();
            builder.include(ltlng);
        }
        adjustMapBoundary(builder);
    }

    private void adjustMapBoundary(LatLngBounds.Builder builder ){
        mMap.setPadding(0,50 ,0 ,0 );
        LatLngBounds bounds = null;
        bounds = builder.build();
        bounds = adjustBoundsForMinimumLatitudeDegrees(0.005, bounds);
        int padding = 65; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu);
    }

    public void createDestinationMarker(){
        if(mDestinationlatlang!=null){
            if(mDestinationMarker!=null){
                //removing previous destination marker
                markerUserLocation.remove(mDestinationMarker);
                mDestinationMarker.remove();
                mMarkers.remove(mDestinationMarker);
            }
            mDestinationMarker = MarkerHelper.drawDestinationMarker(mDestinationlatlang, mMap);
            markerUserLocation.put(mDestinationMarker, null);
            mMarkers.add(mDestinationMarker);
        }
    }

    public Marker findMarkerByLatLang(LatLng endpoint) {
        Marker marker = null;
        LatLng markerLaLang;
        if(mMarkers!=null && mMarkers.size()>0){
            for (Marker m : mMarkers){
                markerLaLang = m.getPosition();
                if(markerLaLang.latitude==endpoint.latitude&& markerLaLang.longitude==endpoint.longitude){
                    marker = m;
                    break;
                }
            }
        }
        return marker;
    }

    public void removeRoute(){
        if(mPreviousPolyline!=null){
            mPreviousPolyline.remove();
        }
        if(mIsInfoWindowOpen){
            mIsInfoWindowOpen = false;
            if(mCurrentMarker!=null && mCurrentMarker.isInfoWindowShown()){
                mCurrentMarker.hideInfoWindow();
            }
        }
        mShowRouteLoadedView=false;
    }

    private LatLngBounds adjustBoundsForMinimumLatitudeDegrees(double minLatitudeDegrees, LatLngBounds bounds) {
        LatLng sw = bounds.southwest;
        LatLng ne = bounds.northeast;
        double visibleLatitudeDegrees = Math.abs(sw.latitude - ne.latitude);

        if (visibleLatitudeDegrees < minLatitudeDegrees) {
            LatLng center = bounds.getCenter();
            sw = new LatLng(center.latitude - (minLatitudeDegrees / 2), sw.longitude);
            ne = new LatLng(center.latitude + (minLatitudeDegrees / 2), ne.longitude);
            bounds = new LatLngBounds(sw, ne);
        }

        return bounds;
    }

    protected void addMarkersOfNewlyAddedUsers() {
        LatLng latlang = null;
        Marker marker = null;
        for(UsersLocationDetail userLocationDetail : mUsersLocationDetailList )
        {
            marker = null;
            if(userLocationDetail.acceptanceStatus== AcceptanceStatus.Accepted)
            {
                if (!isMarkerExistForTheUser(userLocationDetail.userId)){

                    if(!userLocationDetail.latitude.equals("")&&!userLocationDetail.longitude.equals("")){
                        latlang = new LatLng(userLocationDetail.latitude, userLocationDetail.longitude);
                        marker = MarkerHelper.drawParticipantMarker(latlang, userLocationDetail, mMap);
                        markerUserLocation.put(marker, userLocationDetail);
                        mMarkers.add(marker);
                    }
                }
            }
        }
    }
    //endregion



    private void initializeEventStartTimeForUI(){

        Calendar calendar = Calendar.getInstance();
        try {
            Date startParsedDate =  sdf.parse(mEvent.startTime);
            calendar.setTime(startParsedDate);
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        mEventStartTimeForUI = DateUtil.getTime(calendar);
    }

    public void gotoPreviousPage(){
        super.onBackPressed();
        finish();
    }

    public void actBasedOnTimeLeft(){
        if(mEvent==null){
            return;
        }
        switch (getTimeLeft()){
            case "5 MINS" :
                if(snoozeFlag != 1 && ParticipantManager.isCurrentUserInitiator(mEvent.initiatorId)){
                    snoozeFlag  = 1;
                    FragmentManager fm = getSupportFragmentManager();
                    ExtendEventFragment fragment = ExtendEventFragment.newInstance();
                    fragment.show(fm, "Extend");
                }
                break;
        }
    }

    public String getTimeLeft(){
        Calendar eventEnd = Calendar.getInstance();
        try {
            eventEnd.setTime(sdf.parse(mEvent.endTime));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        long diffMinutes  = 1+ (eventEnd.getTimeInMillis() - Calendar.getInstance().getTimeInMillis())/60000;
        return DateUtil.getDurationText(diffMinutes);
    }

    protected void arrangeListinAvailabilityOrder() {
        if(mEventTypeId < 100){
            Collections.sort(mUsersLocationDetailList, mComparer);
        }
    }

    protected void runningEventAlertDialog(String title, String message, final Boolean dismissFlag){
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(dismissFlag){
                            alertDialog.setCanceledOnTouchOutside(true);
                            dialog.dismiss();
                        }else{
                            gotoPreviousPage();
                        }
                    }
                });
        alertDialog.show();
    }
}

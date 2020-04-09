package com.redtop.engaze;

import java.util.List;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.redtop.engaze.Interface.IActionHandler;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.Interface.OnRefreshEventListCompleteListner;
import com.redtop.engaze.adapter.HomePendingEventListAdapter;
import com.redtop.engaze.adapter.HomeRunningEventListAdapter;
import com.redtop.engaze.adapter.HomeRunningEventListAdapter.RunningEventAdapterCallback;
import com.redtop.engaze.adapter.HomeTrackLocationListAdapter;
import com.redtop.engaze.adapter.HomeTrackLocationListAdapter.TrackLocationAdapterCallback;
import com.redtop.engaze.adapter.NewSuggestedLocationAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.enums.TrackingType;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.TrackLocationMember;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.fragment.NavDrawerFragment;
import com.redtop.engaze.localbroadcastmanager.HomeBroadcastManager;
import com.redtop.engaze.viewmanager.HomeViewManager;
import com.redtop.engaze.viewmanager.LocationViewManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class HomeActivity extends LocationActivity implements RunningEventAdapterCallback, NavDrawerFragment.FragmentDrawerListener, OnMapReadyCallback, TrackLocationAdapterCallback, OnRefreshEventListCompleteListner, IActionHandler {

    private List<TrackLocationMember> mShareMyLocationList;
    private List<TrackLocationMember> mTrackBuddyList;
    private List<Event> mRunningEventList;
    private List<Event> mPendingEventList;
    private HomeRunningEventListAdapter mRunningEventAdapter;
    private HomePendingEventListAdapter mPendingEventAdapter;
    private HomeTrackLocationListAdapter mShareMyLocationAdapter;
    private HomeTrackLocationListAdapter mTrackBuddyAdapter;
    private Boolean isGPSEnableThreadRun = false;
    private HomeViewManager homeViewManager = null;
    private Duration mSnooze;
    protected HomeBroadcastManager mBroadcastManager = null;
    public Event notificationselectedEvent;

    private static String TAG = HomeActivity.class.getName();

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastManager);
        homeViewManager.hideAllListViewLayout();

    }

    @Override
    protected void onResume() {
        super.onResume();
        turnOnOfInternetAvailabilityMessage();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastManager,
                mBroadcastManager.getFilter());
        displayNotifications();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_home);
        homeViewManager = new HomeViewManager(this);
        locationViewManager = (LocationViewManager) homeViewManager;
        mBroadcastManager = new HomeBroadcastManager(mContext);
        Log.i(TAG, "density: " + AppUtility.deviceDensity);
        //mRunningEventAdapter = new HomeEventListAdapter(null, mContext);
        mRunningEventAdapter = new HomeRunningEventListAdapter(mContext, R.layout.item_home_running_event_list, mRunningEventList);
        mPendingEventAdapter = new HomePendingEventListAdapter(mContext, R.layout.item_home_pending_event_list, mPendingEventList);
        mSuggestedLocationAdapter = new NewSuggestedLocationAdapter(this, R.layout.item_suggested_location_list, mAutoCompletePlaces);
        //homeViewManager.setRunningEventRecycleViewAdapter(mRunningEventAdapter);
        homeViewManager.setLocationViewAdapter(mSuggestedLocationAdapter);
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.home_map);
        mLatlong = new LatLng(Double.longBitsToDouble(PreffManager.getPrefLong("lat")),
                Double.longBitsToDouble(PreffManager.getPrefLong("long")));
        fragment.getMapAsync(this);
        gpsOnListner = null;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setPadding(0, AppUtility.dpToPx(64, mContext), 0, 0);
        mMap.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng arg0) {
                homeViewManager.hideAllListViewLayout();
            }
        });

        if (AppContext.context.isInternetEnabled) {
            runGPSEnableThread();
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatlong, Constants.ZOOM_VALUE));
        }

        findLatLangOnCameraChange = false;
        initializeMapCameraChangeListner();
        homeViewManager.showPin();
    }

    @Override
    protected void onInternetConnectionResume() {
        if (!isGPSEnableThreadRun) {
            runGPSEnableThread();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        switch (id) {
            //		case R.id.action_myevents:
            //			if(mInternetStatus){
            //				intent = new Intent(this, EventsActivity.class);
            //				startActivity(intent);
            //			}
            //			break;

            case R.id.action_refresh:
                showProgressBar(getResources().getString(R.string.message_general_progressDialog));
                EventManager.refreshEventList(this, this);
                if (Constants.DEBUG) {
                    Log.d(TAG, "Refresh Clicked in Home Layout!");
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    public void saveEventState(String eventId, AcceptanceStatus status) {
        showProgressBar("Please wait");
        EventManager.saveUserResponse(status, eventId, this, this);
    }

    @Override
    public void onBackPressed() {
        if (homeViewManager.isAnyNotificationListViewVisible()) {
            homeViewManager.hideAllListViewLayout();
        } else {
            super.onBackPressed();
        }
        return;
    }


    public void refreshTrackBuddyList() {
        mTrackBuddyList = EventManager.getListOfTrackingMembers(mContext, "locationsIn");
        mTrackBuddyAdapter = new HomeTrackLocationListAdapter(mContext, R.layout.item_home_track_location_list, mTrackBuddyList, TrackingType.BUDDY);
        if (mTrackBuddyList.size() == 0) {
            homeViewManager.hideTrackBuddyListAndButtonViewLayout();
        } else {
            homeViewManager.showTrackBuddyListViewLayout(mTrackBuddyAdapter, mTrackBuddyList.size());
        }

    }

    public void refreshShareMyLocationList() {
        mShareMyLocationList = EventManager.getListOfTrackingMembers(mContext, "LocationsOut");
        mShareMyLocationAdapter = new HomeTrackLocationListAdapter(mContext, R.layout.item_home_track_location_list, mShareMyLocationList, TrackingType.SELF);
        if (mShareMyLocationList.size() == 0) {
            homeViewManager.hideShareMyLocationListAndButtonViewLayout();
        } else {
            homeViewManager.showShareMyLocationListViewLayout(mShareMyLocationAdapter, mShareMyLocationList.size());
        }

    }

    public void refreshRunningEventList() {
        mRunningEventList = EventManager.getRunningEventList();
        mRunningEventAdapter = new HomeRunningEventListAdapter(mContext, R.layout.item_home_running_event_list, mRunningEventList);
        if (mRunningEventList.size() == 0) {
            homeViewManager.hideRunningEventListAndButtonViewLayout();
        } else {
            homeViewManager.showRunningEventListViewLayout(mRunningEventAdapter, mRunningEventList.size());
        }
    }

    public void refreshPendingEventList() {
        mPendingEventList = EventManager.getPendingEventList();
        mPendingEventAdapter = new HomePendingEventListAdapter(mContext, R.layout.item_home_pending_event_list, mPendingEventList);
        if (mPendingEventList.size() == 0) {
            homeViewManager.hidePendingEventListAndButtonViewLayout();
        } else {
            homeViewManager.showPendingEventListViewLayout(mPendingEventAdapter, mPendingEventList.size());
        }
    }

    private void runGPSEnableThread() {
        isGPSEnableThreadRun = false;
        Thread thread = new Thread() {
            @Override
            public void run() {
                checkAndEnableGPS();
            }
        };
        thread.start();
    }

    private void displayNotifications() {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                refreshPendingEventList();
                refreshRunningEventList();
                refreshShareMyLocationList();
                refreshTrackBuddyList();
            }
        });
    }

    public void onShowCurrentPendingEventListButtonClicked() {
        homeViewManager.togglePendingEventListView();
    }

    public void onShowCurrentRunningEventListButtonClicked() {
        homeViewManager.toggleRunningEventListView();
    }

    public void onShowCurrentShareMyLocationListButtonClicked() {
        homeViewManager.toggleShareMyLocationListView();
    }

    public void onShowCurrentTrackBuddyListButtonClicked() {
        homeViewManager.toggleTrackBuddyListView();

    }

    public void onMeetNowClicked() {
        if (AppContext.context.isInternetEnabled) {
            Intent intent = new Intent(mContext, TrackLocationActivity.class);
            intent.putExtra("DestinatonLocation", (Parcelable) ((HomeActivity) mContext).mEventPlace);
            intent.putExtra("caller", HomeActivity.class.toString());
            intent.putExtra("EventTypeId", EventType.QUIK.GetEventTypeId());
            startActivity(intent);
        }
    }

    public void onMeetLaterClicked() {
        if (AppContext.context.isInternetEnabled) {
            Intent intent = new Intent(mContext, CreateEditEventActivity.class);
            intent.putExtra("DestinatonLocation", (Parcelable) ((HomeActivity) mContext).mEventPlace);
            startActivity(intent);
        }
    }

    public void onShareMyLocationClicked() {
        if (AppContext.context.isInternetEnabled) {
            Intent intent = new Intent(mContext, TrackLocationActivity.class);
            intent.putExtra("EventTypeId", EventType.SHAREMYLOACTION.GetEventTypeId());//EventType share my location
            startActivity(intent);
        }
    }

    public void onTrackBuddyClicked() {
        if (AppContext.context.isInternetEnabled) {
            Intent intent = new Intent(mContext, TrackLocationActivity.class);
            intent.putExtra("EventTypeId", EventType.TRACKBUDDY.GetEventTypeId());//EventType track buddy
            startActivity(intent);
        }
    }

    @Override
    public void onEventEndClicked() {
        refreshRunningEventList();
    }

    @Override
    public void onEventLeaveClicked() {
        refreshRunningEventList();
    }

    @Override
    public void refreshTrackingEvents() {
        refreshShareMyLocationList();
        refreshTrackBuddyList();
    }

    @Override
    public void actionComplete(Action action) {
        refreshPendingEventList();
        refreshRunningEventList();
        refreshShareMyLocationList();
        refreshTrackBuddyList();
        AppContext.actionHandler.actionComplete(action);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.SNOOZING_REQUEST_CODE:
                    showProgressBar(getResources().getString(R.string.message_general_progressDialog));
                    //update server and cache with new Event end time
                    mSnooze = (Duration) data.getParcelableExtra("com.redtop.engaze.com.redtop.engaze.entity.Snooze");
                    EventManager.extendEventEndTime(mSnooze.getTimeInterval(), mContext, notificationselectedEvent, new OnActionCompleteListner() {
                        @Override
                        public void actionComplete(Action action) {
                            refreshShareMyLocationList();
                            refreshTrackBuddyList();
                            hideProgressBar();
                        }
                    }, AppContext.actionHandler);

                    break;
            }
        }
    }

    @Override
    public void RefreshEventListComplete(List<Event> eventList) {
        refreshPendingEventList();
        refreshRunningEventList();
        refreshShareMyLocationList();
        refreshTrackBuddyList();
        hideProgressBar();
    }

    @Override
    public void actionCancelled(Action action) {

    }

    @Override
    public void actionFailed(String msg, Action action) {
        AppContext.actionHandler.actionFailed(msg, action);
    }
}

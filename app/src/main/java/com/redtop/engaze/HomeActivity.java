package com.redtop.engaze;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
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
import com.redtop.engaze.adapter.HomePendingEventListAdapter;
import com.redtop.engaze.adapter.HomeRunningEventListAdapter;
import com.redtop.engaze.adapter.HomeRunningEventListAdapter.RunningEventAdapterCallback;
import com.redtop.engaze.adapter.HomeTrackLocationListAdapter;
import com.redtop.engaze.adapter.HomeTrackLocationListAdapter.TrackLocationAdapterCallback;
import com.redtop.engaze.adapter.NewSuggestedLocationAdapter;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.EventDetail;
import com.redtop.engaze.domain.TrackLocationMember;
import com.redtop.engaze.fragment.NavDrawerFragment;
import com.redtop.engaze.interfaces.OnActionCompleteListner;
import com.redtop.engaze.interfaces.OnRefreshEventListCompleteListner;
import com.redtop.engaze.localbroadcastmanager.HomeBroadcastManager;
import com.redtop.engaze.utils.AppUtility;
import com.redtop.engaze.utils.Constants;
import com.redtop.engaze.utils.Constants.AcceptanceStatus;
import com.redtop.engaze.utils.Constants.Action;
import com.redtop.engaze.utils.Constants.TrackingType;
import com.redtop.engaze.utils.EventManager;
import com.redtop.engaze.viewmanager.HomeViewManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class HomeActivity extends LocationActivity implements RunningEventAdapterCallback,  NavDrawerFragment.FragmentDrawerListener, OnMapReadyCallback, TrackLocationAdapterCallback, OnRefreshEventListCompleteListner{		

	private List<TrackLocationMember> mShareMyLocationList;	
	private List<TrackLocationMember> mTrackBuddyList;	
	private List<EventDetail> mRunningEventDetailList;
	private List<EventDetail> mPendingEventDetailList;
	private HomeRunningEventListAdapter mRunningEventAdapter;
	private HomePendingEventListAdapter mPendingEventAdapter;
	private HomeTrackLocationListAdapter mShareMyLocationAdapter;
	private HomeTrackLocationListAdapter mTrackBuddyAdapter;
	private Boolean isGPSEnableThreadRun = false;
	private HomeViewManager homeViewManager = null;
	private Duration mSnooze;

	@Override
	protected void onPause() {	
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver( mBroadcastManager);
		homeViewManager.hideAllListViewLayout();
		super.onPause();		
	}

	@Override
	protected void onResume() {
		turnOnOfInternetAvailabilityMessage(this);
		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastManager,
				mBroadcastManager.getFilter());	
		displayNotifications();
		super.onResume();		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	 	
		mContext = this;
		TAG = HomeActivity.class.getName();
		setContentView(R.layout.activity_home);
		locationViewManager = new HomeViewManager(this);
		homeViewManager = (HomeViewManager)locationViewManager;
		mBroadcastManager = new HomeBroadcastManager(mContext);		
		Log.i(TAG, "density: "+ AppUtility.deviceDensity);		
		//mRunningEventAdapter = new HomeEventListAdapter(null, mContext);
		mRunningEventAdapter = new HomeRunningEventListAdapter(mContext, R.layout.item_home_running_event_list, mRunningEventDetailList);
		mPendingEventAdapter = new HomePendingEventListAdapter(mContext, R.layout.item_home_pending_event_list, mPendingEventDetailList);
		mSuggestedLocationAdapter = new NewSuggestedLocationAdapter(this, R.layout.item_suggested_location_list, mAutoCompletePlaces);
		//homeViewManager.setRunningEventRecycleViewAdapter(mRunningEventAdapter);			
		homeViewManager.setLocationViewAdapter(mSuggestedLocationAdapter);		
		SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.home_map);
		mLatlong = new LatLng( Double.longBitsToDouble(AppUtility.getPrefLong("lat", mContext)), 
				Double.longBitsToDouble(AppUtility.getPrefLong("long", mContext)));
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

		if(mInternetStatus){				
			runGPSEnableThread();			
		}			
		else
		{
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatlong, Constants.ZOOM_VALUE));
		}

		findLatLangOnCameraChange = false;
		initializeMapCameraChangeListner();	
		homeViewManager.showPin();				
	}	

	@Override
	protected void onInternetConnectionResume(){
		if(!isGPSEnableThreadRun){			
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
		Intent intent = null ;
		switch(id){
		//		case R.id.action_myevents:
		//			if(mInternetStatus){
		//				intent = new Intent(this, EventsActivity.class); 
		//				startActivity(intent);
		//			}
		//			break;	

		case R.id.action_refresh:
			showProgressBar(getResources().getString(R.string.message_general_progressDialog));			
			EventManager.refreshEventList(this, this,this) ;
			if(Constants.DEBUG){
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
		EventManager.saveUserResponse(status, mContext, eventId, this, this);
	}	

	@Override
	public void onBackPressed() {
		if(homeViewManager.isAnyNotificationListViewVisible()){
			homeViewManager.hideAllListViewLayout();
		}
		else{
			super.onBackPressed();
		}				
		return;
	}


	public void refreshTrackBuddyList() {
		mTrackBuddyList = EventManager.getListOfTrackingMembers(mContext, "locationsIn");
		mTrackBuddyAdapter = new HomeTrackLocationListAdapter(mContext, R.layout.item_home_track_location_list, mTrackBuddyList, TrackingType.BUDDY);
		if(mTrackBuddyList.size()==0){
			homeViewManager.hideTrackBuddyListAndButtonViewLayout();
		}
		else{
			homeViewManager.showTrackBuddyListViewLayout(mTrackBuddyAdapter, mTrackBuddyList.size());
		}

	}

	public void refreshShareMyLocationList() {
		mShareMyLocationList = EventManager.getListOfTrackingMembers(mContext, "LocationsOut");
		mShareMyLocationAdapter = new HomeTrackLocationListAdapter(mContext, R.layout.item_home_track_location_list, mShareMyLocationList, TrackingType.SELF);
		if(  mShareMyLocationList.size()==0){
			homeViewManager.hideShareMyLocationListAndButtonViewLayout();
		}
		else{
			homeViewManager.showShareMyLocationListViewLayout(mShareMyLocationAdapter, mShareMyLocationList.size());
		}	

	}
	public void refreshRunningEventList(){
		mRunningEventDetailList = EventManager.getRunningEventList(mContext);
		mRunningEventAdapter = new HomeRunningEventListAdapter(mContext, R.layout.item_home_running_event_list, mRunningEventDetailList);
		if(mRunningEventDetailList.size()==0){
			homeViewManager.hideRunningEventListAndButtonViewLayout();
		}
		else{
			homeViewManager.showRunningEventListViewLayout(mRunningEventAdapter, mRunningEventDetailList.size());
		}		
	}

	public void refreshPendingEventList(){
		mPendingEventDetailList = EventManager.getPendingEventList(mContext);
		mPendingEventAdapter = new HomePendingEventListAdapter(mContext, R.layout.item_home_pending_event_list, mPendingEventDetailList);
		if(mPendingEventDetailList.size()==0){
			homeViewManager.hidePendingEventListAndButtonViewLayout();
		}
		else{
			homeViewManager.showPendingEventListViewLayout(mPendingEventAdapter, mPendingEventDetailList.size());
		}		
	}

	private void runGPSEnableThread(){
		isGPSEnableThreadRun =  false;
		Thread thread = new Thread(){
			@Override
			public void run(){
				checkAndEnableGPS();
			}
		};
		thread.start();
	}

	private void displayNotifications(){
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
		if(mInternetStatus){
			Intent intent = new Intent(mContext, TrackLocationActivity.class);			
			intent.putExtra("DestinatonLocation", (Parcelable)((HomeActivity)mContext).mEventPlace);
			intent.putExtra("caller", HomeActivity.class.toString());
			intent.putExtra("EventTypeId", 6);			
			startActivity(intent);
		}		
	}

	public void onMeetLaterClicked() {
		if(mInternetStatus){						
			Intent intent = new Intent(mContext, CreateEditEventActivity.class);
			intent.putExtra("DestinatonLocation", (Parcelable)((HomeActivity)mContext).mEventPlace);
			startActivity(intent);
		}		
	}

	public void onShareMyLocationClicked() {
		if(mInternetStatus){						
			Intent intent = new Intent(mContext, TrackLocationActivity.class);
			intent.putExtra("EventTypeId", 100);//EventType share my location
			startActivity(intent);
		}	
	}

	public void onTrackBuddyClicked() {
		if(mInternetStatus){						
			Intent intent = new Intent(mContext, TrackLocationActivity.class);			
			intent.putExtra("EventTypeId", 200);//EventType track buddy
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
		super.actionComplete(action);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch (requestCode)
			{
			case Constants.SNOOZING_REQUEST_CODE:
				showProgressBar(getResources().getString(R.string.message_general_progressDialog));
				//update server and cache with new Event end time 
				mSnooze = (Duration)data.getParcelableExtra("com.redtop.engaze.com.redtop.engaze.entity.Snooze");
				EventManager.extendEventEndTime(mSnooze.getTimeInterval(), mContext, notificationselectedEvent, new OnActionCompleteListner() {
					@Override
					public void actionComplete(Action action) {						
						refreshShareMyLocationList();
						refreshTrackBuddyList();
						hideProgressBar();
					}
				}, this);

				break;
			}
		}
	}

	@Override
	public void RefreshEventListComplete(List<EventDetail> eventDetailList) {
		refreshPendingEventList();
		refreshRunningEventList();
		refreshShareMyLocationList();
		refreshTrackBuddyList();
		hideProgressBar();
	}	

}

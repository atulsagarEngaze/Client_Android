package com.redtop.engaze.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.EventState;
import com.redtop.engaze.common.constant.DurationConstants;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.domain.manager.LocationManager;

import androidx.annotation.NonNull;

//this service upload the current address to server to be available to other users in the event
public class MyCurrentLocationToServerUploader extends Service {

	private Location location;
	private static Boolean isUpdateInProgress = false;
	private static Boolean isFirstLocationRequiredForNewEvent = false;
	private static Location lastLocation= null;

	private FusedLocationProviderClient fusedLocationClient;
	private LocationCallback locationCallback;
	private LocationRequest locationRequest;

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	public static final String TAG = MyCurrentLocationToServerUploader.class.getName();


	private final Handler runningEventCheckHandler = new Handler();
	private Runnable runningEventCheckRunnable = new Runnable() {
		public void run() {	
			Log.v(TAG, "Running event check callback. Checking for any running event");	
			if(!EventService.isAnyEventInState(EventState.TRACKING_ON, true)){
				AppContext.context.stopService(new Intent(AppContext.context, MyCurrentLocationToServerUploader.class));
			}
			else{
				runningEventCheckHandler.postDelayed(runningEventCheckRunnable, DurationConstants.RUNNING_EVENT_CHECK_INTERVAL);
				//notifyIfEventParticipantsDistanceReminderMet(mContext, mGoogleApiClient);
			}
		}	
	};
	public synchronized static void performStartStop(){

		if(EventService.shouldShareLocation())
		{
			isFirstLocationRequiredForNewEvent = true;
			if(AppContext.context.isInternetEnabled){
				AppContext.context.startService(new Intent(AppContext.context, MyCurrentLocationToServerUploader.class));
			}
		}
		else
		{
			AppContext.context.stopService(new Intent(AppContext.context, MyCurrentLocationToServerUploader.class));
		}
	}

	public synchronized static void performStop(){
		AppContext.context.stopService(new Intent(AppContext.context, MyCurrentLocationToServerUploader.class));
	}

	@Override
	public IBinder onBind(Intent arg0) {		
		return null;
	}

	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "\n LocationUpdatorService created ");
		runningEventCheckHandler.removeCallbacks(runningEventCheckRunnable);
		runningEventCheckHandler.postDelayed(runningEventCheckRunnable, DurationConstants.RUNNING_EVENT_CHECK_INTERVAL);
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		createLocationRequest();
		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				if (locationResult == null) {
					return;
				}
				onLocationChanged(locationResult.getLocations().get(0));
			}
		};
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		Log.v(TAG, "Location Updator Service started");

		fusedLocationClient.requestLocationUpdates(locationRequest,
				locationCallback,
				Looper.getMainLooper());

		return START_STICKY ;
	}

	protected void  createLocationRequest() {
		locationRequest = LocationRequest.create();
		locationRequest.setInterval(DurationConstants.LOCATION_REFRESH_INTERVAL_NORMAL);
		locationRequest.setFastestInterval(DurationConstants.LOCATION_REFRESH_INTERVAL_FAST);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(locationRequest);

		SettingsClient client = LocationServices.getSettingsClient(AppContext.context);
		Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

		task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
			@Override
			public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
				// All location settings are satisfied. The client can initialize
				// location requests here.
				// ...
			}
		});

		task.addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {

			}
		});
	}

	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "Destroy Location Updator Service");
		fusedLocationClient.removeLocationUpdates(locationCallback);
		runningEventCheckHandler.removeCallbacks(runningEventCheckRunnable);
		Log.v(TAG, "Destroy Running event check callback");	
	}

	public void onLocationChanged(Location location) {
		if(!isUpdateInProgress){
			if(lastLocation!=null){

				if(isFirstLocationRequiredForNewEvent ||  lastLocation.distanceTo(location) > 
				Integer.parseInt(AppContext.context.getResources().getString(R.string.min_distance_in_meter_location_update))){
					updateCurrentLocationToServer(location);
					isFirstLocationRequiredForNewEvent = false;
				}
			}
			else{
				updateCurrentLocationToServer(location);
			}
		}
	}
	private void updateCurrentLocationToServer(final Location location){
		isUpdateInProgress = true;
		LocationManager.updateLocationToServer(location, response -> {
			isUpdateInProgress = false;
			lastLocation = location;
		}, (msg, action) -> isUpdateInProgress = false);
	}
}

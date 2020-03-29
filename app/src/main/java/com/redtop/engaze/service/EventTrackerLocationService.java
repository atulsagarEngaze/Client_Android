package com.redtop.engaze.service;

import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.AppService;
import com.redtop.engaze.constant.DurationConstants;
import com.redtop.engaze.constant.Veranstaltung;

public class EventTrackerLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, 
GoogleApiClient.OnConnectionFailedListener, LocationListener {

	private Location location;
	private static Boolean isUpdateInProgress = false;
	private static Boolean isFirstLocationRequiredForNewEvent = false;
	private static Location lastLocation= null;

	protected GoogleApiClient mGoogleApiClient;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private static Context mContext = null;	
	public static final String TAG = EventTrackerLocationService.class.getName();
	private LocationRequest mLocationRequest;
	private final Handler runningEventCheckHandler = new Handler();
	private Runnable runningEventCheckRunnable = new Runnable() {
		public void run() {	
			Log.v(TAG, "Running event check callback. Checking for any running event");	
			if(!AppUtility.isAnyEventInState(mContext, Veranstaltung.TRACKING_ON, true)){
				mContext.stopService(new Intent(mContext, EventTrackerLocationService.class));
			}
			else{
				runningEventCheckHandler.postDelayed(runningEventCheckRunnable, DurationConstants.RUNNING_EVENT_CHECK_INTERVAL);
				//notifyIfEventParticipantsDistanceReminderMet(mContext, mGoogleApiClient);
			}
		}	
	};
	public synchronized static void peroformSartStop(Context context){

		if(AppUtility.shouldShareLocation(context))
		{
			isFirstLocationRequiredForNewEvent = true;
			if(AppService.isNetworkAvailable(context)){
				context.startService(new Intent(context, EventTrackerLocationService.class));
			}
		}
		else
		{
			context.stopService(new Intent(context, EventTrackerLocationService.class));
		}
	}

	public synchronized static void peroformStop(Context context){

		context.stopService(new Intent(context, EventTrackerLocationService.class));		
	}

	@Override
	public IBinder onBind(Intent arg0) {		
		return null;
	}

	public void onCreate() {
		super.onCreate();
		mContext = this;		
		Log.v(TAG, "\n LocationUpdatorService created ");
		createGoogleApiClient();
		runningEventCheckHandler.removeCallbacks(runningEventCheckRunnable);
		runningEventCheckHandler.postDelayed(runningEventCheckRunnable, DurationConstants.RUNNING_EVENT_CHECK_INTERVAL);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		Log.v(TAG, "Location Updator Service started");
		if(mLocationRequest==null){
			mLocationRequest = LocationRequest.create()
					.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
					.setInterval(DurationConstants.LOCATION_REFRESH_INTERVAL_NORMAL)        // 10 seconds, in milliseconds
					.setFastestInterval(DurationConstants.LOCATION_REFRESH_INTERVAL_FAST); // 5 second, in milliseconds
		}
		if (mGoogleApiClient == null) {
			createGoogleApiClient();
			Log.v(TAG, "Recreating google api client");
		}
		else if (!mGoogleApiClient.isConnected()){
			mGoogleApiClient.connect();
			Log.v(TAG, "Reconnecting google api client");
		}

		return START_STICKY ;
	}

	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "Destroy Location Updator Service");		
		if (mGoogleApiClient.isConnected()){
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
			mGoogleApiClient.disconnect();
		}
		runningEventCheckHandler.removeCallbacks(runningEventCheckRunnable);
		Log.v(TAG, "Destroy Running event check callback");	
	}	

	private static void onResponseReturn() {
		Log.d(TAG, "returned from the server");		
	}

	public static void updateLocationToServer(final Location location) {

		if(!AppService.isNetworkAvailable(mContext)){
			Log.d(TAG, "No internet connection. Abortig location update to server.");
			isUpdateInProgress = false;
			return;
		}
		String tag_json_obj = "json_obj_post_user_location";
		JSONObject jobj = new JSONObject();

		try {
			jobj.put("UserId", AppContext.getInstance().loginId);
			jobj.put("Latitude", "" + location.getLatitude());
			jobj.put("Longitude", "" + location.getLongitude());	
			jobj.put("ETA", "1.0");
			jobj.put("ArrivalStatus", "0");
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Failed to update location");
			isUpdateInProgress = false;
		}		

		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				Constants.MAP_API_URL + Constants.METHOD_USER_LOCATION_UPLOAD,
				jobj, new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				//Log.d(TAG, "inside : " + response.toString());
				isUpdateInProgress = false;	
				lastLocation = location;
			}

		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "Error: " + error.getMessage());
				error.printStackTrace();
				isUpdateInProgress = false;
				onResponseReturn();

			}
		}) {

		};
		jsonObjReq.setRetryPolicy((RetryPolicy) new DefaultRetryPolicy(DurationConstants.DEFAULT_SHORT_TIME_TIMEOUT,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		// Adding request to request queue
		VolleyAppController.getInstance().addToRequestQueue(jsonObjReq,
				tag_json_obj);
	}

	@Override
	public void onLocationChanged(Location location) {
		if(!isUpdateInProgress){
			if(lastLocation!=null){

				if(isFirstLocationRequiredForNewEvent ||  lastLocation.distanceTo(location) > 
				Integer.parseInt(mContext.getResources().getString(R.string.min_distance_in_meter_location_update))){
					isUpdateInProgress = true;
					updateLocationToServer(location);
					isFirstLocationRequiredForNewEvent = false;
				}

			}
			else{
				updateLocationToServer(location);

			}
			//lastLocation = location;
		}

	}	

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			Log.i(TAG, "Location services has a resolution " + connectionResult.getErrorCode());
			// Start an Activity that tries to resolve the error
			//connectionResult.startResolutionForResult(mContext, CONNECTION_FAILURE_RESOLUTION_REQUEST);

		} else {
			Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
		}
	}	

	@Override
	public void onConnected(Bundle arg0) {
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
		if (location != null) {
			if(!isUpdateInProgress){

				if(lastLocation!=null)
				{
					if(lastLocation.distanceTo(location)> 
					Integer.parseInt(mContext.getResources().getString(R.string.min_distance_in_meter_location_update))){
						isUpdateInProgress = true;
						updateLocationToServer(location);
					}

				}
				else{
					updateLocationToServer(location);										
				}
				//lastLocation = location;
			}		
		}	
		Log.i(TAG, "Location services connected.");
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.i(TAG, "Location services suspended. Please reconnect.");
	}

	private void createGoogleApiClient(){		
		mGoogleApiClient = 
				new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(LocationServices.API)
		.addApi( Places.GEO_DATA_API )
		.addApi( Places.PLACE_DETECTION_API ).build();	
		mGoogleApiClient.connect();		
	}
}

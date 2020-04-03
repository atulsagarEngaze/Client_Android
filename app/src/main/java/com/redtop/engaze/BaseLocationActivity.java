package com.redtop.engaze;
import java.util.Locale;

import android.content.IntentSender;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.redtop.engaze.Interface.OnGpsSetOnListner;
import com.redtop.engaze.common.utility.AppLocationService;
import com.redtop.engaze.common.constant.DurationConstants;

public class BaseLocationActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener, LocationListener  {
	public static LatLng mMyCoordinates;
	protected static String mDistance ="";
	protected static String mDuration ="";
	protected GoogleMap mMap;
	protected AppLocationService mLh;
	protected Geocoder mGeocoder;
	protected GoogleApiClient mGoogleApiClient;	
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	//protected int markerCenterImageResId;
	protected final static int CHECK_SETTINGS_REQUEST_CODE = 8;
	protected final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
	private LocationRequest mLocationRequest;
	protected OnGpsSetOnListner gpsOnListner= null;

	private final static String TAG = BaseLocationActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLocationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(DurationConstants.LOCATION_REFRESH_INTERVAL_NORMAL)   //in milliseconds
				.setFastestInterval(DurationConstants.LOCATION_REFRESH_INTERVAL_FAST); //in milliseconds
		//markerCenterImageResId =	R.drawable.ic_home;
		createGoogleApiClient();
		mLh = new AppLocationService(this, BaseLocationActivity.this);
		mGeocoder = new Geocoder(this, Locale.getDefault());		
	}

	protected void createGoogleApiClient(){		
		mGoogleApiClient= 
				new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(LocationServices.API)
		.addApi( Places.GEO_DATA_API )
		.addApi( Places.PLACE_DETECTION_API ).build();	
		mGoogleApiClient.connect();		
	}


	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				mGoogleApiClient, this);
	}		

	@Override
	public void onConnected(Bundle arg0) {
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this );
		Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (location != null) {
			onMyLocationFound(location);			
		}		
		Log.i(TAG, "Location services connected.");
	}


	@Override
	public void onLocationChanged(Location location) {
		onMyLocationFound(location)	;	
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.i(TAG, "Location services suspended. Please reconnect.");
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
		}

	}		

	@Override
	protected void onResume() {		
		super.onResume();
		//checkPlayServices();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.connect();
		}		
	}

	@Override
	protected void onPause() {		
		super.onPause();
		if (mGoogleApiClient != null&& mGoogleApiClient.isConnected()) {
			stopLocationUpdates();
			mGoogleApiClient.disconnect();
		}		
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mGoogleApiClient != null&& !mGoogleApiClient.isConnected()) {
			mGoogleApiClient.connect();
		}
	}

	protected void onMyLocationFound(Location location) {			
		mMyCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
	}	
}

package com.redtop.engaze;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.redtop.engaze.adapter.CachedLocationAdapter;
import com.redtop.engaze.adapter.NewSuggestedLocationAdapter;
import com.redtop.engaze.common.PreffManager;
import com.redtop.engaze.common.cache.DestinationCacher;
import com.redtop.engaze.domain.AutoCompletePlace;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.viewmanager.LocationViewManager;


public abstract class LocationActivity extends BaseLocationActivity implements LocationListener  {
	protected LocationViewManager locationViewManager =null;
	public NewSuggestedLocationAdapter mSuggestedLocationAdapter;
	public CachedLocationAdapter mCachedLocationAdapter;
	public ArrayList<Marker> mMarkers = new ArrayList<Marker>();		
	public LatLng mLatlong = null ;
	public EventPlace mEventPlace;	
	public ArrayList<AutoCompletePlace>mAutoCompletePlaces = new ArrayList<AutoCompletePlace>();
	public String mOriginalQuery="";		
	public Boolean isMapSetToMyLocation = false;
	public Boolean findLatLangOnCameraChange = true;
	public Boolean findAddressOnCameraChange = true;	
	public Boolean isCameraMovedToMyLocation = false;
	public Boolean isOnload = true;
	public Boolean needLocation = true;	
	public Boolean isGPSOn = false;
	public Boolean isImageSetToGray = false;	
	public LocationManager mLm;


	protected void createEventPlace(){
		Place place = mLh.getPlaceFromLatLang(mLatlong);
		mEventPlace = null;
		if(place!=null){
			mEventPlace = new EventPlace(place.getName().toString(),
					place.getAddress().toString(),place.getLatLng());
		}
	}		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);	
		new Handler().post(new Runnable() {			
			@Override
			public void run() {
				mCachedLocationAdapter = new CachedLocationAdapter(mContext, R.layout.item_cached_location_list, DestinationCacher.getDestinationsFromCache(mContext));
				locationViewManager.setCacheLocationListAdapter(mCachedLocationAdapter);
			}
		});
	}

	@Override
	protected void onPause() {	
		mLm.removeUpdates(this); 
		super.onPause();		
	}

	@Override
	protected void onResume() {
		mLm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10,(LocationListener)this);
		if ( mLm.isProviderEnabled( LocationManager.GPS_PROVIDER ) ){
			isGPSOn = true;
		}
		else{
			isGPSOn = false;
		}
		if(mCachedLocationAdapter!=null){
			mCachedLocationAdapter.mItems = DestinationCacher.getDestinationsFromCache(mContext);
			locationViewManager.setCacheLocationListAdapter(mCachedLocationAdapter);
		}
		super.onResume();
	}

	protected void bringPinToMyLocation(){
		try{						
			//myImageButton.setVisibility(View.GONE);
			Location location = mLh.getMyLocation2(mGoogleApiClient);
			if(location!=null){
				mLatlong = new LatLng(location.getLatitude(), location.getLongitude());	
				mMyCoordinates = mLatlong;
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatlong, Constants.ZOOM_VALUE));
				findLatLangOnCameraChange = false;
			}
			else{
				mLatlong = null;
				isCameraMovedToMyLocation = false;				
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}	

	protected void myLocationButtonClicked(){}

	protected void postCameraMoved(){}

	protected void initializeMapCameraChangeListner(){
		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition arg0) {			

				if(findLatLangOnCameraChange){
					mLatlong = mMap.getCameraPosition().target;					
				}
				else{
					findLatLangOnCameraChange = true;
				}
				if(isGPSOn){
					if(mMyCoordinates==mLatlong && mMyCoordinates !=null && mLatlong !=null){
						locationViewManager.setGpsOnPinOnMyLocationDrawable();
						PreffManager.setPrefLong("lat", Double.doubleToLongBits(mLatlong.latitude), mContext);
						PreffManager.setPrefLong("long", Double.doubleToLongBits(mLatlong.longitude), mContext);
					}
					else{					
						locationViewManager.setGpsOnDrawable();
					}
				}

				try {
					if(findAddressOnCameraChange){
						new CameraChangeGetPlace().execute();
					}
					else{
						findAddressOnCameraChange = true;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void getAutoCompletePlacePridictions(CharSequence query){
		if(!mInternetStatus){
			return;
		}
		String newQuery = query.toString();
		Location location = new Location("");
		location.setLatitude(mLatlong.latitude);
		location.setLongitude(mLatlong.longitude);
		LatLngBounds bounds = mLh.getLatLongBounds(location);
		List<Integer> filterTypes = new ArrayList<Integer>();

		Places.GeoDataApi.getAutocompletePredictions( mGoogleApiClient, newQuery, bounds, AutocompleteFilter.create( filterTypes ) )
		.setResultCallback (
				new ResultCallback<AutocompletePredictionBuffer>() {
					@Override
					public void onResult( AutocompletePredictionBuffer buffer ) {						
						OnAutocomleteSuccess(buffer);	                	 
					}
				}, 60, TimeUnit.SECONDS );

	}

	private void OnAutocomleteSuccess(AutocompletePredictionBuffer buffer)
	{
		if( buffer == null )
			return;
		mAutoCompletePlaces.clear();

		if( buffer.getStatus().isSuccess() ) {
			for( AutocompletePrediction prediction : buffer ) {
				//Add as a new item to avoid IllegalArgumentsException when buffer is released
				mAutoCompletePlaces.add( new AutoCompletePlace( prediction.getPlaceId(), prediction.getDescription() ) );
			}
		}

		//Prevent memory leak by releasing buffer
		buffer.release();		
		mSuggestedLocationAdapter.mItems = mAutoCompletePlaces;
		mSuggestedLocationAdapter.notifyDataSetChanged();	
	}	

	private class CameraChangeGetPlace extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try{
				createEventPlace();				
			}
			catch(Exception e){
				e.printStackTrace();
			}

			return "";
		}

		@Override
		protected void onPostExecute(String result) {

			if(mEventPlace==null){//when network is slow, or google service is down
				
				turnOnOfLocationAvailabilityMessage(mContext, false);
				
//				Toast.makeText(mContext,
//						getResources().getString(R.string.unable_locate_address),
//						Toast.LENGTH_LONG).show();
				Log.d(TAG, "Connection is slow, unable to fetch address");
				hideProgressBar();
				return;
			}			
			turnOnOfLocationAvailabilityMessage(mContext, true);
			//new CameraChangeGetPlace().execute();
			locationViewManager.setLocationText(mEventPlace.getName());
			locationViewManager.setLocationNameAndAddress(mEventPlace.getName(),mEventPlace.getAddress());			
			postCameraMoved();
		}

		@Override
		protected void onPreExecute() {			
		}

		@Override
		protected void onProgressUpdate(Void... values) {}
	}	

	@Override
	protected void onMyLocationFound(Location location) {
		if(mMap==null){
			return ;//this may call before map is loaded
		}
		synchronized (this)
		{
			if(!needLocation){				
				return;
			}
			else{
				needLocation = false;
			}
		}

		mMyCoordinates = new LatLng(location.getLatitude(), location.getLongitude());	
		if((this.getClass().getSimpleName().equals(HomeActivity.class.getSimpleName()))
				||(this.getClass().getSimpleName()==PickLocationActivity.class.getName()&& mLatlong==null)){
			mLatlong = mMyCoordinates;
			if(!isCameraMovedToMyLocation){
				isCameraMovedToMyLocation = true;
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatlong, Constants.ZOOM_VALUE));
				findLatLangOnCameraChange = false;										
			}
		}	
	}

	@Override
	public void onProviderEnabled(String s) {
		if(LocationManager.GPS_PROVIDER.equals(s)){
			needLocation = true;
			if(mLatlong == mMyCoordinates){
				locationViewManager.setGpsOnPinOnMyLocationDrawable();
			}
			else{
				locationViewManager.setGpsOnDrawable();

			}
			isGPSOn = true;
		}
	}

	@Override
	public void onProviderDisabled(String s) {
		if(LocationManager.GPS_PROVIDER.equals(s)){
			isGPSOn = false;			
			locationViewManager.setGpsOffDrawable();		
		}
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CHECK_SETTINGS_REQUEST_CODE:
			switch (resultCode) {
			case Activity.RESULT_OK:
				//with below three setting onLocationFound method will be called and will move marker to my location
				isCameraMovedToMyLocation = false;
				mLatlong = null;
				needLocation = true;
				break;
			case Activity.RESULT_CANCELED:
				// this mLatlang is taken from preferences ..last place latlang
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatlong, Constants.ZOOM_VALUE));
				break;
			default:
				break;
			}
			break;
		}
	}	

	public void checkGpsAndBringPinToMyLocation() {
		if(!mInternetStatus){
			return;
		}					

		LocationManager manager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE );
		if ( manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ){
			bringPinToMyLocation();
		}
		else{
			((LocationActivity)mContext).needLocation = true;
			checkAndEnableGPS();					
		}		
	}

	protected void checkAndEnableGPS() {

		LocationRequest locReqHighPriority =  LocationRequest.create();
		locReqHighPriority.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);		

		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
		.addLocationRequest(locReqHighPriority).setAlwaysShow(true);


		PendingResult<LocationSettingsResult> result =
				LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

		result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
			@Override
			public void onResult(LocationSettingsResult result) {
				final Status status = result.getStatus();		       
				switch (status.getStatusCode()) {
				case LocationSettingsStatusCodes.SUCCESS:
					//not doing anything because onMyLocationFound will be called and move marker to my location;
					break;
				case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

					try {
						status.startResolutionForResult(
								(BaseActivity)mContext,
								CHECK_SETTINGS_REQUEST_CODE);
					} catch (SendIntentException e) {
						// Ignore the error.
					}
					break;
				case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

					break;
				}
			}
		});
	}	

	protected void OnLocationSelectionComplete(EventPlace eventPlace){}//overridden in pick location activity

	public void onListItemClicked(AutoCompletePlace item) {	
		mLh.findPlaceById(item.getPlaceId(), mGoogleApiClient, new OnSelectLocationCompleteListner() {					
			@Override
			public void OnSelectLocationComplete(Place place) {
				mEventPlace = new EventPlace(place.getName().toString(),
						place.getAddress().toString(),place.getLatLng());
				moveToSelectedLocation(mEventPlace);				
			}
		});		
	}

	public void onFavouriteListItemClicked(EventPlace ep) {
		mEventPlace = ep;
		moveToSelectedLocation(ep);		
	}

	private void moveToSelectedLocation(EventPlace ep){
		mLatlong = ep.getLatLang();
		findAddressOnCameraChange = false;
		findLatLangOnCameraChange =false;
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatlong, Constants.ZOOM_VALUE));					
		locationViewManager.setLocationText(mEventPlace.getName());
		OnLocationSelectionComplete(mEventPlace);
	}

	@Override
	public void onBackPressed() {
		if( locationViewManager.mMapView.getVisibility()==View.VISIBLE){
			super.onBackPressed();
		}
		else{
			locationViewManager.hideSearchView();
		}
		//Include the code here
		return;
	}
}
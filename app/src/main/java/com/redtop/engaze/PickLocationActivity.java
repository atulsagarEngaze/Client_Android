package com.redtop.engaze;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.redtop.engaze.adapter.NewSuggestedLocationAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.viewmanager.PickLocationViewManager;

@SuppressLint("ResourceAsColor")
public class PickLocationActivity extends MapLocationSelectionActivity implements OnMapReadyCallback {
	static LatLng currentLocation = new LatLng(12.9667, 77.5667);
	private static final String TAG = PickLocationActivity.class.getName();		
	private PickLocationViewManager pickLocationViewManager = null;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;		
		setContentView(R.layout.activity_pick_location);
		mEventPlace  =  (EventPlace)this.getIntent().getParcelableExtra("DestinatonLocation");
		mapCameraMovementHandleViewManager = new PickLocationViewManager(this);
		pickLocationViewManager = (PickLocationViewManager) mapCameraMovementHandleViewManager;
		mSuggestedLocationAdapter = new NewSuggestedLocationAdapter(this, R.layout.item_suggested_location_list, mAutoCompletePlaces);
		pickLocationViewManager.setLocationViewAdapter(mSuggestedLocationAdapter);
		SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.pick_location_map);
		fragment.getMapAsync(this);				
		gpsOnListner = null;
	}

	@Override	
	protected void OnLocationSelectionComplete(EventPlace eventPlace)
	{
		pickLocationViewManager.showLocationBar(mEventPlace.getName(), mEventPlace.getAddress());
	}	

	private void runGPSEnableThread(){		
		Thread thread = new Thread(){
			@Override
			public void run(){
				checkAndEnableGPS();
			}
		};
		thread.start();
	}	

	@Override
	public void onMapReady(GoogleMap map) {			
		mMap = map;
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setMapToolbarEnabled(false);
		mMap.setPadding(0, AppUtility.dpToPx(64, mContext), 0, 0);
		initializeMapCameraChangeListner();
		findLatLangOnCameraChange = false;
		if(mEventPlace != null)
		{			
			mapCameraMovementHandleViewManager.setLocationText(mEventPlace.getAddress());
			mMapCameraFocusLatlong = mEventPlace.getLatLang();
			pickLocationViewManager.showLocationBar(mEventPlace.getName(), mEventPlace.getAddress());			
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMapCameraFocusLatlong, Constants.ZOOM_VALUE));
			hideProgressBar();
		}
		else
		{
			if(mMyCoordinates==null){
				mMapCameraFocusLatlong = new LatLng( Double.longBitsToDouble(PreffManager.getPrefLong("lat")),
						Double.longBitsToDouble(PreffManager.getPrefLong("long")));
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMapCameraFocusLatlong, Constants.ZOOM_VALUE));
				if(AppContext.context.isInternetEnabled){
					runGPSEnableThread();			
				}			
			}
			else{			
				mMapCameraFocusLatlong = mMyCoordinates;
				isCameraMovedToMyLocation = true;
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMyCoordinates, Constants.ZOOM_VALUE));
			}
		}

		mapCameraMovementHandleViewManager.showPin();
	}

	@Override
	protected void postCameraMoved(){
		pickLocationViewManager.showLocationBar(mEventPlace.getName(), mEventPlace.getAddress());
		hideProgressBar();
	}

	public void onLocationSelection() {
		Intent intent = new Intent();		
		intent.putExtra("DestinatonPlace", (Parcelable)mEventPlace); 			
		setResult(RESULT_OK, intent);        
		finish();		
	}

	public void moveBack() {
		onBackPressed();
		finish();
	}	
}
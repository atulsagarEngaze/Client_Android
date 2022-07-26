package com.redtop.engaze;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.redtop.engaze.Interface.OnGpsSetOnListner;
import com.redtop.engaze.common.constant.IntentConstants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.common.utility.AppLocationService;
import com.redtop.engaze.restApi.proxy.LocationApiProxy;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

//updates current user address on the map
public abstract class MyCurrentLocationHandlerActivity extends BaseActivity {
	public static LatLng mMyCoordinates;
	protected static String mDistance ="";
	protected static String mDuration ="";
	protected GoogleMap mMap;
	protected AppLocationService mLh;

	private BroadcastReceiver mLocationBroadcastReceiver;
	private IntentFilter mFilter;

	//protected int markerCenterImageResId;
	protected final static int CHECK_SETTINGS_REQUEST_CODE = 8;
	protected OnGpsSetOnListner gpsOnListner= null;

	private final static String TAG = MyCurrentLocationHandlerActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLh = new AppLocationService(this, MyCurrentLocationHandlerActivity.this);
		createMyLocationReceiver();
	}

	@Override
	protected void onResume() {		
		super.onResume();

		LocalBroadcastManager.getInstance(this).registerReceiver(mLocationBroadcastReceiver,
				mFilter);
		sendBroadcastForInstantNeedOfMyLocation();

	}

	@Override
	protected void onPause() {		
		super.onPause();
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mLocationBroadcastReceiver);

	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	private  void createMyLocationReceiver(){
		mLocationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				{
					Location currentLocation = intent.getParcelableExtra(IntentConstants.CURRENT_LOCATION);
					//for testing
					LocationApiProxy.location = currentLocation;
					mMyCoordinates = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
					onMyLocationFound(currentLocation);
				}
			}
		};

		mFilter = new IntentFilter();
		mFilter.addAction(Veranstaltung.CURRENT_LOCATION_FOUND);
	}

	private void sendBroadcastForInstantNeedOfMyLocation(){
		Intent intent = new Intent(Veranstaltung.NEED_LOCATION_INSTANT);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	protected abstract void onMyLocationFound(Location location);
}

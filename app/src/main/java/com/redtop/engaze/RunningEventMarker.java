package com.redtop.engaze;

import java.util.ArrayList;

import org.w3c.dom.Document;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.GoogleDirection;
import com.redtop.engaze.common.utility.InfoWindowHelper;
import com.redtop.engaze.common.utility.MarkerHelper;
import com.redtop.engaze.domain.UsersLocationDetail;

@SuppressLint({ "ResourceAsColor", "SimpleDateFormat" })
public class RunningEventMarker  extends RunningEventBase implements OnMarkerClickListener  {

	protected ArrayList<Marker> mMarkers;
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);				
	}

	@Override
	protected void initialize(Bundle savedInstanceState){
		super.initialize(savedInstanceState);
		if(mEvent!=null){
			mMarkers = new ArrayList<Marker>();		
			mShowRouteLoadedView = false;
			mETADistanceMarkers = new ArrayList<Marker>();
			if (mEvent.destination !=null) {
				mDestinationlatlang = new LatLng(mEvent.destination.getLatitude(), mEvent.destination.getLongitude());
			}
		}
	}

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
				mETADistanceMarkers.add(MarkerHelper.drawTimeDistanceMarker(marker.getPosition(), ud, mMap,RunningEventMarker.this));
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
}

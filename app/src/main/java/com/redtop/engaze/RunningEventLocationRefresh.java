package com.redtop.engaze;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.adapter.EventDetailsOnMapAdapter;
import com.redtop.engaze.adapter.EventUserLocationAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.domain.service.ParticipantService;
import com.redtop.engaze.manager.LocationManager;

import androidx.cardview.widget.CardView;

@SuppressLint({"ResourceAsColor", "SimpleDateFormat"})
public class RunningEventLocationRefresh extends RunningEventMarker {

    private long currentThreadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (mClickedUserLocationView != null) {
            setBackgrounOfRecycleViewItem((CardView) mClickedUserLocationView, Color.TRANSPARENT);
            mClickedUserLocationView = null;
        }
        super.onResume();
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        super.initialize(savedInstanceState);
        createRunningEventDetailList();
        BindUserEventDetails();
        createRunnable();
    }

    public void createRunnable() {
        locationhandler = new Handler();
        locationRunnable = new Runnable() {
            public void run() {

                if (mEventId != null && mEvent != null) {
                    turnOnOfInternetAvailabilityMessage();
                    actBasedOnTimeLeft();
                    //loadMyCoordinates();
                    getLocationsFromServer();
                    startProgressBar();
                }
            }
        };
    }

    private void getLocationsFromServer() {
        if (!AppContext.context.isInternetEnabled) {
            Log.d(TAG, "No internet connection. Abortig fetching locations from server.");
            if (isActivityRunning) {
                locationhandler.postDelayed(locationRunnable, mLocationRefreshTime);
            }
            return;
        }

        LocationManager.getLocationsFromServer(mUserId, mEventId, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                if (isActivityRunning) {
                    onSuccessLocationonResponse(response);
                }
            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                if (isActivityRunning) {
                    locationhandler.postDelayed(locationRunnable, mLocationRefreshTime); // 60 seconds here you can give
                }
            }
        });
    }

    private void onSuccessLocationonResponse(JSONObject response) {

        //Log.d(TAG, response.toString());
        try {
            String Status = (String) response.getString("Status");
            if (Status == "true") {
                new populateLocationListWithAddress().execute(response);
            } else {
                Log.d(TAG, "Location not returned from the server");
                Toast.makeText(mContext,
                        getResources().getString(R.string.unable_locate),
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,
                    getResources().getString(R.string.message_general_exception),
                    Toast.LENGTH_LONG).show();
        } finally {

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


    private class populateLocationListWithAddress extends AsyncTask<JSONObject, Void, String> {

        @Override
        protected String doInBackground(JSONObject... params) {
            try {
                JSONObject response = params[0];
                JsonParser parser = new JsonParser();
                //mUsersLocationDetailList = (ArrayList<UsersLocationDetail>) parser.parseUserLocation(response.getJSONArray("ListOfUserLocation"));
                parser.updateUserListWithLocation(response.getJSONArray("ListOfUserLocation"), mUsersLocationDetailList, mLh, mDestinationlatlang);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }

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
            for (Marker marker : deletedMarkers) {
                mMarkers.remove(marker);
            }
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
                    if (ud.getCurrentAddress() == null || ud.getCurrentAddress() == "") {
                        ud.setCurrentAddress("fetching location..");
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
        size = (ParticipantService.getMembersbyStatusForLocationSharing(mEvent.AcceptanceStatus.getStatus(1))).size();
        if (size > 0) {
            mRunningEventDetailList.add(new UsersLocationDetail(R.drawable.ic_user_accepted, String.valueOf(size), AcceptanceStatus.getStatus(1))); // 1 is ACCEPTED
        }
        size = (ParticipantService.getMembersbyStatusForLocationSharing(mEvent, AcceptanceStatus.getStatus(-1))).size();
        if (size > 0) {
            mRunningEventDetailList.add(new UsersLocationDetail(R.drawable.ic_user_pending, String.valueOf(size), AcceptanceStatus.getStatus(-1))); // -1 is DECLINED
        }
        size = (ParticipantService.getMembersbyStatusForLocationSharing(mEvent, AcceptanceStatus.getStatus(0))).size();
        if (size > 0) {
            mRunningEventDetailList.add(new UsersLocationDetail(R.drawable.ic_user_declined, String.valueOf(size), AcceptanceStatus.getStatus(0))); // 0 is PENDING
        }

        return mRunningEventDetailList;
    }

    protected void createUserLocationList() {
        // TODO Auto-generated method stub
        mUsersLocationDetailList = new ArrayList<UsersLocationDetail>();
        mUsersLocationDetailList.addAll(UsersLocationDetail.createUserLocationListFromEventMembers(mEvent, mContext));

        for (UsersLocationDetail ud : mUsersLocationDetailList) {
            if (ParticipantService.isParticipantCurrentUser(ud.getUserId())) {
                currentUld = ud;
                break;
            }
        }
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
            if (ud != null && ParticipantService.isParticipantCurrentUser(ud.getUserId())) {
                ud.setLatitude(Double.toString(mMyCoordinates.latitude));
                ud.setLongitude(Double.toString(mMyCoordinates.longitude));
                SimpleDateFormat Simpledf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                ud.setCreatedOn(Simpledf.format(Calendar.getInstance().getTime()));
            }
        }
    }

    protected void updateUserLocationList() {

        ArrayList<UsersLocationDetail> temUldList = new ArrayList<UsersLocationDetail>();
        UsersLocationDetail tUl = null;
        temUldList.addAll(mUsersLocationDetailList);
        for (EventParticipant em : mEvent.getParticipants()) {
            Boolean isExist = false;
            tUl = null;
            for (UsersLocationDetail uld : temUldList) {

                if (uld != null && em.getUserId().equalsIgnoreCase(uld.getUserId())) {
                    uld.setAcceptanceStatus(em.getAcceptanceStatus());
                    tUl = uld;
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                if (ParticipantService.isValidForLocationSharing(mEvent, em)) {
                    mUsersLocationDetailList.add(UsersLocationDetail.createUserLocationListFromEventMember(mEvent, em));
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
        Intent intent = new Intent(mContext, RunningEventMenuOptionsActivity.class);
        intent.putExtra("UserName", uld.getUserName());
        intent.putExtra("UserId", uld.getUserId());
        intent.putExtra("EventId", mEvent.getEventId());
        intent.putExtra("AcceptanceStatus", uld.getAcceptanceStatus().getStatus());
        mContext.startActivity(intent);
        canRefreshUserLocation = false;
        mClickedUserLocationView = v;
        setBackgrounOfRecycleViewItem((CardView) mClickedUserLocationView, this.getResources().getColor(R.color.divider));
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

    private void setBackgrounOfRecycleViewItem( CardView view, int colorId){
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
}

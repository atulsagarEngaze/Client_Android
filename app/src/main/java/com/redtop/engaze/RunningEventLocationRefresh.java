package com.redtop.engaze;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import org.json.JSONArray;
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

import com.google.android.gms.maps.model.Marker;
import com.redtop.engaze.Interface.DialogDismissListener;
import com.redtop.engaze.Interface.FragmentToActivity;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.adapter.EventDetailsOnMapAdapter;
import com.redtop.engaze.adapter.EventUserLocationAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.domain.service.ParticipantService;
import com.redtop.engaze.domain.manager.LocationManager;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
            setBackgroundOfRecycleViewItem((CardView) mClickedUserLocationView, Color.TRANSPARENT);
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
                    getCurrentLocationsFromServer();
                    startProgressBar();
                }
            }
        };
    }

    private void getCurrentLocationsFromServer() {
        if (!AppContext.context.isInternetEnabled) {
            Log.d(TAG, "No internet connection. Abortig fetching locations from server.");
            if (isActivityRunning) {
                locationhandler.postDelayed(locationRunnable, mLocationRefreshTime);
            }
            return;
        }

        LocationManager.getLocationsFromServer(mUserId, mEventId, new OnAPICallCompleteListener<JSONArray>() {

            @Override
            public void apiCallSuccess(JSONArray response) {
                if (isActivityRunning) {
                    onSuccessLocationResponse(response);
                }
            }

            @Override
            public void apiCallFailure() {
                if (isActivityRunning) {
                    locationhandler.postDelayed(locationRunnable, mLocationRefreshTime); // 60 seconds here you can give
                }
            }
        });
    }

    private void onSuccessLocationResponse(JSONArray response) {

        //Log.d(TAG, response.toString());
        try {

            new populateLocationListWithAddress().execute(response);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,
                    getResources().getString(R.string.message_general_exception),
                    Toast.LENGTH_LONG).show();
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

    private class populateLocationListWithAddress extends AsyncTask<JSONArray, Void, String> {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected String doInBackground(JSONArray... jsonObjects) {
            try {
                JSONObject locFromServerObj;
                UsersLocationDetail udFromServer;
                JSONArray userLocationsJsonArray = jsonObjects[0];
                ArrayList<UsersLocationDetail> userLocationsFromServer = new ArrayList<>();
                for (int i = 0; i < userLocationsJsonArray.length(); i++) {
                    locFromServerObj = userLocationsJsonArray.getJSONObject(i);
                    udFromServer = AppContext.jsonParser.deserialize(locFromServerObj.get("location").toString(), UsersLocationDetail.class);
                    udFromServer.userId = locFromServerObj.get("userId").toString();
                    userLocationsFromServer.add(udFromServer);
                }

                ParticipantService.updateUserListWithLocation(userLocationsFromServer, mUsersLocationDetailList, mDestinationlatlang);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
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
            mMarkers.removeAll(deletedMarkers);
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
                    if (ud.address == null || ud.address == "") {
                        ud.address = "fetching location..";
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
        size = (ParticipantService.getMembersbyStatusForLocationSharing(mEvent, AcceptanceStatus.Accepted)).size();
        if (size > 0) {
            mRunningEventDetailList.add(new UsersLocationDetail(R.drawable.ic_user_accepted, String.valueOf(size), AcceptanceStatus.getStatus(1))); // 1 is ACCEPTED
        }
        size = (ParticipantService.getMembersbyStatusForLocationSharing(mEvent, AcceptanceStatus.Pending)).size();
        if (size > 0) {
            mRunningEventDetailList.add(new UsersLocationDetail(R.drawable.ic_user_pending, String.valueOf(size), AcceptanceStatus.getStatus(-1))); // -1 is DECLINED
        }
        size = (ParticipantService.getMembersbyStatusForLocationSharing(mEvent, AcceptanceStatus.Rejected)).size();
        if (size > 0) {
            mRunningEventDetailList.add(new UsersLocationDetail(R.drawable.ic_user_declined, String.valueOf(size), AcceptanceStatus.getStatus(0))); // 0 is PENDING
        }

        return mRunningEventDetailList;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void createUserLocationList() {
        // TODO Auto-generated method stub
        mUsersLocationDetailList = new ArrayList<UsersLocationDetail>();
        mUsersLocationDetailList.addAll(UsersLocationDetail.createUserLocationListFromEventMembers(mEvent));
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
            if (ud != null && ParticipantService.isParticipantCurrentUser(ud.userId)) {
                ud.latitude = mMyCoordinates.latitude;
                ud.longitude = mMyCoordinates.longitude;
                if (ud.name == null || ud.name == "" || ud.name == Constants.LOCATION_UNKNOWN) {
                    ud.name = "fetching..";
                }
                if (ud.address == null || ud.address == "" || ud.address == Constants.LOCATION_UNKNOWN) {
                    ud.address = "fetching..";
                }
                SimpleDateFormat Simpledf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                ud.createdOn = Simpledf.format(Calendar.getInstance().getTime());
            }
        }
    }

    protected void updateUserLocationList() {

        ArrayList<UsersLocationDetail> temUldList = new ArrayList<>();
        UsersLocationDetail tUl = null;
        temUldList.addAll(mUsersLocationDetailList);
        for (EventParticipant em : mEvent.participants) {

            Boolean isExist = false;
            tUl = null;
            for (UsersLocationDetail uld : temUldList) {
                if (uld.userId != null && uld.userId.equalsIgnoreCase(em.userId)) {
                    uld.acceptanceStatus = em.acceptanceStatus;
                    tUl = uld;
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                if (ParticipantService.isValidForLocationSharing(mEvent, em)) {
                    mUsersLocationDetailList.add(UsersLocationDetail.createUserLocationListFromEventMember(em));
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
        FragmentManager fm = ((BaseActivity) mContext).getSupportFragmentManager();
        RunningEventParticipantMenuOptionsFragment fragment = RunningEventParticipantMenuOptionsFragment.newInstance(
                uld.userName, uld.userId, mEvent.eventId, uld.acceptanceStatus.getStatus());
        fragment.show(fm, "RunningEventParticipantMenuOptions");

        fragment.dialogDismissListener = () -> {

            if (mClickedUserLocationView != null) {
                setBackgroundOfRecycleViewItem((CardView) mClickedUserLocationView, Color.TRANSPARENT);
                mClickedUserLocationView = null;
            }
        };
        canRefreshUserLocation = false;
        mClickedUserLocationView = v;
        setBackgroundOfRecycleViewItem((CardView) mClickedUserLocationView, this.getResources().getColor(R.color.primaryLight));
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

    private void setBackgroundOfRecycleViewItem(CardView view, int colorId) {
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

package com.redtop.engaze;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.redtop.engaze.Interface.FragmentToActivity;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.utility.GoogleDirection;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.domain.manager.ParticipantManager;
import com.redtop.engaze.domain.service.ParticipantService;

@SuppressLint("ResourceAsColor")
public class RunningEventActivityResults extends RunningEventLocationRefresh implements FragmentToActivity<Duration> {
    private BaseActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
        super.initialize(savedInstanceState);
        activity = this;
    }

    @Override
    public void communicate(Duration duration, Fragment source) {
        showProgressBar(getResources().getString(R.string.message_general_progressDialog));
        mSnooze = duration;
        EventManager.extendEventEndTime(mSnooze.getTimeInterval(), mContext, mEvent, action -> {
            UpdateTimeLeftItemOfRunningEventDetailsDataSet();
            mEventDetailAdapter.notifyDataSetChanged();
            AppContext.actionHandler.actionComplete(action);
            locationhandler.post(locationRunnable);
        }, (msg, action) -> onActionFailed(msg, action));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            showProgressBar(getResources().getString(R.string.message_general_progressDialog));

            switch (requestCode) {

                case Constants.ROUTE_END_POINT_REQUEST_CODE:

                    final LatLng endpoint = data.getParcelableExtra("endpoint");
                    final LatLng startPoint = mCurrentMarker.getPosition();
                    GoogleDirection gd = new GoogleDirection(mContext);
                    gd.setOnDirectionResponseListener((GoogleDirection.OnDirectionResponseListener) (status, doc, gd1) -> {
                        hideProgressBar();
                        if (status.equals("OK")) {
                            mShowRouteLoadedView = true;
                            mRouteStartUd = markerUserLocation.get(mCurrentMarker);
                            mRouteEndUd = markerUserLocation.get(findMarkerByLatLang(endpoint));
                            final String distance = gd1.getTotalDistanceText(doc);
                            final String duration = gd1.getTotalDurationText(doc);
                            //						gd.animateDirection(mMap, gd.getDirection(doc), GoogleDirection.SPEED_VERY_FAST
                            //								, true, true, true, false, null, false, true, new PolylineOptions().width(5).color(mContext.getResources().getColor(R.color.primaryDark)));
                            if (mPreviousPolyline != null) {
                                mPreviousPolyline.remove();
                            }
                            mPreviousPolyline = gd1.loadRoute(mMap, gd1.getDirection(doc), new PolylineOptions().width(15).color(mContext.getResources().getColor(R.color.primaryDark)));

                            mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

                                @Override
                                public View getInfoWindow(Marker arg0) {
                                    // TODO Auto-generated method stub
                                    return null;
                                }

                                @Override
                                public View getInfoContents(Marker arg0) {
                                    View markerInfoWindow = getLayoutInflater().inflate(R.layout.custom_snippet_with_no_address, null);

                                    markerInfoWindow.setLayoutParams(new RelativeLayout.LayoutParams(300, RelativeLayout.LayoutParams.WRAP_CONTENT));
                                    TextView txtDistance = (TextView) markerInfoWindow.findViewById(R.id.distance_endpoint);
                                    txtDistance.setText("Distance " + distance);

                                    TextView eta = (TextView) markerInfoWindow.findViewById(R.id.eta_endpoint);
                                    eta.setText("ETA " + duration);


                                    return markerInfoWindow;
                                }
                            });

                            Marker endPointMarker = findMarkerByLatLang(endpoint);
                            if (endPointMarker != null) {
                                endPointMarker.showInfoWindow();
                                mEnableAutoCameraAdjust = true;
                                adjustMapForLoadedRoute();
                            }
                        } else {
                            Toast.makeText(mContext,
                                    "No Route Found!!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    gd.request(startPoint, endpoint, GoogleDirection.MODE_DRIVING);
                    break;

                case ADDREMOVE_INVITEES_REQUEST_CODE:
                    showProgressBar(getResources().getString(R.string.message_general_progressDialog));
                    mContactsAndgroups = PreffManager.getPrefArrayList("Invitees");
                    ParticipantManager.addRemoveParticipants(mContactsAndgroups, mEvent, action -> {
                        mEvent = EventManager.getEvent(mEventId, true);
                        updateRecyclerViews();
                        AppContext.actionHandler.actionComplete(action);
                        locationhandler.post(locationRunnable);
                    }, (msg, action) -> onActionFailed(msg, action));

                    break;
                case UPDATE_LOCATION_REQUEST_CODE:
                    showProgressBar(getResources().getString(R.string.message_general_progressDialog));
                    mDestinationPlace = data.getParcelableExtra("DestinatonPlace");

                    EventManager.changeDestination(mDestinationPlace, mContext, mEvent, action -> {
                        if (mEvent.destination != null) {
                            mDestinationlatlang = new LatLng(mEvent.destination.getLatitude(), mEvent.destination.getLongitude());
                        }
                        removeRoute();
                        createDestinationMarker();
                        mEnableAutoCameraAdjust = true;
                        showAllMarkers();
                        actionCompleted(action);
                        locationhandler.post(locationRunnable);
                    }, (msg, action) -> {
                        onActionFailed(msg, action);
                    });
                    break;
            }
        }

        fbHelper.getFBCallbackManager().onActivityResult(requestCode, resultCode, data);
    }


    private void onActionFailed(String msg, Action action) {
        if (action == Action.EXTENDEVENTENDTIME || action == Action.ADDREMOVEPARTICIPANTS
                || action == Action.CHANGEDESTINATION) {
            locationhandler.post(locationRunnable);
        }
        actionFailed(msg, action);
    }
}

package com.redtop.engaze.service;

import java.util.ArrayList;

import org.json.JSONObject;
import org.w3c.dom.Document;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.app.Config;
import com.redtop.engaze.common.enums.ReminderFrom;
import com.redtop.engaze.common.utility.GoogleDirection;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.manager.EventNotificationManager;
import com.redtop.engaze.domain.manager.LocationManager;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class EventDistanceReminderService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Context mContext;
    private Event mEvent;
    private EventParticipant mMember;
    private LatLng reminderStartLatLng;
    private LatLng reminderEndLatLng;
    private String mReminderId;
    private Handler mDistancetCheckHandler;
    private Runnable mDistancetCheckRunnable;
    private Boolean mReceivedLocation = false;
    private GoogleApiClient mGoogleApiClient;
    private static int durationTillIntervalSetHalf = 60;//seconds
    private static int intervalPostdurationTillIntervalSetHalf = 15;

    public static final String TAG = EventDistanceReminderService.class.getName();
    private LocationRequest mLocationRequest;

    public EventDistanceReminderService() {
        super(TAG);
        mDistancetCheckHandler = new Handler();
        mDistancetCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (!checkValidityOfReminder()) {
                    return;
                } else {
                    getParticipantLocationsFromServer();
                }
            }
        };
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            mContext = this;
            mEvent = InternalCaching.getEventFromCache(intent.getStringExtra("EventId"));
            mMember = mEvent.getParticipant(intent.getStringExtra("MemberId"));
            mReminderId = mMember.getDistanceReminderId();
            mDistancetCheckHandler.post(mDistancetCheckRunnable);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
        }
    }

    private void createGoogleAPIClientAndLocationRequest() {
        Log.v(TAG, "Creating Google Api Client");
        mGoogleApiClient =
                new GoogleApiClient.Builder(mContext).build();
        mGoogleApiClient.connect();

        Log.v(TAG, "Creating Location Request");
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(Config.LOCATION_REFRESH_INTERVAL_FAST)        // 10 seconds, in milliseconds
                .setFastestInterval(Config.LOCATION_REFRESH_INTERVAL_FAST);
    }

    @Override
    public void onLocationChanged(Location location) {
        synchronized (this) {
            if (mReceivedLocation) {
                return;
            }
            mReceivedLocation = true;
        }

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        reminderEndLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        getDistanceForReminderDistanceCalculation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed with code " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.i(TAG, "Location services connected.");
        if (location != null) {
            mReceivedLocation = true;
        }
        reminderEndLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        getDistanceForReminderDistanceCalculation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    private void getParticipantLocationsFromServer() {
        LocationManager.getLocationsFromServer(mMember.getUserId(), mEvent.eventId, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                Log.d(TAG, response.toString());
                String Status = "";
                try {
                    Status = (String) response.getString("Status");

                    if (Status == "true") {
                        JSONObject c = response.getJSONArray("ListOfUserLocation").getJSONObject(0);
                        reminderStartLatLng = new LatLng(Double.parseDouble(c.getString("Latitude")), Double.parseDouble(c.getString("Longitude")));
                        if (mMember.getReminderFrom() == ReminderFrom.SELF) {
                            createGoogleAPIClientAndLocationRequest();
                        } else {
                            if (mMember.getReminderFrom() == ReminderFrom.DESTINATION) {
                                reminderEndLatLng = new LatLng(mEvent.destination.getLatitude(), mEvent.destination.getLongitude());
                                getDistanceForReminderDistanceCalculation();
                            } else {
                                Log.v(TAG, "This option is yet not implemented");
                            }
                        }
                    } else {
                        Log.v(TAG, "Unable to get the location of participant from server.");
                        Log.v(TAG, "Service will retry after 15 seconds");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void apiCallFailure() {
                Log.v(TAG, "Unable to get the location of participant from server.");
                Log.v(TAG, "Service will retry after 15 seconds");
            }
        });
    }

    private void getDistanceForReminderDistanceCalculation() {
        GoogleDirection mGd = new GoogleDirection(mContext);
        mGd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {

            @Override
            public void onResponse(String status, Document doc,
                                   GoogleDirection gd) {

                checkReminder(gd, doc);
            }
        });
        mGd.request(reminderStartLatLng, reminderEndLatLng, GoogleDirection.MODE_DRIVING);

    }

    private void checkReminder(GoogleDirection gd, Document doc) {
        if (gd.getTotalDistanceValue(doc) <= mMember.getDistanceReminderDistance()) {
            int actual = gd.getTotalDistanceValue(doc) / 1000;
            String notificationMessage = "";
            if (actual > 1) {
                notificationMessage = mMember.getProfileName() + " is just " + actual + " Kms away!";
            } else {
                notificationMessage = mMember.getProfileName() + " is just " + gd.getTotalDistanceValue(doc) + " mtrs away!";
            }
            EventNotificationManager.approachingAlertNotification(mContext, mEvent, notificationMessage);

            mEvent.ReminderEnabledMembers.remove(mEvent.ReminderEnabledMembers.indexOf(mMember));
            InternalCaching.saveEventToCache(mEvent);
        } else {
            int duration = gd.getTotalDurationValue(doc);
            int postDelayTime = intervalPostdurationTillIntervalSetHalf;
            if (duration > durationTillIntervalSetHalf) {
                postDelayTime = duration / 2;
            }
            //mDistancetCheckHandler.postDelayed(mDistancetCheckRunnable, 10*1000);
            mDistancetCheckHandler.postDelayed(mDistancetCheckRunnable, postDelayTime * 1000);
        }
    }

    private Boolean checkValidityOfReminder() {

        mEvent = InternalCaching.getEventFromCache(mEvent.eventId);
        if (mEvent == null) {
            return false;
        }
        mMember = mEvent.getParticipant(mMember.getUserId());
        if (mMember == null) {
            return false;
        }

        ArrayList<EventParticipant> reminderEnabledMem = mEvent.ReminderEnabledMembers;
        if (!(reminderEnabledMem != null && reminderEnabledMem.size() > 0
                && reminderEnabledMem.contains(mMember))) {
            return false;
        }

        if (!mReminderId.equalsIgnoreCase(mMember.getDistanceReminderId())) {
            return false;
        }
        String destLat = Double.toString(mEvent.destination.getLatitude());
        if (mMember.getReminderFrom() == ReminderFrom.DESTINATION &&
                (destLat == null || destLat == "")) {
            return false;
        }
        return true;
    }

    //	public static List<EventDetail> getDistanceReminderEnabledEvents(Context context){
    //		List<EventDetail> events = InternalCaching.getEventListFromCache(context);
    //		List<EventDetail> reminderEvents = new ArrayList<EventDetail>();
    //		if(events==null){
    //			return null;
    //		}
    //
    //		for(EventDetail ed : events){
    //			if(ed.getState().equals(Constants.TRACKING_ON) && ed.isDistanceReminderSet){
    //				reminderEvents.add(ed);
    //			}
    //		}
    //
    //		return reminderEvents;
    //	}

}


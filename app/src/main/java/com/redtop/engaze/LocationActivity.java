package com.redtop.engaze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.redtop.engaze.adapter.CachedLocationAdapter;
import com.redtop.engaze.adapter.NewSuggestedLocationAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.cache.DestinationCacher;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.domain.AutoCompletePlace;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.viewmanager.LocationViewManager;


public abstract class LocationActivity extends BaseLocationActivity implements LocationListener {
    protected LocationViewManager locationViewManager = null;
    public NewSuggestedLocationAdapter mSuggestedLocationAdapter;
    public CachedLocationAdapter mCachedLocationAdapter;
    public ArrayList<Marker> mMarkers = new ArrayList<Marker>();
    public LatLng mLatlong = null;
    public EventPlace mEventPlace;
    public ArrayList<AutoCompletePlace> mAutoCompletePlaces = new ArrayList<AutoCompletePlace>();
    public String mOriginalQuery = "";
    public Boolean isMapSetToMyLocation = false;
    public Boolean findLatLangOnCameraChange = true;
    public Boolean findAddressOnCameraChange = true;
    public Boolean isCameraMovedToMyLocation = false;
    public Boolean isOnload = true;
    public Boolean needLocation = true;
    public Boolean isGPSOn = false;
    public Boolean isImageSetToGray = false;
    public LocationManager mLocationManager;
    protected PlacesClient placesClient = null;

    private final static String TAG = LocationActivity.class.getName();

    protected void createEventPlace() {
        Place place = mLh.getPlaceFromLatLang(mLatlong);
        mEventPlace = null;
        if (place != null) {
            mEventPlace = new EventPlace(place.getName().toString(),
                    place.getAddress().toString(), place.getLatLng());
        }

        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_map_access_key));
        // Create a new Places client instance.
        placesClient = Places.createClient(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
        super.onPause();
        mLocationManager.removeUpdates(this);

    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, (LocationListener) this);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isGPSOn = true;
        } else {
            isGPSOn = false;
        }
        if (mCachedLocationAdapter != null) {
            mCachedLocationAdapter.mItems = DestinationCacher.getDestinationsFromCache(mContext);
            locationViewManager.setCacheLocationListAdapter(mCachedLocationAdapter);
        }

    }

    protected void bringPinToMyLocation() {
        try {
            //myImageButton.setVisibility(View.GONE);
            Location location = mLh.getMyLocation2(mGoogleApiClient);
            if (location != null) {
                mLatlong = new LatLng(location.getLatitude(), location.getLongitude());
                mMyCoordinates = mLatlong;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatlong, Constants.ZOOM_VALUE));
                findLatLangOnCameraChange = false;
            } else {
                mLatlong = null;
                isCameraMovedToMyLocation = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void myLocationButtonClicked() {
    }

    protected void postCameraMoved() {
    }

    protected void initializeMapCameraChangeListner() {
        mMap.setOnCameraChangeListener(new OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition arg0) {

                if (findLatLangOnCameraChange) {
                    mLatlong = mMap.getCameraPosition().target;
                } else {
                    findLatLangOnCameraChange = true;
                }
                if (isGPSOn) {
                    if (mMyCoordinates == mLatlong && mMyCoordinates != null && mLatlong != null) {
                        locationViewManager.setGpsOnPinOnMyLocationDrawable();
                        PreffManager.setPrefLong("lat", Double.doubleToLongBits(mLatlong.latitude));
                        PreffManager.setPrefLong("long", Double.doubleToLongBits(mLatlong.longitude));
                    } else {
                        locationViewManager.setGpsOnDrawable();
                    }
                }

                try {
                    if (findAddressOnCameraChange) {
                        new CameraChangeGetPlace().execute();
                    } else {
                        findAddressOnCameraChange = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getAutoCompletePlacePredictions(CharSequence query) {
        if (!AppContext.context.isInternetEnabled) {
            return;
        }
        String newQuery = query.toString();
        Location location = new Location("");
        location.setLatitude(mLatlong.latitude);
        location.setLongitude(mLatlong.longitude);


        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
// and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
// Create a RectangularBounds object.
        RectangularBounds bounds = mLh.getLatLongBounds(location);
// Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
// Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(bounds)
                //.setLocationRestriction(bounds)
                .setSessionToken(token)
                .setQuery(query.toString())
                .build();


        placesClient.findAutocompletePredictions(request).addOnSuccessListener(
                (response) -> {
                    OnAutoCompleteSuccess(response.getAutocompletePredictions());
                }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });

    }

    private void OnAutoCompleteSuccess(List<AutocompletePrediction> predictions) {
        if (predictions == null || predictions.size() == 0)
            return;
        mAutoCompletePlaces.clear();


        for (AutocompletePrediction prediction : predictions) {
            //Add as a new item to avoid IllegalArgumentsException when buffer is released
            mAutoCompletePlaces.add(new AutoCompletePlace(prediction.getPlaceId(), prediction.getFullText(null).toString()));
        }

        mSuggestedLocationAdapter.mItems = mAutoCompletePlaces;
        mSuggestedLocationAdapter.notifyDataSetChanged();
    }

    private class CameraChangeGetPlace extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                createEventPlace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {

            if (mEventPlace == null) {//when network is slow, or google service is down
                turnOnOfInternetAvailabilityMessage();

                turnOnOfLocationAvailabilityMessage(false);

//				Toast.makeText(mContext,
//						getResources().getString(R.string.unable_locate_address),
//						Toast.LENGTH_LONG).show();
                Log.d(TAG, "Connection is slow, unable to fetch address");
                hideProgressBar();
                return;
            }
            turnOnOfLocationAvailabilityMessage(true);
            //new CameraChangeGetPlace().execute();
            locationViewManager.setLocationText(mEventPlace.getName());
            locationViewManager.setLocationNameAndAddress(mEventPlace.getName(), mEventPlace.getAddress());
            postCameraMoved();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    @Override
    protected void onMyLocationFound(Location location) {
        if (mMap == null) {
            return;//this may call before map is loaded
        }
        synchronized (this) {
            if (!needLocation) {
                return;
            } else {
                needLocation = false;
            }
        }

        mMyCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        if ((this.getClass().getSimpleName().equals(HomeActivity.class.getSimpleName()))
                || (this.getClass().getSimpleName() == PickLocationActivity.class.getName() && mLatlong == null)) {
            mLatlong = mMyCoordinates;
            if (!isCameraMovedToMyLocation) {
                isCameraMovedToMyLocation = true;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatlong, Constants.ZOOM_VALUE));
                findLatLangOnCameraChange = false;
            }
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        if (LocationManager.GPS_PROVIDER.equals(s)) {
            needLocation = true;
            if (mLatlong == mMyCoordinates) {
                locationViewManager.setGpsOnPinOnMyLocationDrawable();
            } else {
                locationViewManager.setGpsOnDrawable();

            }
            isGPSOn = true;
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        if (LocationManager.GPS_PROVIDER.equals(s)) {
            isGPSOn = false;
            locationViewManager.setGpsOffDrawable();
        }
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

    private void turnOnOfLocationAvailabilityMessage(Boolean locationAvailable) {
        // using the same Internet status layout to display the location unavailability message.
        View v = findViewById(R.id.internet_status);
        if (v != null) {

            LinearLayout locationStatusLayout = (LinearLayout) v;
            if (locationAvailable) {
                if (locationStatusLayout != null) {
                    locationStatusLayout.setVisibility(View.GONE);
                }
            } else {
                if (locationStatusLayout != null) {
                    TextView locationAvailabilityTxt = (TextView) findViewById(R.id.txt_internet_unavailable_message);
                    locationAvailabilityTxt.setText(getResources().getString(R.string.unable_locate_address));
                    locationStatusLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void checkGpsAndBringPinToMyLocation() {
        if (!AppContext.context.isInternetEnabled) {
            return;
        }

        LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            bringPinToMyLocation();
        } else {
            ((LocationActivity) mContext).needLocation = true;
            checkAndEnableGPS();
        }
    }

    protected void checkAndEnableGPS() {

        LocationRequest locReqHighPriority = LocationRequest.create();
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
                                    (BaseActivity) mContext,
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

    protected void OnLocationSelectionComplete(EventPlace eventPlace) {
    }//overridden in pick location activity

    public void onListItemClicked(AutoCompletePlace item) {

// Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

// Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.builder(item.getPlaceId(), placeFields)
                .build();

        // Add a listener to handle the response.
                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
                    mEventPlace = new EventPlace(place.getName(),
                            place.getAddress(), place.getLatLng());
                    moveToSelectedLocation(mEventPlace);
            Log.i(TAG, "Place found: " + place.getName());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
        });
    }

    public void onFavouriteListItemClicked(EventPlace ep) {
        mEventPlace = ep;
        moveToSelectedLocation(ep);
    }

    private void moveToSelectedLocation(EventPlace ep) {
        mLatlong = ep.getLatLang();
        findAddressOnCameraChange = false;
        findLatLangOnCameraChange = false;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatlong, Constants.ZOOM_VALUE));
        locationViewManager.setLocationText(mEventPlace.getName());
        OnLocationSelectionComplete(mEventPlace);
    }

    @Override
    public void onBackPressed() {
        if (locationViewManager.mMapView.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
        } else {
            locationViewManager.hideSearchView();
        }
        //Include the code here
        return;
    }
}
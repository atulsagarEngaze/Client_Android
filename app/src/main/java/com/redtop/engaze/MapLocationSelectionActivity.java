package com.redtop.engaze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
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
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.PermissionRequester;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.cache.DestinationCacher;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.domain.AutoCompletePlace;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.viewmanager.MapCameraMovementHandleViewManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import static com.redtop.engaze.common.constant.RequestCode.Permission.ACCESS_BACKGROUND_LOCATION;
import static com.redtop.engaze.common.constant.RequestCode.Permission.SEND_SMS;


public abstract class MapLocationSelectionActivity extends MyCurrentLocationHandlerActivity implements LocationListener {
    protected MapCameraMovementHandleViewManager mapCameraMovementHandleViewManager = null;
    public NewSuggestedLocationAdapter mSuggestedLocationAdapter;
    public CachedLocationAdapter mCachedLocationAdapter;
    public ArrayList<Marker> mMarkers = new ArrayList<Marker>();
    public LatLng mMapCameraFocusLatlong = null;
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
    AutocompleteSessionToken token;

    private final static String TAG = MapLocationSelectionActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mCachedLocationAdapter = new CachedLocationAdapter(mContext, R.layout.item_cached_location_list, DestinationCacher.getDestinationsFromCache(mContext));
                mapCameraMovementHandleViewManager.setCacheLocationListAdapter(mCachedLocationAdapter);
            }
        });
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_map_access_key));
        // Create a new Places client instance.
        placesClient = Places.createClient(this);
        token = AutocompleteSessionToken.newInstance();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(PermissionRequester.CheckPermission(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_BACKGROUND_LOCATION,this)){
            requestLocationUpdate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ACCESS_BACKGROUND_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocationUpdate();
                } else {

                    ArrayList<String> permissionNotGranted = PermissionRequester.permissionsNotGranted(permissions);
                    PermissionRequester.showMandatoryPermissionAlertDialogAndCloseTheApp(permissionNotGranted, this);
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdate(){
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, (LocationListener) this);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isGPSOn = true;
        } else {
            isGPSOn = false;
        }
        if (mCachedLocationAdapter != null) {
            mCachedLocationAdapter.mItems = DestinationCacher.getDestinationsFromCache(mContext);
            mapCameraMovementHandleViewManager.setCacheLocationListAdapter(mCachedLocationAdapter);
        }
    }

    protected void bringPinToMyLocation() {
        try {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {

                if (location != null) {
                    mMyCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
                    mMapCameraFocusLatlong = mMyCoordinates;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMapCameraFocusLatlong, Constants.ZOOM_VALUE));
                    findLatLangOnCameraChange = false;
                } else {
                    mMapCameraFocusLatlong = null;
                    isCameraMovedToMyLocation = false;
                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void postCameraMoved();

    protected void initializeMapCameraChangeListner() {
        mMap.setOnCameraChangeListener(new OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition arg0) {

                if (findLatLangOnCameraChange) {
                    mMapCameraFocusLatlong = mMap.getCameraPosition().target;
                } else {
                    findLatLangOnCameraChange = true;
                }
                if (isGPSOn) {
                    if (mMyCoordinates == mMapCameraFocusLatlong && mMyCoordinates != null && mMapCameraFocusLatlong != null) {
                        mapCameraMovementHandleViewManager.setGpsOnPinOnMyLocationDrawable();
                        PreffManager.setPrefLong("lat", Double.doubleToLongBits(mMapCameraFocusLatlong.latitude));
                        PreffManager.setPrefLong("long", Double.doubleToLongBits(mMapCameraFocusLatlong.longitude));
                    } else {
                        mapCameraMovementHandleViewManager.setGpsOnDrawable();
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
        location.setLatitude(mMapCameraFocusLatlong.latitude);
        location.setLongitude(mMapCameraFocusLatlong.longitude);


        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).

        // Create a RectangularBounds object.
        RectangularBounds bounds = mLh.getLatLongBounds(location);
        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                //.setLocationBias(bounds)
                .setLocationRestriction(bounds)
                .setSessionToken(token)
                .setQuery(query.toString())
                .build();


        placesClient.findAutocompletePredictions(request).addOnSuccessListener(
                (response) -> {
                    OnAutoCompleteSuccess(response.getAutocompletePredictions());
                }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                AppUtility.showAlert(mContext, "\"Place not found", Integer.toString(apiException.getStatusCode()));
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });

    }

    private void OnAutoCompleteSuccess(List<AutocompletePrediction> predictions) {

        mAutoCompletePlaces.clear();
        if (!(predictions == null || predictions.size() == 0)) {

            for (AutocompletePrediction prediction : predictions) {
                //Add as a new item to avoid IllegalArgumentsException when buffer is released
                mAutoCompletePlaces.add(new AutoCompletePlace(prediction.getPlaceId(), prediction.getFullText(null).toString()));
            }
        }

        //mSuggestedLocationAdapter.mItems = mAutoCompletePlaces;
        mSuggestedLocationAdapter.notifyDataSetChanged();
    }

    private class CameraChangeGetPlace extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            return "";
        }

        @Override
        protected void onPostExecute(String result) {

            Place place = mLh.getPlaceFromLatLang(mMapCameraFocusLatlong);
            if (place != null) {
                mEventPlace = new EventPlace(place.getName(), place.getAddress(), place.getLatLng());
            }

            if (mEventPlace == null) {//when network is slow, or google service is down
                turnOnOfInternetAvailabilityMessage();

                turnOnOfLocationAvailabilityMessage(false);

//				Toast.makeText(mContext,
//						getResources().getString(R.string.unable_locate_address),
//						Toast.LENGTH_LONG).show();
                Log.d(TAG, "Connection is slow, unable to fetch address");
                hideProgressBar();
                return;
            } else {
                turnOnOfLocationAvailabilityMessage(true);
                //new CameraChangeGetPlace().execute();
                mapCameraMovementHandleViewManager.setLocationText(mEventPlace.getName());
                mapCameraMovementHandleViewManager.setLocationNameAndAddress(mEventPlace.getName(), mEventPlace.getAddress());

            }

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
                || (this.getClass().getSimpleName() == PickLocationActivity.class.getName() && mMapCameraFocusLatlong == null)) {
            mMapCameraFocusLatlong = mMyCoordinates;
            if (!isCameraMovedToMyLocation) {
                isCameraMovedToMyLocation = true;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMapCameraFocusLatlong, Constants.ZOOM_VALUE));
                findLatLangOnCameraChange = false;
            }
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        if (LocationManager.GPS_PROVIDER.equals(s)) {
            needLocation = true;
            if (mMapCameraFocusLatlong == mMyCoordinates) {
                mapCameraMovementHandleViewManager.setGpsOnPinOnMyLocationDrawable();
            } else {
                mapCameraMovementHandleViewManager.setGpsOnDrawable();

            }
            isGPSOn = true;
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        if (LocationManager.GPS_PROVIDER.equals(s)) {
            isGPSOn = false;
            mapCameraMovementHandleViewManager.setGpsOffDrawable();
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
                        mMapCameraFocusLatlong = null;
                        needLocation = true;
                        break;
                    case Activity.RESULT_CANCELED:
                        // this mLatlang is taken from preferences ..last place latlang
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMapCameraFocusLatlong, Constants.ZOOM_VALUE));
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
            ((MapLocationSelectionActivity) mContext).needLocation = true;
            checkAndEnableGPS();
        }
    }

    protected void checkAndEnableGPS() {

        LocationRequest locReqHighPriority = LocationRequest.create();
        locReqHighPriority.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locReqHighPriority).setAlwaysShow(true);

        Task<LocationSettingsResponse> task =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // no work needed here

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        (BaseActivity) mContext,
                                        CHECK_SETTINGS_REQUEST_CODE);
                            } catch (SendIntentException e) {
                                //Ignore the error.
                            }

                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.

                            break;
                    }
                }
            }
        });
    }

    protected void OnLocationSelectionComplete(EventPlace eventPlace) {
    }//overridden in pick location activity

    public void onListItemClicked(AutoCompletePlace item) {

// Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

// Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.builder(item.getPlaceId(), placeFields)
                .setSessionToken(token)
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
        mMapCameraFocusLatlong = ep.getLatLang();
        findAddressOnCameraChange = false;
        findLatLangOnCameraChange = false;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMapCameraFocusLatlong, Constants.ZOOM_VALUE));
        mapCameraMovementHandleViewManager.setLocationText(mEventPlace.getName());
        OnLocationSelectionComplete(mEventPlace);
    }

    @Override
    public void onBackPressed() {
        if (mapCameraMovementHandleViewManager.mMapView.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
        } else {
            mapCameraMovementHandleViewManager.hideSearchView();
        }
        //Include the code here
        return;
    }

    @Override
    public void onLocationChanged(Location location) {
        mMapCameraFocusLatlong = new LatLng(location.getLatitude(), location.getLongitude());

    }

}
package com.redtop.engaze.common;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.redtop.engaze.Interface.OnSelectLocationCompleteListner;
import com.redtop.engaze.adapter.SuggestedLocationAdapter;
import com.redtop.engaze.domain.EventPlace;

public class AppLocationService {

    private SuggestedLocationAdapter mAdapter;
    private String TAG;
    private Context mContext;
    static final int PLACE_PICKER_REQUEST = 1;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    Activity activity;

    // Prevents instantiation.
    public AppLocationService(Context context, Activity ac) {
        mContext = context;
        activity = ac;
    }

    // Prevents instantiation.
    public AppLocationService(Context context) {
        mContext = context;
    }


    public void findPlaceById(String id, GoogleApiClient mGoogleApiClient, final OnSelectLocationCompleteListner listner) {
        if (TextUtils.isEmpty(id) || mGoogleApiClient == null || !mGoogleApiClient.isConnected())
            return;

        Places.GeoDataApi.getPlaceById(mGoogleApiClient, id).setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (places.getStatus().isSuccess()) {
                    Place place = places.get(0);
                    if (mAdapter != null) {
                        mAdapter.clear();
                    }
                    listner.OnSelectLocationComplete(place);
                }

                //Release the PlaceBuffer to prevent a memory leak
                places.release();


            }
        });
    }

    public void getCurrentPlace(GoogleApiClient mGoogleApiClient, final OnSelectLocationCompleteListner listner) {
        try {
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                    PlaceLikelihood currentPlaceLikelihood = null;
                    if (null != likelyPlaces) {
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            if (currentPlaceLikelihood == null) {
                                currentPlaceLikelihood = placeLikelihood;
                            } else {
                                if (placeLikelihood.getLikelihood() > currentPlaceLikelihood.getLikelihood()) {
                                    currentPlaceLikelihood = placeLikelihood;
                                }
                            }
                        }

                        if (currentPlaceLikelihood != null && currentPlaceLikelihood.getPlace() != null) {
                            listner.OnSelectLocationComplete(currentPlaceLikelihood.getPlace());
                        } else {
                            listner.OnSelectLocationComplete(null);
                        }

                        likelyPlaces.release();
                    }
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "GooglePlayServicesNotAvailableException thrown" + e.toString());
        }
    }

    public void displayPlace(EventPlace place, TextView mEventLocation) {
        mEventLocation.setText(place.getName());
    }

    public LatLngBounds getLatLongBounds(Location location) {
        double radiusDegrees = .25;
        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng northEast = new LatLng(center.latitude + radiusDegrees, center.longitude + radiusDegrees);
        LatLng southWest = new LatLng(center.latitude - radiusDegrees, center.longitude - radiusDegrees);
        LatLngBounds bounds = LatLngBounds.builder()
                .include(northEast)
                .include(southWest)
                .build();

        return bounds;
    }

    public Location getMyLocation2(GoogleApiClient mGoogleApiClient) {

        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    public Place getPlaceFromLatLang(final LatLng ltlang) {
        if (!AppService.isNetworkAvailable(mContext)) {
            return null;
        }
        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        int trycount = 1;
        int maxtry = 5;

        while (addresses == null && trycount <= maxtry) {
            try {
                addresses = geocoder.getFromLocation(ltlang.latitude, ltlang.longitude, 1);
                if (addresses != null) {
                    break;
                }
                trycount = trycount + 1;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                trycount = trycount + 1;
            }
        }// Here 1 represent max location result to returned, by documents it recommended 1 to 5
        if (addresses == null || addresses.size() == 0) {
            return null;
        }

        String adr = "";
        Address adrs = addresses.get(0);


        int index = adrs.getMaxAddressLineIndex();
        if (index != -1) {
            for (int i = 0; i <= index; i++) {
                adr += adrs.getAddressLine(i) + " ";

            }
        }

        String nm = adrs.getPremises();
        if (!(nm != null && nm != "")) {
            //nm = "Lat " + ltlang.latitude + ", Long " + ltlang.longitude;
            nm = adr;
        }

        final String name = nm;

        final String address = adr;

        Place place = new Place() {

            @Override
            public boolean isDataValid() {
                // TODO Auto-generated method stub
                return true;
            }

            @Override
            public Place freeze() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Uri getWebsiteUri() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public CharSequence getName() {
                // TODO Auto-generated method stub
                return name;
            }

            @Override
            public LatLng getLatLng() {
                // TODO Auto-generated method stub
                return ltlang;
            }

            @Override
            public String getId() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public CharSequence getAddress() {
                // TODO Auto-generated method stub
                return address;
            }

            @Override
            public Locale getLocale() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public CharSequence getPhoneNumber() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<Integer> getPlaceTypes() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getPriceLevel() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public float getRating() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public LatLngBounds getViewport() {
                // TODO Auto-generated method stub
                return null;
            }

            public CharSequence getAttributions() {
                // TODO Auto-generated method stub
                return null;
            }
        };
        return place;
    }




}
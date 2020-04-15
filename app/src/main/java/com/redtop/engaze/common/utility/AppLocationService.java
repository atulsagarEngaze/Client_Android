package com.redtop.engaze.common.utility;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Parcel;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlusCode;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.redtop.engaze.adapter.SuggestedLocationAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.domain.EventPlace;

import androidx.annotation.Nullable;

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

    public void displayPlace(EventPlace place, TextView mEventLocation) {
        mEventLocation.setText(place.getName());
    }

    public RectangularBounds getLatLongBounds(Location location) {
        double radiusDegrees = .25;
        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng northEast = new LatLng(center.latitude + radiusDegrees, center.longitude + radiusDegrees);
        LatLng southWest = new LatLng(center.latitude - radiusDegrees, center.longitude - radiusDegrees);
        RectangularBounds bounds = RectangularBounds.newInstance(southWest, northEast);
        return bounds;
    }

    public Location getMyLocation2(GoogleApiClient mGoogleApiClient) {

        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    public Place getPlaceFromLatLang(final LatLng ltlang) {
        if (!AppContext.context.isInternetEnabled) {
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
            @Nullable
            @Override
            public String getAddress() {
                return address;
            }

            @Nullable
            @Override
            public AddressComponents getAddressComponents() {
                return null;
            }

            @Nullable
            @Override
            public List<String> getAttributions() {
                return null;
            }

            @Nullable
            @Override
            public String getId() {
                return null;
            }

            @Nullable
            @Override
            public LatLng getLatLng() {
                return ltlang;
            }

            @Nullable
            @Override
            public String getName() {
                return name;
            }

            @Nullable
            @Override
            public OpeningHours getOpeningHours() {
                return null;
            }

            @Nullable
            @Override
            public String getPhoneNumber() {
                return null;
            }

            @Nullable
            @Override
            public List<PhotoMetadata> getPhotoMetadatas() {
                return null;
            }

            @Nullable
            @Override
            public PlusCode getPlusCode() {
                return null;
            }

            @Nullable
            @Override
            public Integer getPriceLevel() {
                return null;
            }

            @Nullable
            @Override
            public Double getRating() {
                return null;
            }

            @Nullable
            @Override
            public List<Type> getTypes() {
                return null;
            }

            @Nullable
            @Override
            public Integer getUserRatingsTotal() {
                return null;
            }

            @Nullable
            @Override
            public Integer getUtcOffsetMinutes() {
                return null;
            }

            @Nullable
            @Override
            public LatLngBounds getViewport() {
                return null;
            }

            @Nullable
            @Override
            public Uri getWebsiteUri() {
                return null;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {

            }
        };

        return place;
    }




}
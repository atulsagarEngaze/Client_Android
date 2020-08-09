package com.redtop.engaze.common.utility;

import java.util.HashMap;

import org.w3c.dom.Document;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.redtop.engaze.BaseActivity;
import com.redtop.engaze.EventParticipantListWithNoCallSMS;
import com.redtop.engaze.R;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.UsersLocationDetail;

public class InfoWindowHelper {

    private static String distance = "";
    private static String duration = "";

    public static void createAndshowInfoWindow(final Context context, Marker marker, GoogleMap map,
                                               final Event ed, final LatLng destinationLatLng,
                                               GoogleDirection gd, final HashMap<Marker, UsersLocationDetail> markerUserLocation, final boolean enableclick) {


        final Marker currentMarker = marker;
        final UsersLocationDetail ud = markerUserLocation.get(marker);
        if (enableclick) {
            map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    showEndPointSelectionActivity(context, currentMarker, markerUserLocation);
                }
            });
        }

        map.setInfoWindowAdapter(new InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                String address = "";
                String userName = "";
                if (ud != null) {//destination address
                    address = ud.address;
                    userName = ud.userName;
                } else {
                    address = ed.destination.getAddress();
                    userName = ed.destination.getName();
                }

                View markerInfoWindow = ((BaseActivity) context).getLayoutInflater().inflate(R.layout.custom_snippet, null);

                DisplayMetrics dm = new DisplayMetrics();
                ((BaseActivity) context).getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
                int width = dm.widthPixels;

                int customWidth = (int) (width / 4) * 3;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(customWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
                markerInfoWindow.setLayoutParams(params);
                TextView userName_tv = (TextView) markerInfoWindow.findViewById(R.id.user_name);
                userName_tv.setText(userName);
                TextView info = (TextView) markerInfoWindow.findViewById(R.id.info);
                info.setText(address);
                //there should be destination and marker clicked should not be destination itself
                final RelativeLayout mEtDistanceRL = (RelativeLayout) markerInfoWindow.findViewById(R.id.lleta_distance);
                final View mDividerV = (View) markerInfoWindow.findViewById(R.id.info_window_divider);
                final TextView mloadRouteTV = (TextView) markerInfoWindow.findViewById(R.id.loadRoute);
                if (enableclick) {
                    mloadRouteTV.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            showEndPointSelectionActivity(context, currentMarker, markerUserLocation);
                        }
                    });
                } else {
                    mloadRouteTV.setVisibility(View.GONE);
                }

                if (destinationLatLng != null && ud != null) {

                    mEtDistanceRL.setVisibility(View.VISIBLE);
                    mDividerV.setVisibility(View.VISIBLE);
                    TextView txtDistance = (TextView) markerInfoWindow.findViewById(R.id.distance_left);
                    txtDistance.setText("Distance:" + distance);

                    TextView eta = (TextView) markerInfoWindow.findViewById(R.id.eta);
                    eta.setText("ETA " + duration.replace("hour", "hr"));

                } else {
                    mEtDistanceRL.setVisibility(View.GONE);
                    mDividerV.setVisibility(View.GONE);
                }
                return markerInfoWindow;
            }
        });

        gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {

            @Override
            public void onResponse(String status, Document doc,
                                   GoogleDirection gd) {
                //				gd.animateDirection(mMap, gd.getDirection(doc), GoogleDirection.SPEED_FAST
                //						, true, true, true, false, null, false, true, new PolylineOptions().width(5).color(mContext.getResources().getColor(R.color.primaryDark)));

                try {
                    distance = gd.getTotalDistanceText(doc);
                    duration = gd.getTotalDurationText(doc);
                } catch (Exception ex) {
                    distance = "No route";
                    duration = "";
                }
                if (currentMarker != null) {
                    currentMarker.showInfoWindow();
                }

            }
        });

        if (destinationLatLng != null && ud != null) {
            try {
                gd.request(marker.getPosition(), destinationLatLng, GoogleDirection.MODE_DRIVING);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            currentMarker.showInfoWindow();
        }
        //loadRoute(marker.getPosition(), mDestinationlatlang);
    }

    private static void showEndPointSelectionActivity(Context context, Marker marker,
                                                      HashMap<Marker, UsersLocationDetail> markerUserLocation) {
        HashMap<String, LatLng> endPoints = new HashMap<String, LatLng>();
        for (Marker m : markerUserLocation.keySet()) {
            if (!m.equals(marker)) {
                UsersLocationDetail udlocal = markerUserLocation.get(m);
                if (udlocal != null) {
                    endPoints.put(udlocal.userName, m.getPosition());
                } else {
                    endPoints.put("Destination", m.getPosition());
                }
            }
        }

        Intent intent = new Intent(context, EventParticipantListWithNoCallSMS.class);
        intent.putExtra("action", "loadroute");
        intent.putExtra("endpoints", endPoints);
        ((BaseActivity) context).startActivityForResult(intent, Constants.ROUTE_END_POINT_REQUEST_CODE);
    }
}

package com.redtop.engaze.webservice.proxy;

import android.location.Location;
import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.webservice.BaseWebService;
import com.redtop.engaze.webservice.ILocationWS;
import com.redtop.engaze.webservice.Routes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

public class LocationWSProxy extends BaseWebService implements ILocationWS {

   public static Location location;


    public void updateLocation(JSONObject jsonObject,
                               final OnAPICallCompleteListner listnerOnSuccess,
                               final OnAPICallCompleteListner listnerOnFailure) {
        try {
            JSONArray jLocation = new JSONArray();
            UsersLocationDetail ud1 = new UsersLocationDetail("3105beb6-3347-4bdf-8905-29b622b51dbd",
                    12.9823286, 77.6931817,"","");
            ud1.currentAddress = "83, Laxmi Sagar Layout, 2, Goshala Rd, Garudachar Palya, Mahadevapura, Bengaluru, Karnataka 560048";
            ud1.eta = "";

            jLocation.put(new JSONObject( AppContext.jsonParser.Serialize(ud1)));

            UsersLocationDetail ud2 = new UsersLocationDetail("94973d2a-614e-4b2c-8654-7e6b13cdc44e",
                    location.getLatitude(), location.getLongitude(),"","");
            ud2.currentAddress = "#1127, 8th B Main, BTM 1st Stage, Bangalore 560029";
            ud2.eta = "";

            jLocation.put(new JSONObject( AppContext.jsonParser.Serialize(ud2)));


            JSONObject response = new JSONObject();
            response.put("ListOfUserLocation", jLocation);

            listnerOnSuccess.apiCallComplete(response);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }

    public void getLocationsFromServer(String userId, String eventId,
                                       final OnAPICallCompleteListner listnerOnSuccess,
                                       final OnAPICallCompleteListner listnerOnFailure) {
        try {
            JSONArray jLocation = new JSONArray();
            UsersLocationDetail ud1 = new UsersLocationDetail("3105beb6-3347-4bdf-8905-29b622b51dbd",
                    12.9823286, 77.6931817,"","");
            ud1.currentAddress = "83, Laxmi Sagar Layout, 2, Goshala Rd, Garudachar Palya, Mahadevapura, Bengaluru, Karnataka 560048";
            ud1.eta = "";
            Date currentTime = Calendar.getInstance().getTime();
            ud1.createdOn = DateUtil.ConvertDateToString(currentTime);

            jLocation.put(new JSONObject( AppContext.jsonParser.Serialize(ud1)));

            UsersLocationDetail ud2 = new UsersLocationDetail("94973d2a-614e-4b2c-8654-7e6b13cdc44e",
                    location.getLatitude(), location.getLongitude(),"","");
            ud2.currentAddress = "#1127, 8th B Main, BTM 1st Stage, Bangalore 560029";
            ud2.eta = "";

            ud2.createdOn = DateUtil.ConvertDateToString(currentTime);

            jLocation.put(new JSONObject( AppContext.jsonParser.Serialize(ud2)));


            JSONObject response = new JSONObject();
            response.put("ListOfUserLocation", jLocation);

            listnerOnSuccess.apiCallComplete(response);

        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
            ex.printStackTrace();
            listnerOnFailure.apiCallComplete(null);
        }
    }
}

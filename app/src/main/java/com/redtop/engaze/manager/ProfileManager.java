package com.redtop.engaze.manager;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.webservice.ProfileWS;


public class ProfileManager {
    private final static String TAG = ProfileManager.class.getName();

    public static void saveProfile(final Context context, final JSONObject jRequestobj,
                                   final OnAPICallCompleteListner listnerOnSuccess,
                                   final OnActionFailedListner listnerOnFailure) {

        if (!AppContext.context.isInternetEnabled) {
            String message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.SAVEPROFILE);
            return;

        }

        ProfileWS.saveProfile(context, jRequestobj, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                Log.d(TAG, "EventResponse:" + response.toString());

                try {
                    String Status = (String) response.getString("Status");

                    if (Status == "true") {
                        String loginID = (String) response.getString("Id");
                        // save the loginid to preferences
                        PreffManager.setPref(Constants.LOGIN_ID, loginID);
                        PreffManager.setPref(Constants.LOGIN_NAME, jRequestobj.getString("ProfileName"));
                        listnerOnSuccess.apiCallComplete(response);
                    } else {

                        listnerOnFailure.actionFailed(null, Action.SAVEPROFILE);
                    }

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.SAVEPROFILE);
                }

            }
        }, new OnAPICallCompleteListner() {

            @Override
            public void apiCallComplete(JSONObject response) {
                //for testing
                //listnerOnFailure.actionFailed(null, Action.SAVEPROFILE);
                PreffManager.setPref(Constants.LOGIN_ID, "94973d2a-614e-4b2c-8654-7e6b13cdc44e");
                PreffManager.setPref(Constants.LOGIN_NAME, "Atul");
                AppContext.context.loginId = "94973d2a-614e-4b2c-8654-7e6b13cdc44e";
                AppContext.context.loginName = "Atul";
                listnerOnSuccess.apiCallComplete(response);
            }
        });
    }

}



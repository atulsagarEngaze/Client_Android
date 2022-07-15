package com.redtop.engaze.domain.manager;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.webservice.IUserWS;
import com.redtop.engaze.webservice.UserWS;


public class ProfileManager {
    private final static String TAG = ProfileManager.class.getName();

    private final static IUserWS userWS = new UserWS();

    public static void saveProfile(final Context context, final JSONObject jRequestobj,
                                   final OnAPICallCompleteListener listnerOnSuccess,
                                   final OnActionFailedListner listnerOnFailure) {

        if (!AppContext.context.isInternetEnabled) {
            String message = context.getResources().getString(R.string.message_general_no_internet_responseFail);
            Log.d(TAG, message);
            listnerOnFailure.actionFailed(message, Action.SAVEPROFILE);
            return;

        }

        userWS.saveProfile(jRequestobj, new OnAPICallCompleteListener<String>() {

            @Override
            public void apiCallSuccess(String response) {
                Log.d(TAG, "EventResponse:" + response.toString());

                try {
                    String loginID = response.replace("\"","");
                    AppContext.context.loginId = loginID;
                    AppContext.context.loginName =jRequestobj.getString("ProfileName");
                    PreffManager.setPref(Constants.LOGIN_ID, loginID);
                    PreffManager.setPref(Constants.LOGIN_NAME, jRequestobj.getString("ProfileName"));
                    listnerOnSuccess.apiCallSuccess(loginID);


                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.actionFailed(null, Action.SAVEPROFILE);
                }
            }

            @Override
            public void apiCallFailure() {
                listnerOnFailure.actionFailed(null, Action.SAVEPROFILE);
            }
        });
    }
}
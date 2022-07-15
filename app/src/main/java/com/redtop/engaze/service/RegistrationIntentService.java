package com.redtop.engaze.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.manager.ProfileManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = IntentService.class.getName();

    private Context mContext;

    public RegistrationIntentService() {
        super(TAG);
        Log.i(TAG, "Constructor RegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext =  this;
        try {

            Log.i(TAG, "START get_token : ");
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
                String token = instanceIdResult.getToken();
                Log.i(TAG, "FCM Registration Token: " + token);
                PreffManager.setPref(Constants.GCM_REGISTRATION_TOKEN, token);

                try {
                    SaveProfile(new JSONObject(intent.getStringExtra("profileObject")),token);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Profile Object is not in correct format", e);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Veranstaltung.REGISTRATION_FAILED));
                }
                catch (Exception ex){
                    Log.d(TAG, "Failed to complete the registration", ex);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Veranstaltung.REGISTRATION_FAILED));
                }
            });

        } catch (Exception e) {
            Log.d(TAG, "Failed to complete the registration", e);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Veranstaltung.REGISTRATION_FAILED));
        }
    }

    private void SaveProfile(JSONObject jasonProfileObject, String fcmToken) throws JSONException {

            jasonProfileObject.put("GCMClientId", fcmToken);
            ProfileManager.saveProfile(mContext, jasonProfileObject, new OnAPICallCompleteListener<String>() {

                @Override
                public void apiCallSuccess(String response) {
                    // Notify UI that registration has completed, so the progress indicator can be hidden.
                    Intent registrationComplete = new Intent(Veranstaltung.REGISTRATION_COMPLETE);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(registrationComplete);

                }

                @Override
                public void apiCallFailure() {
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Veranstaltung.REGISTRATION_FAILED));
                }
            }, AppContext.actionHandler);


    }
}

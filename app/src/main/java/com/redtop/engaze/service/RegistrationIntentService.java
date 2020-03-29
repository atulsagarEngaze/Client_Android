package com.redtop.engaze.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.redtop.engaze.R;
import com.redtop.engaze.common.Constants;
import com.redtop.engaze.common.PreffManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = IntentService.class.getName();
    private static final String[] TOPICS = {"global"};
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";

    public RegistrationIntentService() {
        super(TAG);
        Log.i(TAG, "Constructore RegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
        	Log.d(TAG, "START get_token : before synchronized");
            synchronized (TAG) {
                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
            	Log.i(TAG, "START get_token : ");
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
						GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                // [END get_token]
                Log.i(TAG, "GCM Registration Token: " + token);
                
                PreffManager.setPref(Constants.GCM_REGISTRATION_TOKEN, token, this);
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Constants.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}

package com.redtop.engaze.receiver;

import com.redtop.engaze.common.constant.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NetworkUpdateReceiver extends BroadcastReceiver{

	public static final String TAG = NetworkUpdateReceiver.class.getName();
    // Called when a broadcast is made targeting this class
    @Override
    public void onReceive(Context context, Intent intent) {
		Intent networkStatusUpdate = new Intent(Constants.NETWORK_STATUS_UPDATE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(networkStatusUpdate);
    }    
}
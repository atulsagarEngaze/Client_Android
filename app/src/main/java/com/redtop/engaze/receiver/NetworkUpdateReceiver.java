package com.redtop.engaze.receiver;

import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.service.MyCurrentLocationListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
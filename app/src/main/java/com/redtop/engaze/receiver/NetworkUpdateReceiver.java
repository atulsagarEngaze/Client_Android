package com.redtop.engaze.receiver;

import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.service.UploadLocationToServerService;

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
    	Boolean isNetAvail = AppUtility.isNetworkAvailable(context);
		PreffManager.setPref("NetworkStaus", isNetAvail.toString());
		Intent networkStatusUpdate = new Intent(Constants.NETWORK_STATUS_UPDATE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(networkStatusUpdate);
        Boolean internetStatus = AppUtility.isNetworkAvailable(context);
		
		if(internetStatus){
			Log.v(TAG, "Performing start/stop operation of Location service as network is back");
			UploadLocationToServerService.performSartStop();
		}
		else{
			Log.v(TAG, "Stopping Location service as network is not available back");
			UploadLocationToServerService.performStop();
		}

    }    
}
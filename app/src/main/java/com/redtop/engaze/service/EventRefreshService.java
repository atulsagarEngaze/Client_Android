package com.redtop.engaze.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*import com.redtop.engaze.entity.EventDetail;
import com.redtop.engaze.interfaces.OnRefreshEventListCompleteListner;
import com.redtop.engaze.utils.Constants;
import com.redtop.engaze.utils.EventManager;*/

public class EventRefreshService extends IntentService {

    private static final String TAG = "EventRefreshService";
    public EventRefreshService() {
        super(TAG);
        Log.i(TAG, "Constructor EventRefreshService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	final Context context = this;
    	/*EventManager.refreshEventList(context, new OnRefreshEventListCompleteListner() {

			@Override
			public void RefreshEventListComplete(List<EventDetail> eventDetailList) {				
				Intent intent = new Intent(Constants.EVENTS_REFRESHED);							
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
			}
		},null);*/
    }
}

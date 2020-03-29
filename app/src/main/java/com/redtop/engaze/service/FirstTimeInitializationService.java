package com.redtop.engaze.service;

import java.util.Hashtable;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FirstTimeInitializationService extends IntentService {

    private static final String TAG = FirstTimeInitializationService.class.getName();;
    private Context mContext;
    public FirstTimeInitializationService() {
        super(TAG);
        Log.d(TAG, "Constructor");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	mContext = this;
    	/*EventHelper.setLocationServiceCheckAlarm(mContext);
    	initializeContactList();*/
    }
    
    /*private void initializeContactList(){
		try {
			
			InternalCaching.initializeCache(mContext);
			AppUtility.setPrefBoolean(Constants.IS_REGISTERED_CONTACT_LIST_INITIALIZED, false, mContext);			
			
			ContactAndGroupListManager.cacheContactAndGroupList(this, new OnRefreshMemberListCompleteListner() {

				@Override
				public void RefreshMemberListComplete(Hashtable<String, ContactOrGroup> memberList) {				
					AppUtility.setPrefBoolean(Constants.IS_REGISTERED_CONTACT_LIST_INITIALIZED, true, mContext);					

				}
			}, new OnRefreshMemberListCompleteListner() {

				@Override
				public void RefreshMemberListComplete(Hashtable<String, ContactOrGroup> memberList) {				

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
}

package com.redtop.engaze.service;

import java.util.HashMap;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.redtop.engaze.Interface.OnRefreshMemberListCompleteListner;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;

public class FirstTimeInitializationService extends IntentService {

    private static final String TAG = FirstTimeInitializationService.class.getName();

    private Context mContext;

    public FirstTimeInitializationService() {
        super(TAG);
        Log.d(TAG, "Constructor");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = this;
        AppContext.context.setDefaultSetting();
        InternalCaching.initializeCache();
        initializeContactList();
    }

    private void initializeContactList() {
        if(!ContactListRefreshIntentService.IsContactListRefreshServiceRunning) {
            Intent serviceIntent = new Intent(mContext, ContactListRefreshIntentService.class);
            serviceIntent.putExtra(Constants.REFRESH_ONLY_REGISTERED_CONTACTS, false);
            startService(serviceIntent);
        }
    }
}
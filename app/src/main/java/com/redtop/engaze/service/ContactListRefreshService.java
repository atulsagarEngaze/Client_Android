package com.redtop.engaze.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.manager.ContactAndGroupListManager;

import java.util.ArrayList;

public class ContactListRefreshService extends IntentService {

    private static final String TAG = ContactListRefreshService.class.getName();

    private Context mContext;
    public static boolean IsContactListRefreshServiceRunning = false;

    public ContactListRefreshService() {
        super(TAG);
        Log.d(TAG, "Constructor");
    }

    public static void start(Context context, Boolean refreshOnlyRegisteredContacts) {
        if (!IsContactListRefreshServiceRunning) {
            Intent serviceIntent = new Intent(context, ContactListRefreshService.class);
            serviceIntent.putExtra(Constants.REFRESH_ONLY_REGISTERED_CONTACTS, refreshOnlyRegisteredContacts);
            context.startService(serviceIntent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IsContactListRefreshServiceRunning = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = this;
        refreshContactList(intent.getBooleanExtra(Constants.REFRESH_ONLY_REGISTERED_CONTACTS, false));
    }

    private void refreshContactList(boolean refreshOnlyRegisteredContacts) {
        ArrayList<ContactOrGroup> contacts;
        if (refreshOnlyRegisteredContacts) {
            contacts = InternalCaching.getContactListFromCache();
            refreshRegisteredContactList(contacts);
            return;
        }

        try {
            contacts = ContactAndGroupListManager.getAllContactsFromDeviceContactList();
            if(contacts==null ||contacts.size()==0){
                PreffManager.setPref(Constants.LAST_CONTACT_LIST_REFRESH_STATUS, Constants.FAILED);
                broadcastContactListRefreshedProcessComplete();
                return;
            }

            InternalCaching.saveContactListToCache(contacts);
            PreffManager.setPref(Constants.LAST_CONTACT_LIST_REFRESH_STATUS, Constants.SUCCESS);
            refreshRegisteredContactList(contacts);

        } catch (Exception ex) {
            PreffManager.setPref(Constants.LAST_CONTACT_LIST_REFRESH_STATUS, Constants.FAILED);
            Log.d(TAG, ex.toString());
            broadcastContactListRefreshedProcessComplete();
            return;
        }
    }

    private void refreshRegisteredContactList(ArrayList<ContactOrGroup> contacts) {
        try {

            if (contacts != null && contacts.size() > 0) {
                ContactAndGroupListManager.cacheRegisteredContacts(contacts,
                        memberList ->
                        {
                            AppContext.context.sortedContacts =  ContactAndGroupListManager.getSortedContacts();//this is to initialize sorted contact list
                            PreffManager.setPref(Constants.LAST_REGISTERED_CONTACT_LIST_REFRESH_STATUS, Constants.SUCCESS);
                            broadcastContactListRefreshedProcessComplete();
                        },
                        memberList ->
                        {
                            PreffManager.setPref(Constants.LAST_REGISTERED_CONTACT_LIST_REFRESH_STATUS, Constants.FAILED);
                            broadcastContactListRefreshedProcessComplete();
                        });
            }
        } catch (Exception ex) {
            PreffManager.setPref(Constants.LAST_REGISTERED_CONTACT_LIST_REFRESH_STATUS, Constants.FAILED);
            Log.d(TAG, ex.toString());
            broadcastContactListRefreshedProcessComplete();
        }
    }

    private void broadcastContactListRefreshedProcessComplete() {
        Intent contactListRefreshedIntent = new Intent(Veranstaltung.CONTACT_LIST_REFRESH_PROCESS_COMPLETE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(contactListRefreshedIntent);

    }

    @Override
    public void onDestroy() {
        IsContactListRefreshServiceRunning = false;
        super.onDestroy();
    }
}

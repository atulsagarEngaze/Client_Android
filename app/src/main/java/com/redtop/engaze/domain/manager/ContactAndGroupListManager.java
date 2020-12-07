package com.redtop.engaze.domain.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.widget.Toast;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.Interface.OnRefreshMemberListCompleteListner;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.utility.BitMapHelper;
import com.redtop.engaze.common.utility.MaterialColor;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.service.ParticipantService;
import com.redtop.engaze.webservice.IUserWS;
import com.redtop.engaze.webservice.UserWS;

public class ContactAndGroupListManager {

    private final static String TAG = ContactAndGroupListManager.class.getName();

    private final static IUserWS userWS = new UserWS();

    public static void cacheContactAndGroupList(final OnRefreshMemberListCompleteListner listnerOnSuccess, final OnRefreshMemberListCompleteListner listnerOnFailure) {

        PreffManager.setPrefBoolean(Constants.IS_CONTACT_LIST_INITIALIZED, false);
        HashMap<String, ContactOrGroup> contacts = getAllContactsFromDeviceContactList();
        if (contacts != null && contacts.size() > 0) {
            InternalCaching.saveContactListToCache(contacts);
            PreffManager.setPrefBoolean(Constants.IS_CONTACT_LIST_INITIALIZED, true);
            cacheRegisteredContacts(contacts, listnerOnSuccess, listnerOnFailure);
        }
    }

    public static ContactOrGroup getContact(String userId) {
        ContactOrGroup cg = null;
        if(userId==null){
            return cg;
        }
        Hashtable<String, ContactOrGroup> table = InternalCaching.getRegisteredContactListFromCache();
        if (table != null) {
            cg = table.get(userId);
        }

        return cg;
    }

    public static void cacheGroupList() {
        PreffManager.setPrefArrayList("Groups", getAllGroups());
    }

    public static ArrayList<ContactOrGroup> sortContacts(ArrayList<ContactOrGroup> contactsAndGroups) {
        if (contactsAndGroups.size() > 0) {
            Collections.sort(contactsAndGroups, (lhs, rhs) -> lhs.getName().compareToIgnoreCase(rhs.getName()));
        }
        return contactsAndGroups;
    }

    public static ArrayList<ContactOrGroup> getAllRegisteredContacts() {
        ArrayList<ContactOrGroup> contactsAndGroups = new ArrayList<ContactOrGroup>(InternalCaching.getRegisteredContactListFromCache().values());
        AppContext.context.setRegisteredContactList(sortContacts(contactsAndGroups));
        return AppContext.context.sortedRegisteredContacts;
    }

    public static ArrayList<ContactOrGroup> getAllContacts() {
        ArrayList<ContactOrGroup> contactsAndGroups = new ArrayList<ContactOrGroup>(InternalCaching.getContactListFromCache().values());
        ArrayList<ContactOrGroup> registered = new ArrayList<ContactOrGroup>();
        ArrayList<ContactOrGroup> unRegistered = new ArrayList<ContactOrGroup>();
        ArrayList<ContactOrGroup> finalContacts = new ArrayList<ContactOrGroup>();

        for (ContactOrGroup cg : contactsAndGroups) {
            if (cg.getUserId() != null) {
                registered.add(cg);
            } else {
                unRegistered.add(cg);
            }
        }

        finalContacts.addAll(sortContacts(registered));
        finalContacts.addAll(sortContacts(unRegistered));
        AppContext.context.setContactList(finalContacts);
        return finalContacts;
    }

    public static HashMap<String, ContactOrGroup> getAllContactsFromCache() {
        return InternalCaching.getContactListFromCache();
    }

    public static ArrayList<ContactOrGroup> getGroups() {
        return PreffManager.getPrefArrayList("Groups");
    }

    public static void initializedRegisteredUser(final OnRefreshMemberListCompleteListner listnerOnSuccess, final OnRefreshMemberListCompleteListner listnerOnFailure) {
        cacheRegisteredContacts(getAllContactsFromCache(), listnerOnSuccess, listnerOnFailure);
    }

    private static void cacheRegisteredContacts(final HashMap<String, ContactOrGroup> contactsAndgroups,
                                                final OnRefreshMemberListCompleteListner listnerOnSuccess,
                                                final OnRefreshMemberListCompleteListner listnerOnFailure) {
        if (!AppContext.context.isInternetEnabled) {
            String message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            //Toast.makeText(mContext,	message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, message);
            listnerOnFailure.RefreshMemberListComplete(null);
            return;
        }

        userWS.AssignUserIdToRegisteredUser(contactsAndgroups, new OnAPICallCompleteListener<JSONObject>() {
            @Override
            public void apiCallSuccess(JSONObject response) {
                try {
                    Hashtable<String, ContactOrGroup> registeredContacts = prepareRegisteredContactList(response, contactsAndgroups);
                    InternalCaching.saveRegisteredContactListToCache(registeredContacts);
                    InternalCaching.saveContactListToCache(contactsAndgroups);
                    PreffManager.setPrefBoolean(Constants.IS_REGISTERED_CONTACT_LIST_INITIALIZED, true);
                    listnerOnSuccess.RefreshMemberListComplete(registeredContacts);

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.RefreshMemberListComplete(null);
                }
            }

            @Override
            public void apiCallFailure() {
                listnerOnFailure.RefreshMemberListComplete(null);
            }
        });

    }

    private static Hashtable<String, ContactOrGroup> prepareRegisteredContactList(JSONObject response, HashMap<String, ContactOrGroup> contactsAndgroups) throws JSONException, IOException, ClassNotFoundException {

        Hashtable<String, ContactOrGroup> registeredContacts = new Hashtable<String, ContactOrGroup>();
        JSONArray jUsers = response.getJSONArray("listOfRegisteredContact");
        if (jUsers.length() == 0) {
            return registeredContacts;
        }

        for (ContactOrGroup cg : contactsAndgroups.values()) {
            if (cg.getThumbnailUri() == null || cg.getThumbnailUri() == "") {
                cg.setIconImageBitmap(ContactOrGroup.getAppUserIconBitmap());
                String startingchar = cg.getName().substring(0, 1);
                if (!(startingchar.matches("[0-9]") || startingchar.startsWith("+"))) {
                    cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(MaterialColor.getColor(cg.getName()), 40, startingchar.toUpperCase()));
                } else {
                    cg.setImageBitmap(BitMapHelper.generateCircleBitmapForIcon(MaterialColor.getColor(cg.getName()), 40, Uri.parse("android.resource://com.redtop.engaze/drawable/ic_person_white_24dp")));
                }
            } else {
                Bitmap pofilePicBitmap = BitMapHelper.generateCircleBitmapForImage(54, Uri.parse(cg.getThumbnailUri()));
                cg.setImageBitmap(pofilePicBitmap);
                cg.setIconImageBitmap(pofilePicBitmap);
            }
        }

        String userId = "";
        ContactOrGroup cg = null;
        for (int i = 0, size = jUsers.length(); i < size; i++) {
            JSONObject jsonObj = jUsers.getJSONObject(i);

            cg = contactsAndgroups.get(jsonObj.getString("mobileNumberStoredInRequestorPhone"));
            if (cg != null) {
                userId = jsonObj.get("userId").toString();
                cg.setUserId(userId);
                registeredContacts.put(userId, cg);
                contactsAndgroups.put(jsonObj.getString("mobileNumberStoredInRequestorPhone"), cg);
            }
        }
        return registeredContacts;
    }

    private static HashMap<String, ContactOrGroup> getAllContactsFromDeviceContactList() {
        Cursor cursor = null;
        HashMap<String, ContactOrGroup> contacts = new HashMap<String, ContactOrGroup>();
        try {
            ContactOrGroup cg;
            String[] columns = {ContactsContract.Contacts.PHOTO_THUMBNAIL_URI, ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};
            cursor = AppContext.context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, columns, null, null, null);

            int ColumeIndex_THUMBNAIL = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
            int ColumeIndex_ID = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            int ColumeIndex_DISPLAY_NAME = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            int ColumeIndex_HAS_PHONE_NUMBER = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

            //Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " DESC");


            if (cursor.getCount() > 0) {

                while (cursor.moveToNext()) {
                    String thumbnail_uri = cursor.getString(ColumeIndex_THUMBNAIL);
                    String has_phone = cursor.getString(ColumeIndex_HAS_PHONE_NUMBER);

                    if (!has_phone.endsWith("0")) {
                        ArrayList<String> phoneNumbers = getPhoneNumbers(cursor.getString(ColumeIndex_ID));
                        for (String phoneNumber : phoneNumbers) {
                            cg = new ContactOrGroup();
                            cg.setMobileNumber(phoneNumber);
                            cg.setName(cursor.getString(ColumeIndex_DISPLAY_NAME));

                            cg.setThumbnailUri(thumbnail_uri);

                            if (thumbnail_uri == null || thumbnail_uri == "") {
                                cg.setIconImageBitmap(ContactOrGroup.getAppUserIconBitmap());
                                String startingchar = cg.getName().substring(0, 1);
                                if (!(startingchar.matches("[0-9]") || startingchar.startsWith("+"))) {
                                    cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(MaterialColor.getColor(cg.getName()), 40, startingchar.toUpperCase()));
                                } else {
                                    cg.setImageBitmap(BitMapHelper.generateCircleBitmapForIcon(MaterialColor.getColor(cg.getName()), 40, Uri.parse("android.resource://com.redtop.engaze/drawable/ic_person_white_24dp")));
                                }
                            } else {
                                Bitmap pofilePicBitmap = BitMapHelper.generateCircleBitmapForImage(54, Uri.parse(cg.getThumbnailUri()));
                                cg.setImageBitmap(pofilePicBitmap);
                                cg.setIconImageBitmap(pofilePicBitmap);
                            }
                            contacts.put(phoneNumber, cg);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contacts;
    }

    private static ArrayList<String> getPhoneNumbers(String id) {
        ArrayList<String> numbers = new ArrayList<String>();
        //Cursor phones = getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + id, null, null);
        Cursor phones = AppContext.context.getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + id, null, null);
        while (phones.moveToNext()) {

            numbers.add(phones.getString(phones.getColumnIndex(Phone.NUMBER)).replaceAll("\\s", ""));
        }

        phones.close();

        return numbers;
    }

    private static ArrayList<ContactOrGroup> getAllGroups() {

        ArrayList<ContactOrGroup> groups = new ArrayList<ContactOrGroup>();
        new ContactOrGroup("group", 123, null);

        return groups;
    }

    public static void refreshMemberList() {


        Thread thread = new Thread() {
            @Override
            public void run() {
                cacheContactAndGroupList(memberList -> {
                    PreffManager.setPrefBoolean(Constants.IS_REGISTERED_CONTACT_LIST_INITIALIZED, true);
                    AppContext.context.isContactListUpdated = false;
                    AppContext.context.isRegisteredContactListUpdated=false;


                }, memberList -> Toast.makeText(AppContext.context.currentActivity, AppContext.context.getResources().getString(R.string.message_contacts_errorRetrieveData), Toast.LENGTH_SHORT).show());
            }
        };
        thread.start();

    }
}
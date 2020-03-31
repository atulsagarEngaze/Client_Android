package com.redtop.engaze.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.Interface.OnRefreshMemberListCompleteListner;
import com.redtop.engaze.R;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.BitMapHelper;
import com.redtop.engaze.common.utility.MaterialColor;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.webservice.ContactsWS;

public class ContactAndGroupListManager {
	private static Context mContext;
	private final static String TAG = ContactAndGroupListManager.class.getName();

	public static void cacheContactAndGroupList(Context context, final OnRefreshMemberListCompleteListner listnerOnSuccess, final OnRefreshMemberListCompleteListner listnerOnFailure)
	{
		mContext = context;
		PreffManager.setPrefBoolean(Constants.IS_CONTACT_LIST_INITIALIZED, false, mContext);
		HashMap<String, ContactOrGroup> contacts = getAllContactsFromDeviceContactList();
		if(contacts !=null && contacts.size()>0){
			InternalCaching.saveContactListToCache(contacts, mContext);
			PreffManager.setPrefBoolean(Constants.IS_CONTACT_LIST_INITIALIZED, true, mContext);
			cacheRegisteredContacts(contacts,listnerOnSuccess, listnerOnFailure);
		}
	}

	public static ContactOrGroup getContact(Context context, String userId)
	{	
		ContactOrGroup cg = null;
		Hashtable<String, ContactOrGroup>table =  InternalCaching.getRegisteredContactListFromCache(context);
		if(table!=null)
		{
			cg = table.get(userId);
		}

		return cg;
	}

	public static void cacheGroupList(Context context)
	{				
		PreffManager.setPrefArrayList("Groups",getAllGroups(), context);
	}	

	public static void assignContactsToEventMembers(ArrayList<EventParticipant> eventMembers, Context context){
		Hashtable<String, ContactOrGroup> registeredList = InternalCaching.getRegisteredContactListFromCache(context);
		ContactOrGroup cg;
		for(EventParticipant mem : eventMembers){
			cg = mem.getContact();
			if(cg==null){
				cg = registeredList.get(mem);
				if(cg==null){
					cg = new ContactOrGroup();
					cg.setIconImageBitmap(ContactOrGroup.getAppUserIconBitmap(context));
					if(EventParticipant.isParticipantCurrentUser(mem.getUserId())|| mem.getProfileName().startsWith("~") ){
						cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(context, MaterialColor.getColor(mem.getProfileName()), 40,mem.getProfileName().substring(1, 2).toUpperCase() ));
					}
					else
					{
						cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(context, MaterialColor.getColor(mem.getProfileName()), 40,mem.getProfileName().substring(0, 1).toUpperCase() ));
					}
				}
				mem.setContact(cg);
			}
		}
	}

	public static ArrayList<ContactOrGroup> sortContacts(ArrayList<ContactOrGroup> contactsAndGroups){
		if(contactsAndGroups.size()>0){
			Collections.sort(contactsAndGroups, new Comparator<ContactOrGroup>()
					{

				@Override
				public int compare(ContactOrGroup lhs, ContactOrGroup rhs) {

					return lhs.getName().compareToIgnoreCase(rhs.getName());
				}

					});
		}
		return contactsAndGroups;	
	}

	public static ArrayList<ContactOrGroup>getAllRegisteredContacts(Context context)
	{
		ArrayList<ContactOrGroup>contactsAndGroups = new ArrayList<ContactOrGroup>( InternalCaching.getRegisteredContactListFromCache(context).values());

		return sortContacts(contactsAndGroups);		 
	}

	public static ArrayList<ContactOrGroup>getAllContacts(Context context)
	{
		ArrayList<ContactOrGroup>contactsAndGroups = new ArrayList<ContactOrGroup>( InternalCaching.getContactListFromCache(context).values());
		ArrayList<ContactOrGroup>registered = new ArrayList<ContactOrGroup>();
		ArrayList<ContactOrGroup>unRegistered = new ArrayList<ContactOrGroup>();
		ArrayList<ContactOrGroup>finalContacts = new ArrayList<ContactOrGroup>();

		for(ContactOrGroup cg : contactsAndGroups){
			if(cg.getUserId() != null){
				registered.add(cg);
			}
			else{
				unRegistered.add(cg);
			}
		}

		finalContacts.addAll(sortContacts(registered));
		finalContacts.addAll(sortContacts(unRegistered));
		return finalContacts;		 
	}

	public static HashMap<String, ContactOrGroup> getAllContactsFromCache(Context context)
	{
		return InternalCaching.getContactListFromCache(context);
	}

	public static ArrayList<ContactOrGroup> getGroups(Context context)
	{
		return PreffManager.getPrefArrayList("Groups", context);
	}

	public static void initializedRegisteredUser(Context context, final OnRefreshMemberListCompleteListner listnerOnSuccess, final OnRefreshMemberListCompleteListner listnerOnFailure)
	{
		cacheRegisteredContacts(getAllContactsFromCache(context), listnerOnSuccess, listnerOnFailure);
	}

	private static void cacheRegisteredContacts(final HashMap<String, ContactOrGroup> contactsAndgroups,
			final OnRefreshMemberListCompleteListner listnerOnSuccess, 
			final OnRefreshMemberListCompleteListner listnerOnFailure){
		if(!AppUtility.isNetworkAvailable(mContext))
		{
			String message = mContext.getResources().getString(R.string.message_general_no_internet_responseFail);		
			//Toast.makeText(mContext,	message, Toast.LENGTH_SHORT).show();
			Log.d(TAG, message);
			listnerOnFailure.RefreshMemberListComplete(null);
			return ;
		}

		ContactsWS.AssignUserIdToRegisteredUser(mContext, contactsAndgroups, new OnAPICallCompleteListner() {
			@Override
			public void apiCallComplete(JSONObject response) {
				try{
					String Status = (String)response.getString("Status");
					if (Status == "true")
					{
						Hashtable<String, ContactOrGroup> registeredContacts  = prepareRegisteredContactList(response, contactsAndgroups);
						InternalCaching.saveRegisteredContactListToCache(registeredContacts, mContext);
						InternalCaching.saveContactListToCache(contactsAndgroups, mContext);
						PreffManager.setPrefBoolean(Constants.IS_REGISTERED_CONTACT_LIST_INITIALIZED, true, mContext);
						listnerOnSuccess.RefreshMemberListComplete(registeredContacts);						
					}
					else
					{
						String error = (String)response.getString("ErrorMessage");
						Log.d(TAG, error);
						listnerOnFailure.RefreshMemberListComplete(null);
					}

				}
				catch(Exception ex){
					Log.d(TAG, ex.toString());
					ex.printStackTrace();			
					listnerOnFailure.RefreshMemberListComplete(null);
				}

			}
		}, new OnAPICallCompleteListner() {

			@Override
			public void apiCallComplete(JSONObject response) {
				listnerOnFailure.RefreshMemberListComplete(null);

			}
		});
	}

	private static Hashtable<String, ContactOrGroup> prepareRegisteredContactList(JSONObject response, HashMap<String, ContactOrGroup> contactsAndgroups) throws JSONException, IOException, ClassNotFoundException{

		Hashtable<String, ContactOrGroup> registeredContacts = new Hashtable<String, ContactOrGroup>();
		JSONArray jUsers = (JSONArray)response.getJSONArray("ListOfRegisteredContacts");		
		if(jUsers.length()==0){
			return registeredContacts;
		}
		String userId ="";
		ContactOrGroup cg = null;
		for (int i = 0, size = jUsers.length(); i < size; i++)
		{
			JSONObject jsonObj = jUsers.getJSONObject(i);
			cg = contactsAndgroups.get(jsonObj.getString("MobileNumberStoredInRequestorPhone"));
			if(cg!=null){
				userId = jsonObj.get("UserId").toString();
				cg.setUserId(userId);
				if(cg.getThumbnailUri()==null)
				{
					cg.setIconImageBitmap(ContactOrGroup.getAppUserIconBitmap(mContext));								
					String startingchar = cg.getName().substring(0, 1);
					if(!(startingchar.matches("[0-9]") ||startingchar.startsWith("+")) ){
						cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(mContext, MaterialColor.getColor(cg.getName()), 40,startingchar.toUpperCase() ));
					}
					else
					{
						cg.setImageBitmap(BitMapHelper.generateCircleBitmapForIcon(mContext, MaterialColor.getColor(cg.getName()), 40, Uri.parse("android.resource://com.redtop.engaze/drawable/ic_person_white_24dp")));
					}
				}
				else
				{
					Bitmap pofilePicBitmap = BitMapHelper.generateCircleBitmapForImage(mContext, 54, Uri.parse(cg.getThumbnailUri()));
					cg.setImageBitmap(pofilePicBitmap);	
					cg.setIconImageBitmap(pofilePicBitmap);

				}

				registeredContacts.put(userId, cg);
				contactsAndgroups.put(jsonObj.getString("MobileNumberStoredInRequestorPhone"), cg);				
			}			
		}
		return registeredContacts;
	}

	private static HashMap<String, ContactOrGroup>getAllContactsFromDeviceContactList(){
		Cursor cursor = null;
		HashMap<String, ContactOrGroup> contacts = new HashMap<String, ContactOrGroup> ();
		try {
			ContactOrGroup cg;			
			String[] columns = {ContactsContract.Contacts.PHOTO_THUMBNAIL_URI, ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};
			cursor = mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, columns, null, null, null);

			int ColumeIndex_THUMBNAIL =  cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
			int ColumeIndex_ID = cursor.getColumnIndex(ContactsContract.Contacts._ID);
			int ColumeIndex_DISPLAY_NAME = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
			int ColumeIndex_HAS_PHONE_NUMBER = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

			//Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " DESC");


			if (cursor.getCount() > 0) {

				while(cursor.moveToNext()) 
				{  
					String thumbnail_uri = cursor.getString(ColumeIndex_THUMBNAIL);
					String has_phone = cursor.getString(ColumeIndex_HAS_PHONE_NUMBER);

					if(!has_phone.endsWith("0")) 
					{
						ArrayList<String> phoneNumbers = getPhoneNumbers(cursor.getString(ColumeIndex_ID));
						for(String phoneNumber : phoneNumbers){

							cg = new ContactOrGroup();
							cg.setMobileNumber(phoneNumber);
							cg.setName(cursor.getString(ColumeIndex_DISPLAY_NAME));

							cg.setThumbnailUri(thumbnail_uri);
							contacts.put(phoneNumber, cg);
						}									       
					} 					
				}					
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
		finally {
			if(cursor!=null){
				cursor.close();
			}
		}			
		return contacts;
	}	

	private static ArrayList<String> getPhoneNumbers(String id) 
	{
		ArrayList<String> numbers = new ArrayList<String>() ;
		//Cursor phones = getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + id, null, null);
		Cursor phones = mContext.getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + id, null, null);
		while (phones.moveToNext()) {

			numbers.add( phones.getString(phones.getColumnIndex(Phone.NUMBER)).replaceAll("\\s",""));			
		}

		phones.close();

		return numbers;
	}

	private static ArrayList<ContactOrGroup> getAllGroups() {

		ArrayList<ContactOrGroup> groups = new ArrayList<ContactOrGroup>();
		new ContactOrGroup("group",123,null);

		return groups ; 
	}
}
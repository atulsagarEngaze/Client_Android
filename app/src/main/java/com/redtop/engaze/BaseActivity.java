/*package com.redtop.engaze;

import java.util.Hashtable;

import com.redtop.engaze.entity.ContactOrGroup;
import com.redtop.engaze.entity.EventDetail;
import com.redtop.engaze.interfaces.OnRefreshMemberListCompleteListner;
import com.redtop.engaze.localbroadcastmanager.HomeBroadcastManager;
import com.redtop.engaze.utils.AppUtility;
import com.redtop.engaze.utils.Constants;
import com.redtop.engaze.utils.ContactAndGroupListManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public abstract class BaseActivity extends ActionSuccessFailMessageHandler  {	
	protected HomeBroadcastManager mBroadcastManager = null;
	protected BroadcastReceiver mNetworkUpdateBroadcastReceiver;	
	protected static Boolean mInternetStatus;		
	protected static final int START_DATE_DIALOG_ID = 1;
	protected static final int START_TIME_DIALOG_ID = 2;
	protected static final int EVENT_TYPE_REQUEST_CODE = 6;
	protected static final int REMINDER_REQUEST_CODE = 2;
	protected static final int TRACKING_REQUEST_CODE = 3;
	protected static final int ADD_INVITEES_REQUEST_CODE =4;
	protected static final int DURATION_REQUEST_CODE = 5;
	protected static final int LOCATION_REQUEST_CODE = 7;
	protected static final int EVENT_CREATE_REQUEST_CODE =8;
	protected static final int ROUTE_END_POINT_REQUEST_CODE = 9;
	protected static final int REQUEST_CODE_EMAIL = 10;
	protected static final int REQUEST_INVITE = 11;
	protected String TAG;
	protected static Boolean isFirstTime = false;
	public EventDetail notificationselectedEvent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		mInternetStatus = AppUtility.isNetworkAvailable(this);	

		mNetworkUpdateBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {	
				if(intent.getAction().equals(Constants.NETWORK_STATUS_UPDATE))
				{
					mInternetStatus = AppUtility.isNetworkAvailable(context);
					turnOnOfInternetAvailabilityMessage(context);
					if(mInternetStatus){
						onInternetConnectionResume();
					}
					else{
						onInternetConnectionLost();
					}
				}

			}
		};		
	}	

	protected void turnOnOfInternetAvailabilityMessage(Context context)
	{
		View v = findViewById(R.id.internet_status);
		if(v!=null){

			LinearLayout networkStatusLayout= (LinearLayout) v;
			if(mInternetStatus)
			{
				if(networkStatusLayout!=null)
				{
					networkStatusLayout.setVisibility(View.GONE);
				}			
			}
			else
			{
				if(networkStatusLayout!=null)
				{
					networkStatusLayout.setVisibility(View.VISIBLE);
				}			
			}
		}
	}
	
	protected void turnOnOfLocationAvailabilityMessage(Context context, Boolean locationAvailable)
	{
		// using the same Internet status layout to display the location unavailability message.
		View v = findViewById(R.id.internet_status);
		if(v!=null){

			LinearLayout locationStatusLayout= (LinearLayout) v;
			if(locationAvailable)
			{
				if(locationStatusLayout!=null)
				{
					locationStatusLayout.setVisibility(View.GONE);
				}			
			}
			else
			{
				if(locationStatusLayout!=null)
				{
					TextView locationAvailabilityTxt = (TextView) findViewById(R.id.txt_internet_unavailable_message);
					locationAvailabilityTxt.setText(getResources().getString(R.string.unable_locate_address));
					locationStatusLayout.setVisibility(View.VISIBLE);
				}			
			}
		}
	}


	@Override 
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mNetworkUpdateBroadcastReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();		
		LocalBroadcastManager.getInstance(this).registerReceiver(mNetworkUpdateBroadcastReceiver,
				new IntentFilter(Constants.NETWORK_STATUS_UPDATE));
		turnOnOfInternetAvailabilityMessage(this);
	}	

	protected void onInternetConnectionResume(){
	}

	protected void onInternetConnectionLost(){

	}	

	public void inviteFriend(){
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");		
		sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.message_invitation_success));
		sharingIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.message_invitation_body));
		startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.label_invitation_inviteUsing)));
	}

	protected void displayView(int position) {
		Intent intent = null ;
		switch (position) {
		case 0:
			if(TAG.equals(EventsActivity.class.getName())){
				finish();
			}
			else if(TAG.equals(HomeActivity.class.getName())){
				//do nothing
			}
			break;
		case 1:	
			if(TAG.equals(HomeActivity.class.getName())){
				intent = new Intent(this, EventsActivity.class);
			}
			else if(TAG.equals(EventsActivity.class.getName())){
				//do nothing
			}			
			break;

		case 2:			
			inviteFriend();
			break;

		case 3:
			intent = new Intent(this, MemberListActivity.class);
			break;
		case 4:
			intent = new Intent(this, EventSettingsActivity.class);
			break;

		case 5:
			intent = new Intent(this, FeedbackActivity.class);
			break;

		case 6:
			intent = new Intent(this, AboutActivity.class);
			break;

		default:
			break;
		}		
		if(intent != null){ 
			startActivity(intent);
		}
	}	

	public Boolean accessingContactsFirstTime(){
		if(BaseActivity.isFirstTime ){
			BaseActivity.isFirstTime = false;
			processMemberList();
			return true;
		}	
		return false;
	}

	protected void registeredMemberListCached(){

	}

	protected void memberListRefreshed_success(Hashtable<String, ContactOrGroup> memberList){

	}

	protected void memberListRefreshed_fail(){

	}



	private void processMemberList(){
		if (AppUtility.getPrefBoolean(Constants.IS_REGISTERED_CONTACT_LIST_INITIALIZED, mContext)){
			registeredMemberListCached();			
		}
		else if (AppUtility.getPrefBoolean(Constants.IS_CONTACT_LIST_INITIALIZED, mContext)){
			showProgressBar(getResources().getString(R.string.message_general_progressDialog));			
			ContactAndGroupListManager.initializedRegisteredUser(mContext, new OnRefreshMemberListCompleteListner() {

				@Override
				public void RefreshMemberListComplete(Hashtable<String, ContactOrGroup> memberList) {
					AppUtility.setPrefBoolean(Constants.IS_REGISTERED_CONTACT_LIST_INITIALIZED, true, mContext);					
					hideProgressBar();
					//accessingContactsFirstTime();
				}
			}, new OnRefreshMemberListCompleteListner() {

				@Override
				public void RefreshMemberListComplete(Hashtable<String, ContactOrGroup> memberList) {
					hideProgressBar();
					Toast.makeText(mContext,
							getResources().getString(R.string.message_contacts_errorRetrieveData), Toast.LENGTH_SHORT).show();
				}
			});
		}
		else {
			refreshMemberList();								
		}
	}
	
	protected void refreshMemberList(){		
		if(AppUtility.isNetworkAvailable(mContext))
		{
			
			Thread thread= new Thread(){
				@Override
				public void run(){
					ContactAndGroupListManager.cacheContactAndGroupList(mContext, new OnRefreshMemberListCompleteListner() {

						@Override
						public void RefreshMemberListComplete(Hashtable<String, ContactOrGroup> memberList) {
							AppUtility.setPref(Constants.IS_REGISTERED_CONTACT_LIST_INITIALIZED, "true", mContext);
							memberListRefreshed_success(memberList);
							
						}
					}, new OnRefreshMemberListCompleteListner() {

						@Override
						public void RefreshMemberListComplete(Hashtable<String, ContactOrGroup> memberList) {
							memberListRefreshed_fail();							
							Toast.makeText(mContext,
									getResources().getString(R.string.message_contacts_errorRetrieveData), Toast.LENGTH_SHORT).show();
						}
					});
				}
			};
			thread.start();
		}
		else
		{

			Toast.makeText(mContext,
					getResources().getString(R.string.internet_not_available), Toast.LENGTH_SHORT).show();
		}

	}
}
*/
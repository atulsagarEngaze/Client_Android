package com.redtop.engaze;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.redtop.engaze.adapter.MemberAdapter;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MemberListActivity extends BaseActivity1 implements SwipeRefreshLayout.OnRefreshListener, OnItemClickListener{

	ListView listView;
	List<String> rowItems;
	public  String[] friends ;
	//public ArrayList<ContactOrGroup>mMembers;
	public ArrayList<ContactOrGroup>mAllMembers;
	public MemberAdapter mAdapter;
	public BroadcastReceiver mMemberListBroadcastReceiver;
	public static Boolean isFirstTime = false;

	public String[]images ;
	private LinearLayout mLl_nocontacts;
	private SwipeRefreshLayout swipeRefreshLayout;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_friend_list);
		mLl_nocontacts = (LinearLayout)findViewById(R.id.ll_drawer_contacts_help_text);
		listView = (ListView) findViewById(R.id.friend_list);
		Toolbar toolbar = (Toolbar) findViewById(R.id.friend_toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
			getSupportActionBar().setTitle(R.string.title_friend_list);
			//toolbar.setSubtitle(R.string.title_event);
			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});
		}

		if(!accessingContactsFirstTime()){
			//mMembers = ContactAndGroupListManager.getAllRegisteredContacts(mContext);
			mAllMembers = ContactAndGroupListManager.getAllContacts();
			loadFriendList();
		}	

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_friendlist);
		swipeRefreshLayout.setOnRefreshListener(this);

	}



	private void loadFriendList(){										
		if(mAllMembers==null || mAllMembers.size()== 0)
		{
			mAllMembers = new ArrayList<ContactOrGroup>();
			inviteFriend();			
			mLl_nocontacts.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);

		}else{
			mLl_nocontacts.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			mAdapter = new MemberAdapter(this,
					R.layout.member_list_item, mAllMembers);
			listView.setAdapter(mAdapter);
			mAdapter.notifyDataSetChanged();

			listView.setOnItemClickListener(new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> adapter, View v, int position,
						long arg3) 
				{					
					final ContactOrGroup value = (ContactOrGroup)adapter.getItemAtPosition(position);	
					if(value.getUserId()!=null){	

						AlertDialog.Builder adb = null;
						adb = new AlertDialog.Builder(mContext);				

						adb.setTitle("Meet " + value.getName());
						adb.setMessage(getResources().getString(R.string.message_meetnow_memberlistactivity));
						adb.setIcon(android.R.drawable.ic_dialog_alert);

						adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Intent i = new Intent(mContext, TrackLocationActivity.class); 
								i.putExtra("EventTypeId", 6);
								i.putExtra("meetNowUserID", value.getUserId());
								startActivity(i);
							} });

						adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {							
								dialog.dismiss();						
							} });
						adb.show();
					}
				}
			});
		}
	}

	@Override
	protected void registeredMemberListCached(){
		//mMembers = ContactAndGroupListManager.getAllRegisteredContacts(mContext);
		mAllMembers = ContactAndGroupListManager.getAllContacts();
		loadFriendList();
	}

	@Override
	protected void memberListRefreshed_success(Hashtable<String, ContactOrGroup> memberList){
		if(memberList!=null && memberList.values().size()>0){

			//mMembers = ContactAndGroupListManager.sortContacts(new ArrayList<ContactOrGroup>(memberList.values()));
			loadFriendList();
		}	

		swipeRefreshLayout.setRefreshing(false);
	}

	@Override
	protected void memberListRefreshed_fail(){
		swipeRefreshLayout.setRefreshing(false);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_member, menu);
		
		MenuItem searchItem = menu.findItem(R.id.menu_search_member);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		//*** setOnQueryTextFocusChangeListener ***
		searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

			}
		});

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {

				return false;
			}

			@Override
			public boolean onQueryTextChange(String searchQuery) {
				mAdapter.filter(searchQuery.toString().trim());
				listView.invalidate();
				return true;
			}
		});

		MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				// Do something when collapsed
				return true;  // Return true to collapse action view
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// Do something when expanded
				return true;  // Return true to expand action view
			}
		});

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		int id = item.getItemId();
		switch(id){	        	
		//		case R.id.action_add_member:
		//			inviteFriend();
		//			break;
		case R.id.action_refresh_contactlist:
			swipeRefreshLayout.setRefreshing(true);
			refreshMemberList();
			break;
		}        

		return super.onOptionsItemSelected(item);
	}	

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		swipeRefreshLayout.setRefreshing(true);
		refreshMemberList();
		//swipeRefreshLayout.setRefreshing(false);
	}


}

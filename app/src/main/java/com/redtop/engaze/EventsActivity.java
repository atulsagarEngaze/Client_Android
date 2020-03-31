package com.redtop.engaze;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.redtop.engaze.adapter.EventsPagerAdapter;
import com.redtop.engaze.app.SlidingTabLayout;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.constant.DurationConstants;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.domain.EventDetail;
import com.redtop.engaze.entity.EventDetail;
import com.redtop.engaze.fragment.AcceptedEventsFragment;
import com.redtop.engaze.fragment.DeclinedEventsFragment;
import com.redtop.engaze.fragment.NavDrawerFragment;
import com.redtop.engaze.fragment.PendingEventsFragment;
import com.redtop.engaze.interfaces.OnRefreshEventListCompleteListner;
import com.redtop.engaze.utils.AppUtility;
import com.redtop.engaze.utils.Constants;
import com.redtop.engaze.utils.Constants.AcceptanceStatus;
import com.redtop.engaze.utils.Constants.Action;
import com.redtop.engaze.utils.EventHelper;
import com.redtop.engaze.utils.EventManager;
import com.redtop.engaze.utils.InternalCaching;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

@SuppressLint({ "NewApi", "Recycle" })
public class EventsActivity extends BaseActivity1 implements NavDrawerFragment.FragmentDrawerListener {


	private ViewPager pager;
	private EventsPagerAdapter tabAdapter;
	private SlidingTabLayout tabs;
	private CharSequence Titles[]={"Accepted","Pending","Declined"};
	private int Numboftabs =3;
	private int mStatusBarColor;	
	public AcceptedEventsFragment aef;
	public DeclinedEventsFragment def;
	public PendingEventsFragment pef;
	public  TypedArray mEventTypeImages;
	public ActionMode mActionMode;
	public View mCurrentItem;
	private LinearLayout mViewItemDetailRectangle;
	public HashMap<AcceptanceStatus, List<EventDetail>> mEventDetailHashmap= new HashMap<AcceptanceStatus, List<EventDetail>>();
	private BroadcastReceiver mEventBroadcastReceiver;
	private IntentFilter mFilter;
	private final Handler EventsRefreshHandler = new Handler();
	private Runnable EventsRefreshRunnable = new Runnable() {
		public void run() {	
			refreshEventFragments();
			EventsRefreshHandler.postDelayed(this, DurationConstants.EVENTS_REFRESH_INTERVAL); // 60 seconds here you can give
		}	
	};

	@Override
	protected void onResume() {	
		loadEventDetailHashmap(InternalCaching.getEventListFromCache(mContext));
		refreshEventFragments();
		LocalBroadcastManager.getInstance(this).registerReceiver(mEventBroadcastReceiver,
				mFilter);
		EventsRefreshHandler.post(EventsRefreshRunnable);
		super.onResume();						
	}

	@Override
	protected void onPause() {	
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mEventBroadcastReceiver);
		EventsRefreshHandler.removeCallbacks(EventsRefreshRunnable);
		super.onPause();		
	}


	@Override
	public void onBackPressed() {
		finish();
	}	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mContext = this;
		TAG = EventsActivity.class.getName();
		loadEventDetailHashmap(InternalCaching.getEventListFromCache(mContext));
		super.onCreate(savedInstanceState);			
		setContentView(R.layout.activity_events);
		turnOnOfInternetAvailabilityMessage(mContext);
		mEventTypeImages = getResources().obtainTypedArray(R.array.event_type_image);

		final Drawable mylocationImage = getResources().getDrawable(R.drawable.ic_my_location_black_18dp);
		mylocationImage.setColorFilter(getResources().getColor(R.color.secondary_text), PorterDuff.Mode.SRC_ATOP);

		Toolbar toolbar = (Toolbar) findViewById(R.id.event_list_toolbar);
		if (toolbar != null) {		
			setSupportActionBar(toolbar);			
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setTitle(getString(R.string.app_name));
			NavDrawerFragment drawerFragment = (NavDrawerFragment)
					getFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
			drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
			drawerFragment.setDrawerListener(this);
		}

		tabAdapter = new EventsPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs);
		pager = (ViewPager) findViewById(R.id.events_list_pager);
		pager.setAdapter(tabAdapter);
		tabs = (SlidingTabLayout) findViewById(R.id.events_list_tabs);
		tabs.setDistributeEvenly(true);
		tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
			@Override
			public int getIndicatorColor(int position) {
				return getResources().getColor(R.color.tabsScrollColor);
			}
		});
		tabs.setViewPager(pager);
		int deafaulttab = 0;
		deafaulttab = this.getIntent().getIntExtra("defaultTab", deafaulttab);
		pager.setCurrentItem(deafaulttab);
		
		mEventBroadcastReceiver = new BroadcastReceiver() {			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if(intent.getAction().equals(Constants.EVENT_RECEIVED)
						|| intent.getAction().equals(Constants.EVENT_OVER)
						|| intent.getAction().equals(Constants.EVENT_ENDED)
						|| intent.getAction().equals(Constants.EVENTS_REFRESHED)
						|| intent.getAction().equals(Constants.EVENT_USER_RESPONSE)
						|| intent.getAction().equals(Constants.EVENT_EXTENDED_BY_INITIATOR)						
						|| intent.getAction().equals(Constants.EVENT_ENDED_BY_INITIATOR)
						|| intent.getAction().equals(Constants.EVENT_DELETE_BY_INITIATOR)
						|| intent.getAction().equals(Constants.EVENT_UPDATED_BY_INITIATOR)
						|| intent.getAction().equals(Constants.EVENT_DESTINATION_UPDATED_BY_INITIATOR)						
						|| intent.getAction().equals(Constants.REMOVED_FROM_EVENT_BY_INITIATOR)
						|| intent.getAction().equals(Constants.EVENT_PARTICIPANTS_UPDATED_BY_INITIATOR)						
						)
				{
					loadEventDetailHashmap(InternalCaching.getEventListFromCache(mContext));
					refreshEventFragments();
				}
			}
		};

		mFilter = new IntentFilter();
		mFilter.addAction(Constants.EVENT_RECEIVED);
		mFilter.addAction(Constants.EVENT_USER_RESPONSE);
		mFilter.addAction(Constants.EVENT_OVER);
		mFilter.addAction(Constants.EVENT_ENDED);
		mFilter.addAction(Constants.EVENTS_REFRESHED);
		mFilter.addAction(Constants.EVENT_EXTENDED_BY_INITIATOR);		
		mFilter.addAction(Constants.EVENT_ENDED_BY_INITIATOR);
		mFilter.addAction(Constants.EVENT_DELETE_BY_INITIATOR);	
		mFilter.addAction(Constants.EVENT_UPDATED_BY_INITIATOR);
		mFilter.addAction(Constants.EVENT_DESTINATION_UPDATED_BY_INITIATOR);		
		mFilter.addAction(Constants.REMOVED_FROM_EVENT_BY_INITIATOR);
		mFilter.addAction(Constants.EVENT_PARTICIPANTS_UPDATED_BY_INITIATOR);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_create_event, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		Intent intent = null ;
		switch(id){
		case R.id.action_add:
			intent = new Intent(this, CreateEditEventActivity.class); 
			startActivity(intent);	
			finish();
			break;
		case R.id.action_track:
			AppUtility.setPrefArrayList("Invitees", null, mContext);
			intent = new Intent(this, TrackLocationActivity.class);
			intent.putExtra("EventTypeId", 6);
			startActivity(intent);
			finish();
			break;
		case R.id.action_refresh:
			showProgressBar(getResources().getString(R.string.message_general_progressDialog));
			EventsRefreshHandler.removeCallbacks(EventsRefreshRunnable);
			EventManager.refreshEventList(this, 

					new OnRefreshEventListCompleteListner() {			
				@Override
				public void RefreshEventListComplete(List<EventDetail> eventDetailList) {
					loadEventDetailHashmap(eventDetailList);
					EventsRefreshHandler.post(EventsRefreshRunnable);
					hideProgressBar();
				}

			}, this);

			break;	
		}		

		return super.onOptionsItemSelected(item);
	}	

	public void refreshEventFragments() {		
		if(aef!=null){
			aef.updateEventFragment(mEventDetailHashmap.get(AcceptanceStatus.ACCEPTED));
		}
		if(pef!=null){
			pef.updateEventFragment(mEventDetailHashmap.get(AcceptanceStatus.PENDING));
		}
		if(def!=null){
			def.updateEventFragment(mEventDetailHashmap.get(AcceptanceStatus.DECLINED));
		}	
	}

	public void loadEventDetailHashmap(List<EventDetail>eventList){
		EventHelper.SortListByStartDate(eventList);
		if(mEventDetailHashmap ==  null){
			mEventDetailHashmap = new HashMap<Constants.AcceptanceStatus, List<EventDetail>>();
		}
		else
		{
			mEventDetailHashmap.clear();
		}
		ArrayList<EventDetail> al =  new ArrayList<EventDetail>();
		ArrayList<EventDetail> pl =  new ArrayList<EventDetail>();
		ArrayList<EventDetail> dl =  new ArrayList<EventDetail>();
		for(EventDetail ed : eventList){
			if(Integer.parseInt(ed.getEventTypeId()) <= 100){
			switch (ed.getCurrentMember().getAcceptanceStatus()) {
			case ACCEPTED:
				al.add(ed);
				break;
			case PENDING:
				pl.add(ed);
				break;
			case DECLINED:
				dl.add(ed);
				break;

			default:
				break;
			}
			}
		}		
		mEventDetailHashmap.put(AcceptanceStatus.ACCEPTED, al);
		mEventDetailHashmap.put(AcceptanceStatus.PENDING, pl);
		mEventDetailHashmap.put(AcceptanceStatus.DECLINED, dl);

	}

	@Override
	public void onDrawerItemSelected(View view, int position) {
		displayView(position);
	}

	public ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.menu_events_context_action, menu);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {				

				//hold current color of status bar
				mStatusBarColor = getWindow().getStatusBarColor();
				//set your gray color
				getWindow().setStatusBarColor(getResources().getColor(R.color.secondary_text));
			}
			else{

				MenuItem liveitem = null;
				Drawable originalDrawable = null;
				Drawable wrappedDrawable = null;

				for(int i = 0; i < menu.size(); i++){
					liveitem =  menu.getItem(i);					
					originalDrawable = liveitem.getIcon();
					wrappedDrawable = DrawableCompat.wrap(originalDrawable);
					DrawableCompat.setTint(wrappedDrawable, mContext.getResources().getColor(R.color.icon) );
					liveitem.setIcon(wrappedDrawable);
				}

			}
			return true;
		}

		// Called each time the action mode is shown. Always called after onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			final EventDetail eventDetail = (EventDetail)mode.getTag();
			if(item.getItemId()!=R.id.context_action_mute_unmute){
				showProgressBar(getResources().getString(R.string.message_general_progressDialog));
			}

			switch (item.getItemId()) {

			case R.id.context_action_mute_unmute:

				Drawable dr = null;
				if(eventDetail.isMute){
					eventDetail.isMute = false;
					dr = getResources().getDrawable(R.drawable.event_unmute);
				}
				else{
					eventDetail.isMute = true;
					dr = getResources().getDrawable(R.drawable.event_mute);
				}

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					Drawable wrappedDrawable = DrawableCompat.wrap(dr);
					DrawableCompat.setTint(wrappedDrawable, mContext.getResources().getColor(R.color.icon) );
					item.setIcon(wrappedDrawable);
				}
				else{
					item.setIcon(dr);					
				}
				
				InternalCaching.saveEventToCache(eventDetail, mContext);

				return true;

			case R.id.context_action_accept:

				EventManager.saveUserResponse(AcceptanceStatus.ACCEPTED, mContext, eventDetail.getEventId(), 
						(EventsActivity)mContext, (EventsActivity)mContext);

				mode.finish();
				return true;
			case R.id.context_action_decline:

				EventManager.saveUserResponse(AcceptanceStatus.DECLINED, mContext, eventDetail.getEventId(), 
						(EventsActivity)mContext, (EventsActivity)mContext);

				mode.finish();
				return true;
			case R.id.context_action_edit:
				//shareCurrentItem();
				hideProgressBar();
				mode.finish();
				Intent i = new Intent(mContext, CreateEditEventActivity.class); 
				i.putExtra("IsForEdit", "true");
				i.putExtra("EventDetail", eventDetail);
				startActivity(i);
				return true;
			case R.id.context_action_delete:

				if(AppUtility.isCurrentUserInitiator(eventDetail.getInitiatorId(), mContext)){

					AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
					// adb.setView(alertDialogView);

					adb.setTitle("Cancel Event");
					adb.setMessage("Are you sure to Delete this Event?");
					adb.setIcon(android.R.drawable.ic_dialog_alert);

					adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							EventManager.deleteEvent(mContext, eventDetail, (EventsActivity)mContext, (EventsActivity)mContext);
						} });

					adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {							
							dialog.dismiss();
						} });
					adb.show();
				}
				else{
					Toast.makeText(mContext,
							"Oops! Only the Event Initiator can Cancel the Event!!",
							Toast.LENGTH_LONG).show();
				}

				hideProgressBar();
				mode.finish();
				return true;
			default:
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mViewItemDetailRectangle = (LinearLayout)mCurrentItem.findViewById(R.id.ll_detail_rectangle);
			mViewItemDetailRectangle.setBackground(mContext.getResources().getDrawable( R.drawable.event_detail_rectangle));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().setStatusBarColor(mStatusBarColor);
			}			
			mActionMode = null;
		}
	};
	@Override	
	public void actionFailed(String msg, Action action) {
		if(action==Action.REFRESHEVENTLIST){
			EventsRefreshHandler.post(EventsRefreshRunnable);
		}
		super.actionFailed(msg, action);		
	}
	@Override
	public void actionComplete(Action action) {
		if(action == Action.SAVEUSERRESPONSE){
		loadEventDetailHashmap(InternalCaching.getEventListFromCache(mContext));
		}
		refreshEventFragments();
		super.actionComplete(action);
	}
}

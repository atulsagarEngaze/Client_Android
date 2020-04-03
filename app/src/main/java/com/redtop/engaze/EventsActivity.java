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
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.redtop.engaze.Interface.IActionHandler;
import com.redtop.engaze.Interface.OnRefreshEventListCompleteListner;
import com.redtop.engaze.adapter.EventsPagerAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.PreffManager;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.constant.DurationConstants;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.common.customeviews.SlidingTabLayout;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.domain.service.ParticipantService;
import com.redtop.engaze.fragment.AcceptedEventsFragment;
import com.redtop.engaze.fragment.DeclinedEventsFragment;
import com.redtop.engaze.fragment.NavDrawerFragment;
import com.redtop.engaze.fragment.PendingEventsFragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

@SuppressLint({ "NewApi", "Recycle" })
public class EventsActivity extends BaseActivity implements NavDrawerFragment.FragmentDrawerListener, IActionHandler {


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
	public HashMap<AcceptanceStatus, List<Event>> mEventDetailHashmap= new HashMap<AcceptanceStatus, List<Event>>();
	private BroadcastReceiver mEventBroadcastReceiver;
	private IntentFilter mFilter;

	private final static String TAG = EventsActivity.class.getName();

	private final Handler EventsRefreshHandler = new Handler();

	private Runnable EventsRefreshRunnable = new Runnable() {
		public void run() {	
			refreshEventFragments();
			EventsRefreshHandler.postDelayed(this, DurationConstants.EVENTS_REFRESH_INTERVAL); // 60 seconds here you can give
		}	
	};

	@Override
	protected void onResume() {	
		loadEventDetailHashmap(InternalCaching.getEventListFromCache());
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
		loadEventDetailHashmap(InternalCaching.getEventListFromCache());
		super.onCreate(savedInstanceState);			
		setContentView(R.layout.activity_events);
		turnOnOfInternetAvailabilityMessage();
		mEventTypeImages = getResources().obtainTypedArray(R.array.event_type_image);

		final Drawable mylocationImage = getResources().getDrawable(R.drawable.ic_my_location_black_18dp);
		mylocationImage.setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);

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
				if(intent.getAction().equals(Veranstaltung.EVENT_RECEIVED)
						|| intent.getAction().equals(Veranstaltung.EVENT_OVER)
						|| intent.getAction().equals(Veranstaltung.EVENT_ENDED)
						|| intent.getAction().equals(Veranstaltung.EVENTS_REFRESHED)
						|| intent.getAction().equals(Veranstaltung.EVENT_USER_RESPONSE)
						|| intent.getAction().equals(Veranstaltung.EVENT_EXTENDED_BY_INITIATOR)
						|| intent.getAction().equals(Veranstaltung.EVENT_ENDED_BY_INITIATOR)
						|| intent.getAction().equals(Veranstaltung.EVENT_DELETE_BY_INITIATOR)
						|| intent.getAction().equals(Veranstaltung.EVENT_UPDATED_BY_INITIATOR)
						|| intent.getAction().equals(Veranstaltung.EVENT_DESTINATION_UPDATED_BY_INITIATOR)
						|| intent.getAction().equals(Veranstaltung.REMOVED_FROM_EVENT_BY_INITIATOR)
						|| intent.getAction().equals(Veranstaltung.EVENT_PARTICIPANTS_UPDATED_BY_INITIATOR)
						)
				{
					loadEventDetailHashmap(InternalCaching.getEventListFromCache());
					refreshEventFragments();
				}
			}
		};

		mFilter = new IntentFilter();
		mFilter.addAction(Veranstaltung.EVENT_RECEIVED);
		mFilter.addAction(Veranstaltung.EVENT_USER_RESPONSE);
		mFilter.addAction(Veranstaltung.EVENT_OVER);
		mFilter.addAction(Veranstaltung.EVENT_ENDED);
		mFilter.addAction(Veranstaltung.EVENTS_REFRESHED);
		mFilter.addAction(Veranstaltung.EVENT_EXTENDED_BY_INITIATOR);
		mFilter.addAction(Veranstaltung.EVENT_ENDED_BY_INITIATOR);
		mFilter.addAction(Veranstaltung.EVENT_DELETE_BY_INITIATOR);
		mFilter.addAction(Veranstaltung.EVENT_UPDATED_BY_INITIATOR);
		mFilter.addAction(Veranstaltung.EVENT_DESTINATION_UPDATED_BY_INITIATOR);
		mFilter.addAction(Veranstaltung.REMOVED_FROM_EVENT_BY_INITIATOR);
		mFilter.addAction(Veranstaltung.EVENT_PARTICIPANTS_UPDATED_BY_INITIATOR);
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
			PreffManager.setPrefArrayList("Invitees", null);
			intent = new Intent(this, TrackLocationActivity.class);
			intent.putExtra("EventTypeId", 6);
			startActivity(intent);
			finish();
			break;
		case R.id.action_refresh:
			showProgressBar(getResources().getString(R.string.message_general_progressDialog));
			EventsRefreshHandler.removeCallbacks(EventsRefreshRunnable);
			EventManager.refreshEventList(

					new OnRefreshEventListCompleteListner() {
				@Override
				public void RefreshEventListComplete(List<Event> eventList) {
					loadEventDetailHashmap(eventList);
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

	public void loadEventDetailHashmap(List<Event>eventList){
		EventService.SortListByStartDate(eventList);
		if(mEventDetailHashmap ==  null){
			mEventDetailHashmap = new HashMap<AcceptanceStatus, List<Event>>();
		}
		else
		{
			mEventDetailHashmap.clear();
		}
		ArrayList<Event> al =  new ArrayList<Event>();
		ArrayList<Event> pl =  new ArrayList<Event>();
		ArrayList<Event> dl =  new ArrayList<Event>();
		for(Event ed : eventList){
			if(Integer.parseInt(ed.getEventTypeId()) <= 100){
			switch (ed.getCurrentParticipant().getAcceptanceStatus()) {
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
				getWindow().setStatusBarColor(getResources().getColor(R.color.secondaryText));
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
			final Event event = (Event)mode.getTag();
			if(item.getItemId()!=R.id.context_action_mute_unmute){
				showProgressBar(getResources().getString(R.string.message_general_progressDialog));
			}

			switch (item.getItemId()) {

			case R.id.context_action_mute_unmute:

				Drawable dr = null;
				if(event.isMute){
					event.isMute = false;
					dr = getResources().getDrawable(R.drawable.event_unmute);
				}
				else{
					event.isMute = true;
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
				
				InternalCaching.saveEventToCache(event);

				return true;

			case R.id.context_action_accept:

				EventManager.saveUserResponse(AcceptanceStatus.ACCEPTED, event.getEventId(),
						EventsActivity.this, EventsActivity.this);

				mode.finish();
				return true;
			case R.id.context_action_decline:

				EventManager.saveUserResponse(AcceptanceStatus.DECLINED,  event.getEventId(),
						EventsActivity.this, EventsActivity.this);

				mode.finish();
				return true;
			case R.id.context_action_edit:
				//shareCurrentItem();
				hideProgressBar();
				mode.finish();
				Intent i = new Intent(mContext, CreateEditEventActivity.class); 
				i.putExtra("IsForEdit", "true");
				i.putExtra("EventDetail", event);
				startActivity(i);
				return true;
			case R.id.context_action_delete:

				if(ParticipantService.isCurrentUserInitiator(event.getInitiatorId())){

					AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
					// adb.setView(alertDialogView);

					adb.setTitle("Cancel Event");
					adb.setMessage("Are you sure to Delete this Event?");
					adb.setIcon(android.R.drawable.ic_dialog_alert);

					adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							EventManager.deleteEvent(event, EventsActivity.this);
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
		AppContext.actionHandler.actionFailed(msg, action);
	}
	@Override
	public void actionComplete(Action action) {
		if(action == Action.SAVEUSERRESPONSE){
		loadEventDetailHashmap(InternalCaching.getEventListFromCache());
		}
		refreshEventFragments();
		AppContext.actionHandler.actionComplete(action);
	}

	@Override
	public void actionCancelled(Action action) {

	}
}

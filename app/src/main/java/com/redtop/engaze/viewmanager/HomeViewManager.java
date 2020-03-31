package com.redtop.engaze.viewmanager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.HomeActivity;
import com.redtop.engaze.R;
import com.redtop.engaze.adapter.HomePendingEventListAdapter;
import com.redtop.engaze.adapter.HomeRunningEventListAdapter;
import com.redtop.engaze.adapter.HomeTrackLocationListAdapter;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.fragment.NavDrawerFragment;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

// this can be a person from contact list or can be a group which will be resolved to actual contact at server
public class HomeViewManager  extends LocationViewManager {	

	public RelativeLayout mHomeTrackBuddyListView;	
	private RelativeLayout mTrackBuddyImageButtonLayout;
	private ImageButton mCurrentTrackBuddyListButton;
	public ListView mHomeTrackBuddyList;
	private TextView mTxtTrackBuddyEventListItemCount;

	public RelativeLayout mHomeShareMyLocationtListView;
	private RelativeLayout mShareMyLocationImageButtonLayout;
	private ImageButton mCurrentShareMyLocationListButton;
	public ListView mHomeShareMyLocationList;	
	private TextView mTxtShareMyLocationEventListItemCount;

	public RelativeLayout mHomePendingEventListView;
	public RelativeLayout mPendingImageButtonLayout;
	private ImageButton mCurrentPendingEventListButton;
	public ListView mHomePendingEventList;
	public TextView mTxtPendingEventListItemCount;

	public RelativeLayout mHomeRunningEventListView;
	public RelativeLayout mRunningImageButtonLayout;
	private ImageButton mCurrentRunningEventListButton;
	public ListView mHomeRunningEventList;
	public TextView mTxtRunningEventListItemCount;
	public ImageButton mImgBtnMeetNow;
	public ImageButton mImgBtnMeetLater;
	public ImageButton mImgBtnMeetTrackBuddy;
	public ImageButton mImgBtnMeetShareMyLoc;

	public RelativeLayout rlEventList ;	
	private HomeActivity activity;

	public HomeViewManager(Context context) {
		super(context);
		activity = (HomeActivity)context;
		setToolBar();
		initializeElements();
		setClickListener();				
	}

	private void setToolBar(){
		Toolbar toolbar = (Toolbar)activity.findViewById(R.id.home_toolbar);
		if (toolbar != null) {
			activity.setSupportActionBar(toolbar);			
			activity.getSupportActionBar().setDisplayShowHomeEnabled(true);			
			NavDrawerFragment drawerFragment = (NavDrawerFragment)
					activity.getFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
			drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) activity.findViewById(R.id.drawer_layout), toolbar);
			drawerFragment.setDrawerListener(activity);

			toolbar.setOnTouchListener(new OnTouchListener() {
				Handler handler = new Handler();

				int numberOfTaps = 0;
				long lastTapTimeMs = 0;
				long touchDownMs = 0;
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						touchDownMs = System.currentTimeMillis();
						break;
					case MotionEvent.ACTION_UP:
						handler.removeCallbacksAndMessages(null);

						if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
							//it was not a tap

							numberOfTaps = 0;
							lastTapTimeMs = 0;
							break;
						}

						if (numberOfTaps > 0 
								&& (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()) {
							numberOfTaps += 1;
						} else {
							numberOfTaps = 1;
						}

						lastTapTimeMs = System.currentTimeMillis();

						if (numberOfTaps == 5) {		                    
							//handle triple tap
							if(Constants.DEBUG){
								Constants.DEBUG = false;
								Toast.makeText(activity, "DEBUG mode Disabled!", Toast.LENGTH_SHORT).show();
							}else{
								Constants.DEBUG = true;
								Toast.makeText(activity, "DEBUG mode Enabled!", Toast.LENGTH_SHORT).show();

							}		                   
						}
					}

					return true;
				}			
			});
		}
	}

	@Override
	protected void initializeElements(){
		mTrackBuddyImageButtonLayout = (RelativeLayout)activity.findViewById(R.id.rl_hn_track_buddy_events);
		mCurrentTrackBuddyListButton = (ImageButton)activity.findViewById(R.id.img_hn_buddy_tracking);
		mTxtTrackBuddyEventListItemCount = (TextView)activity.findViewById(R.id.txt_track_buddy_events );
		mHomeTrackBuddyListView  = (RelativeLayout)activity.findViewById(R.id.rl_home_trackbuddy_list );
		mHomeTrackBuddyList = (ListView)activity.findViewById(R.id.home_trackbuddy_list);

		mShareMyLocationImageButtonLayout = (RelativeLayout)activity.findViewById(R.id.rl_hn_share_location_events);
		mCurrentShareMyLocationListButton = (ImageButton)activity.findViewById(R.id.img_hn_location_sharing);
		mTxtShareMyLocationEventListItemCount = (TextView)activity.findViewById(R.id.txt_sharing_location_events );
		mHomeShareMyLocationtListView  = (RelativeLayout)activity.findViewById(R.id.rl_home_sharemylocation_list );
		mHomeShareMyLocationList = (ListView)activity.findViewById(R.id.home_sharemylocation_list);

		mPendingImageButtonLayout = (RelativeLayout)activity.findViewById(R.id.rl_hn_pending_events);
		mCurrentPendingEventListButton = (ImageButton)activity.findViewById(R.id.img_hn_pending_events);
		mTxtPendingEventListItemCount = (TextView)activity.findViewById(R.id.txt_unread_events );
		mHomePendingEventListView  = (RelativeLayout)activity.findViewById(R.id.rl_home_pending_event_list );
		mHomePendingEventList = (ListView)activity.findViewById(R.id.home_pending_event_list);

		mRunningImageButtonLayout = (RelativeLayout)activity.findViewById(R.id.rl_hn_running_events);
		mCurrentRunningEventListButton = (ImageButton)activity.findViewById(R.id.img_hn_running_events);
		mTxtRunningEventListItemCount = (TextView)activity.findViewById(R.id.txt_running_events );
		mHomeRunningEventListView  = (RelativeLayout)activity.findViewById(R.id.rl_home_running_event_list );
		mHomeRunningEventList = (ListView)activity.findViewById(R.id.home_running_event_list);
		mImgBtnMeetNow = (ImageButton)activity.findViewById(R.id.img_meet_now);
		mImgBtnMeetLater =  (ImageButton)activity.findViewById(R.id.img_meet_later);
		mImgBtnMeetTrackBuddy = (ImageButton)activity.findViewById(R.id.img_track_buddy);
		mImgBtnMeetShareMyLoc = (ImageButton)activity.findViewById(R.id.img_share_mylocation);

		mSearchLocationTextLength = Constants.HOME_ACTIVITY_LOCATION_TEXT_LENGTH;

		mPin= (ImageView)activity.findViewById(R.id.img_center_pin);
		mPin.setVisibility(View.GONE);
		//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
		//			final Drawable originalDrawable = mIconSearchClear.getBackground();
		//			final Drawable wrappedDrawable = DrawableCompat.wrap(originalDrawable);
		//			DrawableCompat.setTint(wrappedDrawable, activity.getResources().getColor(R.color.icon) );
		//			mIconSearchClear.setBackground(wrappedDrawable);
		//			rlSearchView = (RelativeLayout)activity.findViewById(R.id.ll_pick_location);
		//			rlSearchView.setBackground(activity.getResources().getDrawable(R.drawable.my_location_textbox));
		//		}		

		if(AppUtility.deviceDensity <320){
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mImgBtnMeetTrackBuddy.getLayoutParams();
			params.setMargins(10, 0, 10, 2); //substitute parameters for left, top, right, bottom
			mImgBtnMeetTrackBuddy.setLayoutParams(params);
			LinearLayout.LayoutParams paramsml = (LinearLayout.LayoutParams)mImgBtnMeetLater.getLayoutParams();
			paramsml.setMargins(10, 0, 10, 2); //substitute parameters for left, top, right, bottom
			mImgBtnMeetLater.setLayoutParams(paramsml);
			LinearLayout.LayoutParams paramsh = (LinearLayout.LayoutParams)mImgBtnMeetShareMyLoc.getLayoutParams();
			paramsh.setMargins(10, 0, 10, 2); //substitute parameters for left, top, right, bottom
			mImgBtnMeetShareMyLoc.setLayoutParams(paramsh);
			TextView txtTB = (TextView)activity.findViewById(R.id.txt_track_buddy);
			txtTB.setTextSize(11);
			txtTB = (TextView)activity.findViewById(R.id.txt_share_my_location);
			txtTB.setTextSize(11);
			txtTB = (TextView)activity.findViewById(R.id.txt_meet_later);
			txtTB.setTextSize(11);
			txtTB = (TextView)activity.findViewById(R.id.txt_meet_now);
			txtTB.setTextSize(11);
		}		
		super.initializeElements();
	}

	@Override
	protected void setClickListener(){
		mImgBtnMeetNow.setOnClickListener(this);
		mImgBtnMeetLater.setOnClickListener(this);	
		mImgBtnMeetTrackBuddy.setOnClickListener(this);
		mImgBtnMeetShareMyLoc.setOnClickListener(this);
		mCurrentPendingEventListButton.setOnClickListener(this);
		mCurrentRunningEventListButton.setOnClickListener(this);
		mCurrentShareMyLocationListButton.setOnClickListener(this);
		mCurrentTrackBuddyListButton.setOnClickListener(this);
		mRunningImageButtonLayout.setOnClickListener(this);
		mPendingImageButtonLayout.setOnClickListener(this);
		mShareMyLocationImageButtonLayout.setOnClickListener(this);
		mTrackBuddyImageButtonLayout.setOnClickListener(this);
		super.setClickListener();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){		
		case R.id.img_meet_now:			
			activity.onMeetNowClicked();			
			break;
			
		case R.id.img_meet_later:
			activity.onMeetLaterClicked();
			break;
			
		case R.id.img_share_mylocation:
			activity.onShareMyLocationClicked();
			break;
			
		case R.id.img_track_buddy:
			activity.onTrackBuddyClicked();
			break;
			
		case R.id.img_hn_pending_events:
			activity.onShowCurrentPendingEventListButtonClicked();
			break;
			
		case R.id.img_hn_running_events:
			activity.onShowCurrentRunningEventListButtonClicked();
			break;
			
		case R.id.img_hn_location_sharing:
			activity.onShowCurrentShareMyLocationListButtonClicked();	
			break;
		case R.id.img_hn_buddy_tracking:
			activity.onShowCurrentTrackBuddyListButtonClicked();
			break;
			
		case R.id.rl_hn_running_events:
			activity.onShowCurrentRunningEventListButtonClicked();
			break;
			
		case R.id.rl_hn_track_buddy_events:
			activity.onShowCurrentTrackBuddyListButtonClicked();
			break;
			
		case R.id.rl_hn_share_location_events:
			activity.onShowCurrentShareMyLocationListButtonClicked();
			break;
			
		case R.id.rl_hn_pending_events:
			activity.onShowCurrentPendingEventListButtonClicked();
			break;
		}

		super.onClick(v);
	}

	public void setPendingEventListViewAdapter(HomePendingEventListAdapter adapter){
		mHomePendingEventList.setAdapter(adapter);
	}

	public void updatePendingEventListView(HomePendingEventListAdapter adapter){
		mHomePendingEventList.setAdapter(adapter);		
	}

	public void showPendingEventListViewLayout(HomePendingEventListAdapter adapter, int count){
		this.updatePendingEventListView(adapter);
		mPendingImageButtonLayout.setVisibility(View.VISIBLE);
		mTxtPendingEventListItemCount.setText(Integer.toString(count));
	}		

	public void hidePendingEventListAndButtonViewLayout() {
		mPendingImageButtonLayout.setVisibility(View.GONE);	
		mHomePendingEventListView.setVisibility(View.GONE);
	}

	public void hidePendingEventListViewLayout() {
		//mCurrentPendingEventListButton.setBackground(activity.getResources().getDrawable(R.drawable.ripple_lightgray));
		mHomePendingEventListView.setVisibility(View.GONE);
	}

	public void hideShareMyLocationListViewLayout() {
		//mCurrentShareMyLocationListButton.setBackground(activity.getResources().getDrawable(R.drawable.ripple_lightgray));
		mHomeShareMyLocationtListView.setVisibility(View.GONE);
	}

	public void hideShareMyLocationListAndButtonViewLayout() {
		//mCurrentShareMyLocationListButton.setVisibility(View.GONE);	
		mShareMyLocationImageButtonLayout .setVisibility(View.GONE);
		mHomeShareMyLocationtListView.setVisibility(View.GONE);
	}

	public void showShareMyLocationListViewLayout(
			HomeTrackLocationListAdapter adapter, int count) {
		this.updateShareMyLocationListView(adapter);
		//		mCurrentShareMyLocationListButton.setVisibility(View.VISIBLE);	
		mShareMyLocationImageButtonLayout .setVisibility(View.VISIBLE);
		mTxtShareMyLocationEventListItemCount.setText(Integer.toString(count));
	}

	public void updateShareMyLocationListView(HomeTrackLocationListAdapter adapter){
		mHomeShareMyLocationList.setAdapter(adapter);		
	}	

	public void hideTrackBuddyListViewLayout() {
		//mCurrentTrackBuddyListButton.setBackground(activity.getResources().getDrawable(R.drawable.ripple_lightgray));
		mHomeTrackBuddyListView.setVisibility(View.GONE);
	}

	public void hideTrackBuddyListAndButtonViewLayout() {
		//mCurrentTrackBuddyListButton.setVisibility(View.GONE);	
		mTrackBuddyImageButtonLayout.setVisibility(View.GONE);
		mHomeTrackBuddyListView.setVisibility(View.GONE);
	}

	public void showTrackBuddyListViewLayout(
			HomeTrackLocationListAdapter adapter, int count) {
		this.updateTrackBuddyListView(adapter);
		//mCurrentTrackBuddyListButton.setVisibility(View.VISIBLE);	
		mTrackBuddyImageButtonLayout.setVisibility(View.VISIBLE);	
		mTxtTrackBuddyEventListItemCount.setText(Integer.toString(count));
	}

	public void updateTrackBuddyListView(HomeTrackLocationListAdapter adapter){
		mHomeTrackBuddyList.setAdapter(adapter);		
	}

	public void toggleTrackBuddyListView() {
		mHomeRunningEventListView.setVisibility(View.GONE);
		mHomePendingEventListView.setVisibility(View.GONE);
		mHomeShareMyLocationtListView.setVisibility(View.GONE);
		mCurrentShareMyLocationListButton.setSelected(false);
		mCurrentPendingEventListButton.setSelected(false);
		mCurrentRunningEventListButton.setSelected(false);
		toggleListView(mHomeTrackBuddyListView,mCurrentTrackBuddyListButton, R.id.img_arrow_tb);		
	}

	public void toggleRunningEventListView() {
		mHomeTrackBuddyListView.setVisibility(View.GONE);
		mHomePendingEventListView.setVisibility(View.GONE);
		mHomeShareMyLocationtListView.setVisibility(View.GONE);
		mCurrentShareMyLocationListButton.setSelected(false);
		mCurrentPendingEventListButton.setSelected(false);
		mCurrentTrackBuddyListButton.setSelected(false);
		toggleListView(mHomeRunningEventListView, mCurrentRunningEventListButton, R.id.img_arrow_re);			
	}

	public void togglePendingEventListView() {
		mHomeTrackBuddyListView.setVisibility(View.GONE);
		mHomeRunningEventListView.setVisibility(View.GONE);
		mHomeShareMyLocationtListView.setVisibility(View.GONE);
		mCurrentShareMyLocationListButton.setSelected(false);		
		mCurrentRunningEventListButton.setSelected(false);
		mCurrentTrackBuddyListButton.setSelected(false);
		toggleListView(mHomePendingEventListView, mCurrentPendingEventListButton, R.id.img_arrow_pe);			
	}

	public void toggleShareMyLocationListView() {	
		mHomeTrackBuddyListView.setVisibility(View.GONE);
		mHomeRunningEventListView.setVisibility(View.GONE);
		mHomePendingEventListView.setVisibility(View.GONE);		
		mCurrentPendingEventListButton.setSelected(false);
		mCurrentRunningEventListButton.setSelected(false);
		mCurrentTrackBuddyListButton.setSelected(false);
		toggleListView(mHomeShareMyLocationtListView, mCurrentShareMyLocationListButton, R.id.img_arrow_sml);		
	}

	public boolean isAnyNotificationListViewVisible(){
		return mHomeTrackBuddyListView.getVisibility()==View.VISIBLE ||
				mHomeRunningEventListView.getVisibility()==View.VISIBLE ||
				mHomePendingEventListView.getVisibility()==View.VISIBLE ||
				mHomeShareMyLocationtListView.getVisibility()==View.VISIBLE;
	}

	public void hideAllListViewLayout(){
		mCurrentShareMyLocationListButton.setSelected(false);
		mCurrentPendingEventListButton.setSelected(false);
		mCurrentRunningEventListButton.setSelected(false);
		mCurrentTrackBuddyListButton.setSelected(false);
		mHomeTrackBuddyListView.setVisibility(View.GONE);
		mHomeRunningEventListView.setVisibility(View.GONE);
		mHomePendingEventListView.setVisibility(View.GONE);
		mHomeShareMyLocationtListView.setVisibility(View.GONE);
	}

	private void toggleListView(final RelativeLayout listViewLayout, ImageButton listViewShowHideButton, int arrowId ){
		if(listViewLayout.getVisibility()== View.GONE){
			listViewShowHideButton.setSelected(true);		
			hideAllListViewExceptThis(listViewLayout);
			int[] loc = new int[2]; 
			listViewShowHideButton.getLocationInWindow(loc);
			activity.findViewById(arrowId).setX(loc[0] + 10);			
			//mCurrentRunningEventListButton.setBackground(activity.getResources().getDrawable(R.drawable.ripple_gray));
			listViewLayout.setVisibility(View.VISIBLE);
			listViewLayout.setAlpha(0.0f);
			listViewLayout.animate()
			.translationY(0)
			.alpha(1.0f)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					listViewLayout.setVisibility(View.VISIBLE);
				}
			});
		}
		else{
			listViewShowHideButton.setSelected(false);			
			//mCurrentRunningEventListButton.setBackground(activity.getResources().getDrawable(R.drawable.ripple_lightgray));
			listViewLayout.setVisibility(View.GONE);			
		}
	}

	public void hideRunningEventListAndButtonViewLayout() {
		mRunningImageButtonLayout.setVisibility(View.GONE);	
		mHomeRunningEventListView.setVisibility(View.GONE);

	}

	public void showRunningEventListViewLayout(
			HomeRunningEventListAdapter adapter, int count) {
		this.updateRunningEventListView(adapter);
		mRunningImageButtonLayout.setVisibility(View.VISIBLE);
		mTxtRunningEventListItemCount.setText(Integer.toString(count));

	}

	private void updateRunningEventListView(HomeRunningEventListAdapter adapter) {
		mHomeRunningEventList.setAdapter(adapter);		
	}	

	private void hideAllListViewExceptThis(	RelativeLayout listView) {
		int visibility = listView.getVisibility();		
		mHomeRunningEventListView.setVisibility(View.GONE);
		mHomeTrackBuddyListView.setVisibility(View.GONE);
		mHomeShareMyLocationtListView.setVisibility(View.GONE);
		mHomePendingEventListView.setVisibility(View.GONE);
		listView.setVisibility(visibility);
	}	
}

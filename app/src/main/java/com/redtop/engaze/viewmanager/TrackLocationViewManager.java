package com.redtop.engaze.viewmanager;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.redtop.engaze.R;
import com.redtop.engaze.TrackLocationActivity;
import com.redtop.engaze.adapter.ContactListAutoCompleteAdapter;
import com.redtop.engaze.common.customeviews.CustomAutoCompleteTextView;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.ContactOrGroup;

import androidx.appcompat.widget.Toolbar;

// this can be a person from contact list or can be a group which will be resolved to actual contact at server
public class TrackLocationViewManager  {	
	public  ViewGroup mFlowContainer;
	public Button mBtnStartTracking;
	public CustomAutoCompleteTextView mAutoCompleteInviteeTextView;
	protected TextView mEventLocationTextView;
	protected TextView mDurationtext;
	private int mEventTypeId;
	private String mHintFriendText;
	private String mHintLocationText;
	private TextView mDurationLabel;
	protected TextView mQuickEventName;
	private TrackLocationActivity activity;


	public TrackLocationViewManager(Context context, int eventTypeId) {

		activity = (TrackLocationActivity)context;
		mEventTypeId = eventTypeId;
		setToolBar();
		initializeElements();
		setUiLabelsBasedOnEventType();
		setClickListener();				
	}

	private void setToolBar(){
		Toolbar toolbar = activity.findViewById(R.id.track_location_event_toolbar);
		if (toolbar != null) {
			activity.setSupportActionBar(toolbar);
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
			activity.getSupportActionBar().setTitle(R.string.title_share_my_location);
			//toolbar.setSubtitle(R.string.title_event);
			toolbar.setNavigationOnClickListener(v -> {
				activity.hideKeyboard(v);
				activity.onBackPressed();
			});
		}
	}

	public CustomAutoCompleteTextView getAutoCompleteInviteeTextView(){
		return mAutoCompleteInviteeTextView;
	}
	private void initializeElements(){
		mQuickEventName = activity.findViewById(R.id.QuickEventName);
		mDurationLabel = activity.findViewById(R.id.Duration);
		mFlowContainer = activity.findViewById(R.id.participant_layout);
		mEventLocationTextView = activity.findViewById(R.id.tracklocation_location);
		mDurationtext = activity.findViewById(R.id.tracklocation_Duration_holder);
		//mBtnStartTracking = activity.findViewById(R.id.btn_tracking_start);
		mAutoCompleteInviteeTextView = activity.findViewById(R.id.searchAutoComplete);
	}

	private void setClickListener(){
		//mBtnStartTracking.setOnClickListener(activity);
		mDurationtext.setOnClickListener(activity);
		mEventLocationTextView.setOnClickListener(activity);		
	}	

	public void bindAutoCompleteTextViewToAdapter(
			ContactListAutoCompleteAdapter mAdapter) {

		mAutoCompleteInviteeTextView.setAdapter(mAdapter);
		mAutoCompleteInviteeTextView.setOnItemClickListener(activity);
		mAutoCompleteInviteeTextView.setOnKeyListener(activity);
	}

	public void clearAutoCompleteInviteeTextView() {
		mAutoCompleteInviteeTextView.setWidth(AppUtility.dpToPx(50, activity));
		mAutoCompleteInviteeTextView.setText("");
		mAutoCompleteInviteeTextView.setHint("");
		mAutoCompleteInviteeTextView.clearListSelection();
	}

	public void createContactLayoutItem(ContactOrGroup cg){
		int childrenCount= mFlowContainer.getChildCount();
		LinearLayout contactLayout = (LinearLayout)activity.getLayoutInflater().inflate(R.layout.template_contact_item_autocomplete, null);

		TextView lblname = (TextView)contactLayout.getChildAt(0);
		lblname.setText(cg.getName());
		lblname.setTag(cg.getName());

		contactLayout.setOnClickListener(v -> {
			mFlowContainer.removeView(v);
			activity.onContactViewClicked(v);
		});
		mFlowContainer.addView(contactLayout, childrenCount-1,new LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT ));
	}

	public View getContactView(int index) {
		
		return 	mFlowContainer.getChildAt(index);
	}

	public void removeContactView(View view, int index) {
		mFlowContainer.removeView(view);
		if(index==0){
			mAutoCompleteInviteeTextView.setHint(mHintFriendText);
			mAutoCompleteInviteeTextView.setWidth(AppUtility.dpToPx(250, activity));
		}

	}

	public void setUiLabelsBasedOnEventType() {
		
		switch(mEventTypeId){
		case 100:
			activity.findViewById(R.id.divider_event_name).setVisibility(View.GONE);
			activity.findViewById(R.id.rl_event_name).setVisibility(View.GONE);
			mDurationLabel.setText(activity.getResources().getString(R.string.share_my_location_text_duration));
			mHintLocationText = activity.getResources().getString(R.string.share_my_location_location_hint);
			mHintFriendText = activity.getResources().getString(R.string.share_my_location_add_friends_hint);
			activity.getSupportActionBar().setTitle(R.string.title_share_my_location);	
			break;
		case 200:
			activity.findViewById(R.id.divider_event_name).setVisibility(View.GONE);
			activity.findViewById(R.id.rl_event_name).setVisibility(View.GONE);
			mDurationLabel.setText(activity.getResources().getString(R.string.track_my_buddy_text_duration));
			mHintLocationText = activity.getResources().getString(R.string.track_my_buddy_location_hint);
			mHintFriendText = activity.getResources().getString(R.string.track_my_buddy_add_friends_hint);
			activity.getSupportActionBar().setTitle(R.string.title_track_buddies);
			break;
		default:
			mDurationLabel.setText(activity.getResources().getString(R.string.meet_now_text_duration));
			mHintLocationText = activity.getResources().getString(R.string.meet_now_location_hint);
			mHintFriendText = activity.getResources().getString(R.string.meet_now_add_friends_hint);
			activity.getSupportActionBar().setTitle(R.string.title_meet_now);	
			break;				
		}
		
		mAutoCompleteInviteeTextView.setHint(Html.fromHtml("<i>" + mHintFriendText+ "</i>"));
		mEventLocationTextView.setHint(Html.fromHtml("<i>" + mHintLocationText + "</i>"));
	}

	public void setDurationText(String durationText) {
		mDurationtext.setText(durationText);

	}

	public TextView getDurationTextView() {	
		return mDurationtext;
	}

	public TextView getLocationTextView() {	
		return mEventLocationTextView;
	}

	public TextView getEventNameView() {		
		return mQuickEventName;
	}
}

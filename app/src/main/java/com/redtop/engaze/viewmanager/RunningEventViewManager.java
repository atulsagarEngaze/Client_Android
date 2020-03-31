package com.redtop.engaze.viewmanager;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.redtop.engaze.R;
import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.adapter.EventDetailsOnMapAdapter;
import com.redtop.engaze.adapter.EventUserLocationAdapter;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// this can be a person from contact list or can be a group which will be resolved to actual contact at server
public class RunningEventViewManager implements OnClickListener {

	protected ListView mUserLocationListItemMenulistView; 
	protected RecyclerView mUserLocationDetailRecyclerView;
	public RelativeLayout rlEventList ;	
	private RunningEventActivity activity;
	protected RecyclerView.LayoutManager mUserLocationDetailLayoutManager;
	protected LayoutManagerType mCurrentLayoutManagerType;
	protected static final String KEY_LAYOUT_MANAGER = "layoutManager";
	protected RecyclerView mEventDetailRecyclerView;
	protected RecyclerView.LayoutManager mEventDetailLayoutManager;
	
	protected ImageButton mNavigationButton;
	protected ImageButton mTrafficButton;
	protected ImageButton mETAButton;
	protected Button mReCenterButton;
	public View mCurrentClickedItem;
	public ProgressBar mProgressBar;

	protected static final int SPAN_COUNT = 3;	
	protected enum LayoutManagerType {
		GRID_LAYOUT_MANAGER,
		LINEAR_LAYOUT_MANAGER
	}
	public RunningEventViewManager(Context context, Bundle savedInstanceState, String toolbarTitle) {

		activity = (RunningEventActivity)context;
		setToolBar(toolbarTitle);
		initializeElements(savedInstanceState);
		setClickListener();				
	}

	private void setToolBar(String toolbarTitle){
		Toolbar toolbar = (Toolbar)activity.findViewById(R.id.running_event_toolbar);
		if (toolbar != null) {
			activity.setSupportActionBar(toolbar);
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
			activity.getSupportActionBar().setTitle(toolbarTitle);			
			toolbar.setNavigationOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.onToolbarBackArrowClicked();					
				}
			});
		}
	}

	protected void initializeElements(Bundle savedInstanceState){
		mUserLocationListItemMenulistView = (ListView)activity.findViewById(R.id.user_menu_options);
		mEventDetailRecyclerView = (RecyclerView)activity.findViewById(R.id.map_user_event_details);		
		mEventDetailLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
		mEventDetailRecyclerView.setLayoutManager(mEventDetailLayoutManager);
		mUserLocationDetailRecyclerView = (RecyclerView)activity.findViewById(R.id.map_user_location_list);	
		mUserLocationDetailLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);

		mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
		if (savedInstanceState != null) {
			// Restore saved layout manager type.
			mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
					.getSerializable(KEY_LAYOUT_MANAGER);
			if(mCurrentLayoutManagerType ==null){
				mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
			}
		}		

		mNavigationButton = (ImageButton) activity.findViewById(R.id.img_navigation);
		mNavigationButton.setVisibility(View.GONE);	
		mReCenterButton = (Button) activity.findViewById(R.id.reCenter);		
		mReCenterButton.setVisibility(View.GONE);
		mETAButton = (ImageButton)activity.findViewById(R.id.img_etaDistance);
		mTrafficButton = (ImageButton)activity.findViewById(R.id.img_traffic);
		setRecyclerViewLayoutManager(mCurrentLayoutManagerType);		
		mProgressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
	}

	protected void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
		int scrollPosition = 0;

		// If a layout manager has already been set, get current scroll position.
		if (mUserLocationDetailRecyclerView.getLayoutManager() != null) {
			scrollPosition = ((LinearLayoutManager) mUserLocationDetailRecyclerView.getLayoutManager())
					.findFirstCompletelyVisibleItemPosition();
		} 
		switch (layoutManagerType) {
		case GRID_LAYOUT_MANAGER:
			mUserLocationDetailLayoutManager = new GridLayoutManager(activity, SPAN_COUNT);
			mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
			break;
		case LINEAR_LAYOUT_MANAGER:
			mUserLocationDetailLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
			mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
			break;
		default:
			mUserLocationDetailLayoutManager = new LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL, false);
			mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
		}

		mUserLocationDetailRecyclerView.setLayoutManager(mUserLocationDetailLayoutManager);
		mUserLocationDetailRecyclerView.scrollToPosition(scrollPosition);
	}	


	protected void setClickListener(){
		((ImageButton)activity.findViewById(R.id.img_traffic)).setOnClickListener(this);
		((ImageButton)activity.findViewById(R.id.img_etaDistance)).setOnClickListener(this);	
		((Button)activity.findViewById(R.id.reCenter)).setOnClickListener(this);
		((ImageButton)activity.findViewById(R.id.img_navigation)).setOnClickListener(this);		
	}	

	@Override
	public void onClick(View v) {
		switch(v.getId()){		
		case R.id.img_traffic:			
			activity.onTrafficButtonClicked();			
			break;
		case R.id.img_etaDistance:
			activity.onEtaDistanceButtonClicked();
			break;
		case R.id.reCenter:
			activity.onReCenterButtonClicked();
			break;
		case R.id.img_navigation:
			activity.onNavigationButtonClicked();		
			break;
		}
	}	

	public void setTrafficButtonOff(){
		mTrafficButton.setImageResource(R.drawable.traffic_off);
	}

	public void setTrafficButtonOn(){
		mTrafficButton.setImageResource(R.drawable.traffic_on);
	}

	public void setEtaButtonOff(){
		mETAButton.setImageResource(R.drawable.eta_off);
	}

	public void setEtaButtonOn(){
		mETAButton.setImageResource(R.drawable.eta_on);
	}

	public void clickRecenterButton() {
		mReCenterButton.performClick();		
	}

	public void showReCenterButton(){
		mReCenterButton.setVisibility(View.VISIBLE);
	}

	public boolean isRecenterButtonHidden(){
		return mReCenterButton.getVisibility()==View.GONE;
	}

	public void hideReCenterButton(){
		mReCenterButton.setVisibility(View.GONE);
	}

	public void showGoogleNavigatioButton(){
		mNavigationButton.setVisibility(View.VISIBLE);
	}

	public void hideGoogleNavigatioButton(){
		mNavigationButton.setVisibility(View.GONE);
	}

	public void bindUserLocationDetailRecyclerView(EventUserLocationAdapter adapter){
		mUserLocationDetailRecyclerView.setAdapter(adapter);
	}

	public void bindEventDetailRecyclerViewBind(EventDetailsOnMapAdapter adapter){
		mEventDetailRecyclerView.setAdapter(adapter);
	}
}

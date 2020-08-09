package com.redtop.engaze.viewmanager;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.redtop.engaze.PickLocationActivity;
import com.redtop.engaze.R;
import com.redtop.engaze.adapter.NewSuggestedLocationAdapter;
import com.redtop.engaze.common.utility.AppUtility;

// this can be a person from contact list or can be a group which will be resolved to actual contact at server
public class PickLocationViewManager  extends MapCameraMovementHandleViewManager {

	private PickLocationActivity activity;
	private ImageButton mBtnImgeAddLocation;
	private RelativeLayout mselectedLocationLayout;	
	private ImageView mBackButton;
	public TextView mSelectedLocationNameText;
	public TextView mSelectedLocationAddressText;

	public static final int PICK_LOCATION_ACTIVITY_LOCATION_TEXT_LENGTH = 36;

	public PickLocationViewManager(Context context) {
		super(context);
		activity = (PickLocationActivity)context;		
		initializeElements();
		setClickListener();
	}	

	@Override
	protected void initializeElements(){

		mBackButton = activity.findViewById(R.id.img_pick_location_back);
		mSelectedLocationAddressText = activity.findViewById(R.id.txt_selected_location_address);
		mSelectedLocationNameText= activity.findViewById(R.id.txt_selected_location_name);
		mselectedLocationLayout = activity.findViewById(R.id.rl_selected_location);
		mselectedLocationLayout.setVisibility(View.GONE);
		mSearchLocationTextLength = PICK_LOCATION_ACTIVITY_LOCATION_TEXT_LENGTH;
		
		mBtnImgeAddLocation = activity.findViewById(R.id.img_add_location);
		mBtnImgeAddLocation.setVisibility(View.GONE);
		activity.mSuggestedLocationAdapter = new NewSuggestedLocationAdapter(activity, R.layout.item_suggested_location_list, activity.mAutoCompletePlaces);
		mPin= activity.findViewById(R.id.img_center_pin);
		mPin.setVisibility(View.GONE);
//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//			final Drawable originalDrawable = mIconSearchClear.getBackground();
//			final Drawable wrappedDrawable = DrawableCompat.wrap(originalDrawable);
//			DrawableCompat.setTint(wrappedDrawable, activity.getResources().getColor(R.color.icon) );
//			mIconSearchClear.setBackground(wrappedDrawable);
//			rlSearchView = (RelativeLayout)activity.findViewById(R.id.ll_pick_location);
//			rlSearchView.setBackground(activity.getResources().getDrawable(R.drawable.my_location_textbox));
//		}
		super.initializeElements();
	}
	
	@Override
	protected void setClickListener(){
		mBackButton.setOnClickListener(this);		
		mBtnImgeAddLocation.setOnClickListener(this);
		super.setClickListener();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.icon_search_clear:			
			mBtnImgeAddLocation.setVisibility(View.GONE);
			mselectedLocationLayout.setVisibility(View.GONE);
			mSelectedLocationNameText.setText("");
			mSelectedLocationAddressText.setText("");
			mEventLocation.setText("");
			mEventLocation.setHint( activity.getResources().getString(R.string.location_search_bar_hint));			
			break;

		case R.id.img_add_location:			
			activity.onLocationSelection();
			break;
		case R.id.img_pick_location_back:			
			activity.moveBack();
			break;	
		}
		super.onClick(v);
	}
	
	public void showLocationBar(String name, String address){
		mBtnImgeAddLocation.setVisibility(View.VISIBLE);
		mBtnImgeAddLocation.bringToFront();
		mselectedLocationLayout.setVisibility(View.VISIBLE);
		mSelectedLocationNameText.setText(AppUtility.createTextForDisplay(name,mSearchLocationTextLength + 2));
		mSelectedLocationAddressText.setText(address);		
	}

	@Override
	public void setLocationNameAndAddress(String name, String address){
		mSelectedLocationNameText.setText(AppUtility.createTextForDisplay(name, mSearchLocationTextLength));
		mSelectedLocationAddressText.setText(address);
		mselectedLocationLayout.setVisibility(View.VISIBLE);
		mBtnImgeAddLocation.setVisibility(View.VISIBLE);
		mBtnImgeAddLocation.bringToFront();	
	}
}

package com.redtop.engaze.viewmanager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.redtop.engaze.BaseActivity1;
import com.redtop.engaze.LocationActivity;
import com.redtop.engaze.R;
import com.redtop.engaze.adapter.CachedLocationAdapter;
import com.redtop.engaze.adapter.NewSuggestedLocationAdapter;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.MarkerHelper;
import com.redtop.engaze.domain.AutoCompletePlace;
import com.redtop.engaze.domain.EventPlace;

public class LocationViewManager implements  OnItemClickListener, OnClickListener, TextWatcher, View.OnFocusChangeListener, View.OnTouchListener
{
	protected int mSearchLocationTextLength;
	private LocationActivity activity;
	public TextView mEventLocation;
	public EditText mTxtSearchLocation;
	public ListView mLocationListView;
	public ListView mFavouriteLocationListView;	
	public RelativeLayout mLocationSearchResultView;
	public RelativeLayout mLocationSearchView;
	public RelativeLayout mMapView;
	public ImageButton myLocationButton;	
	public ImageView mPin;
	public RelativeLayout rlSearchView;
	public ImageView mIconSearchClear ;
	public ImageView mTxtSelectLocationBack;
	public int mFontSize;
	public LocationViewManager(Context context){
		activity = (LocationActivity)context;		
	}	

	protected void initializeElements(){
		myLocationButton = (ImageButton)activity.findViewById(R.id.img_my_location);
		mEventLocation = (TextView)activity.findViewById(R.id.txt_location);
		mLocationSearchView = (RelativeLayout)activity.findViewById(R.id.rl_location_search_view);
		mMapView = (RelativeLayout)activity.findViewById(R.id.rl_map_view);
		mLocationSearchResultView = (RelativeLayout)activity.findViewById(R.id.rl_location_search_result);
		mLocationListView = (ListView)activity.findViewById(R.id.location_list);
		mFavouriteLocationListView = (ListView)activity.findViewById(R.id.favourite_location_list);
		mTxtSelectLocationBack = (ImageView)activity.findViewById(R.id.img_select_location_back); 
		mTxtSearchLocation = (EditText)activity.findViewById(R.id.txt_search_location);
		mFontSize = (int)activity.getResources().getDimension(R.dimen.small_text_size);	
		mIconSearchClear = (ImageView)activity.findViewById(R.id.icon_search_clear);
		mIconSearchClear.setVisibility(View.GONE);
		mPin.setBackground(MarkerHelper.CreateLocatonPinDrawable(activity));
	}

	protected void setClickListener(){
		myLocationButton.setOnClickListener(this);
		mTxtSelectLocationBack.setOnClickListener(this);
		mIconSearchClear.setOnClickListener(this);
		mLocationListView.setOnItemClickListener(this);
		mTxtSearchLocation.addTextChangedListener(this);
		mTxtSearchLocation.setOnFocusChangeListener(this);
		mTxtSearchLocation.setOnTouchListener(this);
		mEventLocation.setOnClickListener(this);
		mFavouriteLocationListView.setOnItemClickListener(this);
	}

	public void updateCacheLocationListAdapter(CachedLocationAdapter adapter){
		adapter.notifyDataSetChanged();
	}

	public void setCacheLocationListAdapter(CachedLocationAdapter adapter){
		mFavouriteLocationListView.setAdapter(adapter);
	}

	public void setLocationViewAdapter(NewSuggestedLocationAdapter adapter){
		mLocationListView.setAdapter(adapter);
	}

	public void updateLocationListView(NewSuggestedLocationAdapter adapter){
		adapter.notifyDataSetChanged();
	}	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AutoCompletePlace item = null;
		switch(parent.getId()){
		case R.id.location_list:
			item = (AutoCompletePlace) parent.getItemAtPosition(position);
			mEventLocation.setFocusable(false);
			mEventLocation.clearFocus();			
			hideSearchView();		
			activity.onListItemClicked(item);			
			break;
		case R.id.favourite_location_list:
			EventPlace ep = (EventPlace) parent.getItemAtPosition(position);
			mEventLocation.setFocusable(false);
			mEventLocation.clearFocus();			
			hideSearchView();		
			activity.onFavouriteListItemClicked(ep);			
			break;
		}		
	}		

	public void showPin(){
		mPin.setVisibility(View.VISIBLE);
	}

	public void hidePin(){
		mPin.setVisibility(View.GONE);		
	}

	public void setGpsOnPinOnMyLocationDrawable(){
		myLocationButton.setImageResource(R.drawable.pointer_on_gps_on);
	}

	public void setGpsOnDrawable(){
		myLocationButton.setImageResource(R.drawable.gps_on);
	}

	public void setGpsOffDrawable(){
		myLocationButton.setImageResource(R.drawable.gps_off);
	}

	public void setLocationText(String text){
		mEventLocation.setText( AppUtility.createTextForDisplay(text, mSearchLocationTextLength));
	}	

	//overridden in pick location child view manager
	public void setLocationNameAndAddress(String name, String address) {		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){

		case R.id.img_select_location_back:			
			mEventLocation.setFocusable(false);
			mEventLocation.clearFocus();			
			hideSearchView();
			break;
		case R.id.img_my_location:			
			activity.checkGpsAndBringPinToMyLocation();			
			break;	
		case R.id.icon_search_clear:			
			mTxtSearchLocation.setText("");
			mTxtSearchLocation.setHint( activity.getResources().getString(R.string.location_search_bar_hint));			
			break;
		case R.id.txt_location:
			showSearchView();			
			break;
		}		
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTextChanged(CharSequence query, int start, int before, int count) {		
		activity.getAutoCompletePlacePridictions(query);
		if(mTxtSearchLocation.getText().toString().equals("")){
			mIconSearchClear.setVisibility(View.GONE);
			if(mFavouriteLocationListView.getAdapter()!=null && mFavouriteLocationListView.getAdapter().getCount() !=0){
				mFavouriteLocationListView.setVisibility(View.VISIBLE);	
			}
		}
		else
		{
			mIconSearchClear.setVisibility(View.VISIBLE);
			mFavouriteLocationListView.setVisibility(View.GONE);
			mLocationListView.setVisibility(View.VISIBLE);
		}		
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus) {
			((BaseActivity1)activity).hideKeyboard(v);
		}		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mTxtSearchLocation.setFocusableInTouchMode(true);		
		return false;
	}

	public void hideSearchView(){
		mTxtSearchLocation.setText("");
		mMapView.setVisibility(View.VISIBLE);
		mLocationSearchView.setVisibility(View.GONE);
		mLocationSearchResultView.setVisibility(View.GONE);
	}

	public void showSearchView(){
		mLocationListView.setVisibility(View.GONE);	
		mMapView.setVisibility(View.GONE);
		mLocationSearchView.setVisibility(View.VISIBLE);
		if(mFavouriteLocationListView.getAdapter()!=null && mFavouriteLocationListView.getAdapter().getCount() !=0){
			mLocationSearchResultView.setVisibility(View.VISIBLE);
			mLocationSearchResultView.setAlpha(0.0f);
			mLocationSearchResultView.animate()
			.translationY(0)
			.alpha(1.0f)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					mLocationSearchResultView.setVisibility(View.VISIBLE);
				}
			});
		}
		else{
			mLocationSearchResultView.setVisibility(View.VISIBLE);
			mFavouriteLocationListView.setVisibility(View.GONE);
		}
		if(mTxtSearchLocation.requestFocus()) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(mTxtSearchLocation, InputMethodManager.SHOW_IMPLICIT);

		}
	}
}
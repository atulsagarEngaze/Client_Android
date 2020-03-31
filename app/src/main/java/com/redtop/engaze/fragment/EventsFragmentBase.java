package com.redtop.engaze.fragment;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;

import com.redtop.engaze.adapter.EventReCycleViewAdapter;
import com.redtop.engaze.entity.EventDetail;

public class EventsFragmentBase extends Fragment implements OnItemClickListener{
	protected static final String TAG = EventsFragmentBase.class.getName();
	protected static final int SPAN_COUNT = 2;
	protected static final String KEY_LAYOUT_MANAGER = "layoutManager";
	protected Context mContext;
	protected LinearLayout mLl_noevent;
	protected enum LayoutManagerType {
		GRID_LAYOUT_MANAGER,
		LINEAR_LAYOUT_MANAGER
	}	
	protected RecyclerView mRecyclerView;
	protected RecyclerView.LayoutManager mLayoutManager;
	protected LayoutManagerType mCurrentLayoutManagerType;
	public List<EventDetail> mEventDetailList;
	public EventReCycleViewAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);							
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {	
		//final FragmentActivity c = getActivity();		
		return super.onCreateView(inflater, container, savedInstanceState);					
	}

	protected void createLayout(Bundle savedInstanceState){
		mLayoutManager = new LinearLayoutManager(getActivity());
		mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
		if (savedInstanceState != null) {
			// Restore saved layout manager type.
			mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
					.getSerializable(KEY_LAYOUT_MANAGER);
		}

		setRecyclerViewLayoutManager(mCurrentLayoutManagerType);
		mAdapter = new EventReCycleViewAdapter(mEventDetailList,mContext);
		// Set CustomAdapter as the adapter for RecyclerView.
		mRecyclerView.setAdapter(mAdapter);
		enableDisableNoEventLayout();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save currently selected layout manager.
		savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
		super.onSaveInstanceState(savedInstanceState);
	}

	private void enableDisableNoEventLayout(){
		if(mEventDetailList!=null && mEventDetailList.size()>0){

			mLl_noevent.setVisibility(View.GONE);
			mRecyclerView.setVisibility(View.VISIBLE);
		}
		else {
			mLl_noevent.setVisibility(View.VISIBLE);
			mRecyclerView.setVisibility(View.GONE);
		}
	}

	public void updateEventFragment(List<EventDetail> eventDetailList){	
		mEventDetailList = eventDetailList;
		mAdapter.mEventList = eventDetailList;
		mAdapter.notifyDataSetChanged();
		enableDisableNoEventLayout();
	}	

	public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
		int scrollPosition = 0;

		// If a layout manager has already been set, get current scroll position.
		if (mRecyclerView.getLayoutManager() != null) {
			scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
					.findFirstCompletelyVisibleItemPosition();
		} 
		switch (layoutManagerType) {
		case GRID_LAYOUT_MANAGER:
			mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
			mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
			break;
		case LINEAR_LAYOUT_MANAGER:
			mLayoutManager = new LinearLayoutManager(getActivity());
			mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
			break;
		default:
			mLayoutManager = new LinearLayoutManager(getActivity());
			mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
		}

		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.scrollToPosition(scrollPosition);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}	 
}
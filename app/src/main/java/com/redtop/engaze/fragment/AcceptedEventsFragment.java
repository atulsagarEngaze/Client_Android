package com.redtop.engaze.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.redtop.engaze.EventsActivity;
import com.redtop.engaze.R;
import com.redtop.engaze.utils.Constants.AcceptanceStatus;

public class AcceptedEventsFragment extends EventsFragmentBase implements OnItemClickListener{
	private static final String TAG = AcceptedEventsFragment.class.getName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);				
		mContext = getActivity();
		mEventDetailList = ((EventsActivity)mContext).mEventDetailHashmap.get(AcceptanceStatus.ACCEPTED);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_accepted_events, container, false);
		rootView.setTag(TAG);
		mLl_noevent = (LinearLayout)rootView.findViewById(R.id.rl_accepted_help_text);		
		((EventsActivity)mContext).aef = this;
		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.accepted_event_recycle_list);
		createLayout(savedInstanceState);				
		return rootView;			
	}	
}
	

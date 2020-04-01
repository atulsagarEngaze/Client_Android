package com.redtop.engaze.adapter;


import android.util.Log;

import com.redtop.engaze.fragment.AcceptedEventsFragment;
import com.redtop.engaze.fragment.DeclinedEventsFragment;
import com.redtop.engaze.fragment.DraftEventsFragment;
import com.redtop.engaze.fragment.PendingEventsFragment;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class EventsPagerAdapter extends FragmentStatePagerAdapter {

	CharSequence Titles[]; // This will Store the Titles of the Tabs which are
							// Going to be passed when ViewPagerAdapter is
							// created
	int NumbOfTabs; // Store the number of tabs, this will also be passed when
					// the ViewPagerAdapter is created
	private static final String TAG = EventsPagerAdapter.class.getName();
	// Build a Constructor and assign the passed Values to appropriate values in
	// the class
	public EventsPagerAdapter(FragmentManager fm, CharSequence mTitles[],
							  int mNumbOfTabsumb) {
		super(fm);

		this.Titles = mTitles;
		this.NumbOfTabs = mNumbOfTabsumb;

	}

	// This method return the fragment for the every position in the View Pager
	@Override
	public Fragment getItem(int position) {
		Fragment returnTab = null;
		Log.d(TAG,"Fragment position "+position);
		switch(position){
		case 0:
			AcceptedEventsFragment allEventsTab = new AcceptedEventsFragment();
			returnTab = allEventsTab;
			break;
//		case 1:
//			DraftEventsFragment draftEventsTab = new DraftEventsFragment();
//			returnTab = draftEventsTab;
//			break;
		case 1:
			PendingEventsFragment pendingEventsTab = new PendingEventsFragment();
			returnTab = pendingEventsTab;
			break;
		case 2:
			DeclinedEventsFragment declinedEventsTab = new DeclinedEventsFragment();
			returnTab = declinedEventsTab;
			break;
		}
		return returnTab;
	}

	// This method return the titles for the Tabs in the Tab Strip

	@Override
	public CharSequence getPageTitle(int position) {
		return Titles[position];
	}

	// This method return the Number of tabs for the tabs Strip

	@Override
	public int getCount() {
		return NumbOfTabs;
	}
}

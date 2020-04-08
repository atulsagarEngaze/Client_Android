package com.redtop.engaze.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.redtop.engaze.R;
import com.redtop.engaze.adapter.NavDrawerAdapter;
import com.redtop.engaze.domain.NavDrawerItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NavDrawerFragment extends Fragment {
	private static String TAG = NavDrawerFragment.class.getSimpleName();

	private RecyclerView recyclerView;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private NavDrawerAdapter adapter;
	private View containerView;
	private static String[] titles = null;
	private static int[] titlesIcon  ={R.drawable.ic_home_24dp,R.drawable.ic_event_24dp,R.drawable.ic_person_add_24dp,
		//R.drawable.ic_group_add_black_36dp,R.drawable.ic_group_black_36dp,
		R.drawable.ic_person_24dp,R.drawable.ic_settings_24dp,R.drawable.ic_feedback_black_24dp, R.drawable.ic_info_outline_24dp};
	//private static int[] titlesIconFont  ={R.string.fa_home,R.string.fa_user,R.string.fa_group,R.string.fa_adjust,R.string.fa_adjust,R.string.fa_gear,R.string.fa_info};
	private FragmentDrawerListener drawerListener;

	public NavDrawerFragment() {

	}

	public void setDrawerListener(FragmentDrawerListener listener) {
		this.drawerListener = listener;
	}

	public static List<NavDrawerItem> getData() {
		List<NavDrawerItem> data = new ArrayList<NavDrawerItem>();


		// preparing navigation drawer items
		for (int i = 0; i < titles.length; i++) {
			NavDrawerItem navItem = new NavDrawerItem();
			navItem.setTitle(titles[i]);
			navItem.setTitleIcon(titlesIcon[i]);
			//navItem.setTitleIconFont(titlesIconFont[i]);
			data.add(navItem);
		}
		return data;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// drawer labels
		titles = getActivity().getResources().getStringArray(R.array.nav_drawer_labels);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflating view layout
		View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
		recyclerView = (RecyclerView) layout.findViewById(R.id.drawerList);

		adapter = new NavDrawerAdapter(getActivity(), getData());
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
			@Override
			public void onClick(View view, int position) {
				drawerListener.onDrawerItemSelected(view, position);
				mDrawerLayout.closeDrawer(containerView);
			}

			@Override
			public void onLongClick(View view, int position) {

			}
		}));

		return layout;
	}


	public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
		containerView = getActivity().findViewById(fragmentId);
		mDrawerLayout = drawerLayout;
		mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActivity().invalidateOptionsMenu();
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				getActivity().invalidateOptionsMenu();
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				super.onDrawerSlide(drawerView, slideOffset);
				if(toolbar!=null)
				{
					toolbar.setAlpha(1 - slideOffset / 2);
				}
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mDrawerToggle.syncState();
			}
		});

	}

	public static interface ClickListener {
		public void onClick(View view, int position);

		public void onLongClick(View view, int position);
	}

	static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

		private GestureDetector gestureDetector;
		private ClickListener clickListener;

		public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
			this.clickListener = clickListener;
			gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					return true;
				}

				@Override
				public void onLongPress(MotionEvent e) {
					View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
					if (child != null && clickListener != null) {
						clickListener.onLongClick(child, recyclerView.getChildPosition(child));
					}
				}
			});
		}

		@Override
		public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

			View child = rv.findChildViewUnder(e.getX(), e.getY());
			if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
				clickListener.onClick(child, rv.getChildPosition(child));
			}
			return false;
		}

		@Override
		public void onTouchEvent(RecyclerView rv, MotionEvent e) {
		}

		public void onRequestDisallowInterceptTouchEvent(boolean arg0) {
			// TODO Auto-generated method stub

		}
		
	}

	public interface FragmentDrawerListener {
		public void onDrawerItemSelected(View view, int position);
	}
}

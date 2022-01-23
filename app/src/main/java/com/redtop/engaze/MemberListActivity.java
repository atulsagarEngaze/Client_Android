package com.redtop.engaze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.redtop.engaze.adapter.MemberAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;
import com.redtop.engaze.service.ContactListRefreshIntentService;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MemberListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, OnItemClickListener {

    ListView listView;
    public String[] friends;
    //public ArrayList<ContactOrGroup>mMembers;
    public ArrayList<ContactOrGroup> mAllMembers;
    public MemberAdapter mAdapter;
    public BroadcastReceiver mMemberListBroadcastReceiver;
    public static Boolean isFirstTime = false;

    public String[] images;
    private LinearLayout mLl_nocontacts;
    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_friend_list);
        mLl_nocontacts = findViewById(R.id.ll_drawer_contacts_help_text);
        listView = findViewById(R.id.friend_list);
        Toolbar toolbar = findViewById(R.id.friend_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitleTextAppearance(this, R.style.toolbarTextFontFamilyStyle);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            getSupportActionBar().setTitle(R.string.title_friend_list);
            //toolbar.setSubtitle(R.string.title_event);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        mAllMembers = new ArrayList<>();
        mAllMembers.addAll( AppContext.context.sortedContacts);
        if (mAllMembers.size() == 0) {
            //swipeRefreshLayout.setRefreshing(true);
            startContactRefreshService();

        } else {
            loadFriendList();
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_friendlist);
        swipeRefreshLayout.setOnRefreshListener(this);

    }




    private void loadFriendList() {
        if (mAllMembers == null || mAllMembers.size() == 0) {
            mAllMembers = new ArrayList<>();
            inviteFriend();
            mLl_nocontacts.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);

        } else {
            mLl_nocontacts.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            mAdapter = new MemberAdapter(this,
                    R.layout.member_list_item, mAllMembers);
            listView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();

            listView.setOnItemClickListener((adapter, v, position, arg3) -> {
                final ContactOrGroup value = (ContactOrGroup) adapter.getItemAtPosition(position);
                if (value.getUserId() != null) {

                    AlertDialog.Builder adb = null;
                    adb = new AlertDialog.Builder(mContext);

                    adb.setTitle("Meet " + value.getName());
                    adb.setMessage(getResources().getString(R.string.message_meetnow_memberlistactivity));
                    adb.setIcon(android.R.drawable.ic_dialog_alert);

                    adb.setPositiveButton("OK", (dialog, which) -> {
                        Intent i = new Intent(mContext, TrackLocationActivity.class);
                        i.putExtra("EventTypeId", 6);
                        i.putExtra("meetNowUserID", value.getUserId());
                        startActivity(i);
                    });

                    adb.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                    adb.show();
                }
            });
        }
    }

    @Override
    public void contact_list_refresh_process_complete() {

        String contactsRefreshStatus = PreffManager.getPref(Constants.LAST_CONTACT_LIST_REFRESH_STATUS);
        String registeredContactsRefreshStatus = PreffManager.getPref(Constants.LAST_REGISTERED_CONTACT_LIST_REFRESH_STATUS);

        if (contactsRefreshStatus.equals(Constants.FAILED) || registeredContactsRefreshStatus.equals(Constants.FAILED)) {
            Toast.makeText(AppContext.context.currentActivity, AppContext.context.getResources().getString(R.string.message_contacts_errorRetrieveData), Toast.LENGTH_SHORT).show();
        }
        if (contactsRefreshStatus.equals(Constants.SUCCESS)) {
            mAllMembers = AppContext.context.sortedContacts;
            loadFriendList();
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_member, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search_member);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        //*** setOnQueryTextFocusChangeListener ***
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {

        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                mAdapter.filter(searchQuery.trim());
                listView.invalidate();
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            //		case R.id.action_add_member:
            //			inviteFriend();
            //			break;
            case R.id.action_refresh_contactlist:
                swipeRefreshLayout.setRefreshing(true);
                startContactRefreshService();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        // TODO Auto-generated method stub
        swipeRefreshLayout.setRefreshing(true);
        startContactRefreshService();
    }

    private void startContactRefreshService() {
        ContactListRefreshIntentService.start(mContext,false);
    }
}

package com.redtop.engaze;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.redtop.engaze.Interface.IActionHandler;
import com.redtop.engaze.adapter.ContactListAutoCompleteAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.domain.NameImageItem;
import com.redtop.engaze.viewmanager.TrackLocationViewManager;
import com.redtop.engaze.webservice.Routes;

public class TrackLocationActivity extends BaseEventActivity implements OnItemClickListener, OnClickListener, OnKeyListener, IActionHandler {

    static final int PLACE_PICKER_REQUEST = 1;
    ArrayList<ContactOrGroup> mMembers = new ArrayList<ContactOrGroup>();
    ContactListAutoCompleteAdapter mAdapter;
    Hashtable<String, ContactOrGroup> mAddedMembers;
    private TrackLocationViewManager viewManager = null;
    private int mEventTypeId;
    private ImageView imgView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_location_event);
        TAG = TrackLocationActivity.class.getName();
        mContext = this;
        mEventTypeId = this.getIntent().getIntExtra("EventTypeId", mEventTypeId);
        viewManager = new TrackLocationViewManager(mContext, mEventTypeId);
        mDurationTextView = viewManager.getDurationTextView();//have to do this because code of populating this is written in eventbase activity
        mQuickEventName = viewManager.getEventNameView();
        mEventLocationTextView = viewManager.getLocationTextView();
        populateControlsAndDeafultEvenData();

        imgView = (ImageView) findViewById(R.id.icon_track_location_clear);
        imgView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEventLocationTextView.setText("");
                mDestinationPlace = null;
            }
        });

    }


    protected void populateControlsAndDeafultEvenData() {

        initializeBasedOnEventType();
        mEventTypeItem = new NameImageItem(R.drawable.ic_event_black_24dp, "General", mEventTypeId);
        mAddedMembers = new Hashtable<String, ContactOrGroup>();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        createOrUpdateEvent.Duration = new Duration(Integer.parseInt(sharedPrefs.getString("defaultDuration", getResources().getString(R.string.event_default_duration))), sharedPrefs.getString("defaultPeriod", getResources().getString(R.string.event_default_period)), Boolean.parseBoolean(sharedPrefs.getString("trackingEnabled", getResources().getString(R.string.event_tracking_default_enabled))));
        SetDurationText();
        if (this.getIntent().getParcelableExtra("DestinatonLocation") != null) {
            createOrUpdateEvent.Destination = (EventPlace) this.getIntent().getParcelableExtra("DestinatonLocation");
            mEventLocationTextView.setText(AppUtility.createTextForDisplay(mDestinationPlace.getName(), Constants.EDIT_ACTIVITY_LOCATION_TEXT_LENGTH));
        }
        if (!accessingContactsFirstTime()) {
            //mMembers = ContactAndGroupListManager.getAllRegisteredContacts(mContext);
            mMembers = ContactAndGroupListManager.getAllContacts();
            if (mMembers != null) {
                mAdapter = new ContactListAutoCompleteAdapter(mContext, R.layout.item_contact_group_list, mMembers);
                viewManager.bindAutoCompleteTextViewToAdapter(mAdapter);
            }
        }

        //if activity is loaded from members list activity then add the selected contact
        addIfAnyContactIsSeletedFromMemberListActivity();
    }

    private void addIfAnyContactIsSeletedFromMemberListActivity() {
        String memberId = this.getIntent().getStringExtra("meetNowUserID");
        if (memberId != null) {
            ContactOrGroup contact = ContactAndGroupListManager.getContact(memberId);
            mAddedMembers.put(contact.getName(), contact);
            viewManager.createContactLayoutItem(contact);
            viewManager.clearAutoCompleteInviteeTextView();
        }
    }

    @Override
    protected void registeredMemberListCached() {
        //mMembers = ContactAndGroupListManager.getAllRegisteredContacts(mContext);
        mMembers = ContactAndGroupListManager.getAllContacts();
        if (mMembers != null) {
            mAdapter = new ContactListAutoCompleteAdapter(mContext, R.layout.item_contact_group_list, mMembers);
            viewManager.bindAutoCompleteTextViewToAdapter(mAdapter);
        }
    }

    @Override
    protected void memberListRefreshed_success(Hashtable<String, ContactOrGroup> memberList) {
        mMembers = (ArrayList<ContactOrGroup>) memberList.values();
        if (mMembers != null) {
            mAdapter = new ContactListAutoCompleteAdapter(mContext, R.layout.item_contact_group_list, mMembers);
            viewManager.bindAutoCompleteTextViewToAdapter(mAdapter);
        }
    }

    @Override
    protected void memberListRefreshed_fail() {

    }

    private void initializeBasedOnEventType() {
        switch (createOrUpdateEvent.EventType) {
            case SHAREMYLOACTION:
                mCreateUpdateSuccessfulMessage = getResources().getString(R.string.sharemylocation_event_create_successful);
                createOrUpdateEvent.Name = "S_" + AppContext.context.loginName + "_";
                createOrUpdateEvent.Description = "ShareMyLocationEvent";
                createOrUpdateEvent.IsQuickEvent = false;
                break;
            case TRACKBUDDY:
                mCreateUpdateSuccessfulMessage = getResources().getString(R.string.track_my_buddy_event_create_successful);
                createOrUpdateEvent.Name = "T_L_" + AppContext.context.loginName + "_B";
                createOrUpdateEvent.Description = "TrackBuddy";
                createOrUpdateEvent.IsQuickEvent = false;
                break;
            default:
                Calendar calendar_start = Calendar.getInstance();
                mCreateUpdateSuccessfulMessage = getResources().getString(R.string.meet_now_event_create_successful);
                createOrUpdateEvent.Name = "Meet " + AppContext.context.loginName + " @" + DateUtil.getTime(calendar_start);
                mQuickEventName.setText(createOrUpdateEvent.Name);
                createOrUpdateEvent.Description = "QuickEvent";
                createOrUpdateEvent.IsQuickEvent = true;
                break;
        }
    }

    @Override
    public void actionFailed(String msg, Action action) {
        if (action == Action.SAVEEVENT) {
            if (mEventTypeId == 100) {
                action = Action.SAVEEVENTSHAREMYLOCATION;
            } else if (mEventTypeId == 200) {
                action = Action.SAVEEVENTTRACKBUDDY;
            }
        }
        AppContext.actionHandler.actionFailed(msg, action);
    }

    public void SaveEvent() {

        populateEventData();

        if (!validateInputData()) {
            return;
        }
        saveEvent(true);
    }

    private Boolean validateInputData() {

        try {
            if (mEventJobj.getJSONArray("UserList").length() == 0) {
                setAlertDialog("Oops no invitee has been selected !", "Kindly select atleast one invitee");
                mAlertDialog.show();
                return false;
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void populateEventData() {
        Calendar calendar_start = Calendar.getInstance();
        if (createOrUpdateEvent.IsQuickEvent) {
            createOrUpdateEvent.Name = mQuickEventName.getText().toString();
        }
        startDate = calendar_start.getTime();
        super.populateEventData();
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        ContactOrGroup contact = (ContactOrGroup) adapter.getItemAtPosition(position);
        //v.setSelected(true);

        if (mAddedMembers.size() < 10) {

            if (mAddedMembers.containsKey(contact.getName())) {
                Toast.makeText(mContext,
                        "User is already added", Toast.LENGTH_SHORT).show();
            } else {
                mAddedMembers.put(contact.getName(), contact);
                viewManager.createContactLayoutItem(contact);
                viewManager.clearAutoCompleteInviteeTextView();
            }
        } else {
            Toast.makeText(mContext,
                    "You have reached maximum limit of participants!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.tracklocation_location:

                intent = new Intent(TrackLocationActivity.this, PickLocationActivity.class);
                if (mDestinationPlace != null) {
                    intent.putExtra("DestinatonLocation", (Parcelable) mDestinationPlace);
                }
                startActivityForResult(intent, LOCATION_REQUEST_CODE);
                break;

            case R.id.tracklocation_Duration_holder:
                intent = new Intent(TrackLocationActivity.this, DurationOffset.class);
                intent.putExtra("com.redtop.engaze.entity.Duration", createOrUpdateEvent.Duration);
                startActivityForResult(intent, DURATION_REQUEST_CODE);
                break;
            case R.id.btn_tracking_start:
                mContactsAndgroups = new ArrayList<ContactOrGroup>(mAddedMembers.values());
                SaveEvent();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track_now, menu);
        // Get the root inflator.
        LayoutInflater baseInflater = (LayoutInflater) getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate your custom view.
        View myCustomView = baseInflater.inflate(R.layout.layout_start_menu_item, null);
        MenuItem item = menu.findItem(R.id.track_action_start).setActionView(myCustomView);
        item.getActionView().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mContactsAndgroups = new ArrayList<ContactOrGroup>(mAddedMembers.values());
                SaveEvent();
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.track_action_start:
                mContactsAndgroups = new ArrayList<ContactOrGroup>(mAddedMembers.values());
                SaveEvent();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (viewManager.getAutoCompleteInviteeTextView().getText().toString().length() <= 0) {
                if (mAddedMembers.size() > 0) {
                    int index = mAddedMembers.size() - 1;
                    View view = viewManager.getContactView(index);
                    String key = (String) ((LinearLayout) view).getChildAt(0).getTag();
                    mAddedMembers.remove(key);
                    viewManager.removeContactView(view, index);
                }
            }
        }
        return false;
    }

    public void onContactViewClicked(View v) {
        mAddedMembers.remove((String) ((LinearLayout) v).getChildAt(0).getTag());
    }

    @Override
    public void actionCancelled(Action action) {
        AppContext.actionHandler.actionCancelled(action);
    }

    @Override
    public void actionComplete(Action action) {
        AppContext.actionHandler.actionComplete(action);
    }
}

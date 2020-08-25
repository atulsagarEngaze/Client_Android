package com.redtop.engaze;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.redtop.engaze.Interface.IActionHandler;
import com.redtop.engaze.adapter.ContactListAutoCompleteAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.constant.IntentConstants;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.domain.NameImageItem;
import com.redtop.engaze.fragment.DurationOffsetFragment;
import com.redtop.engaze.viewmanager.TrackLocationViewManager;

public class TrackLocationActivity extends BaseEventActivity implements OnItemClickListener, OnClickListener, OnKeyListener, IActionHandler {

    static final int PLACE_PICKER_REQUEST = 1;
    private TrackLocationViewManager viewManager = null;

    private ImageView imgView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_location_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.track_location_event_toolbar);
        toolbar.setTitleTextAppearance(this, R.style.toolbarTextFontFamilyStyleNoElevation);
        Window window = getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setDimAmount(0.05f);
        this.setFinishOnTouchOutside(false);
        TAG = TrackLocationActivity.class.getName();
        mContext = this;
        mEventTypeId = this.getIntent().getIntExtra("EventTypeId", EventType.TRACKBUDDY.GetEventTypeId());
        initializeEventWithDefaultValues();
        viewManager = new TrackLocationViewManager(mContext, mEventTypeId);
        mDurationTextView = viewManager.getDurationTextView();//have to do this because code of populating this is written in eventbase activity
        mQuickEventNameView = viewManager.getEventNameView();
        mEventLocationTextView = viewManager.getLocationTextView();
        populateControlsAndDefaultEvenData();

        imgView = (ImageView) findViewById(R.id.icon_track_location_clear);
        imgView.setOnClickListener(v -> {
            mEventLocationTextView.setText("");
            createOrUpdateEvent.destination = null;
        });
    }

    private void populateControlsAndDefaultEvenData() {

        initializeBasedOnEventType();
        mEventTypeItem = new NameImageItem(R.drawable.ic_event_black_24dp, "General", mEventTypeId);
        SetDurationText();
        if (this.getIntent().getParcelableExtra(IntentConstants.DESTINATION_LOCATION) != null) {
            createOrUpdateEvent.destination = (EventPlace) this.getIntent().getParcelableExtra(IntentConstants.DESTINATION_LOCATION);
            mEventLocationTextView.setText(AppUtility.createTextForDisplay(createOrUpdateEvent.destination.getName(), Constants.EDIT_ACTIVITY_LOCATION_TEXT_LENGTH));
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
        addIfAnyContactIsSelectedFromMemberListActivity();
    }

    private void addIfAnyContactIsSelectedFromMemberListActivity() {
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
        switch (createOrUpdateEvent.eventType) {
            case SHAREMYLOACTION:
                mCreateUpdateSuccessfulMessage = getResources().getString(R.string.sharemylocation_event_create_successful);
                createOrUpdateEvent.name = "S_" + AppContext.context.loginName + "_";
                createOrUpdateEvent.description = "ShareMyLocationEvent";
                break;
            case TRACKBUDDY:
                mCreateUpdateSuccessfulMessage = getResources().getString(R.string.track_my_buddy_event_create_successful);
                createOrUpdateEvent.name = "T_L_" + AppContext.context.loginName + "_B";
                createOrUpdateEvent.description = "TrackBuddy";
                break;
            default:
                Calendar calendar_start = Calendar.getInstance();
                mCreateUpdateSuccessfulMessage = getResources().getString(R.string.meet_now_event_create_successful);
                createOrUpdateEvent.name = "Meet " + AppContext.context.loginName + " @" + DateUtil.getTime(calendar_start);
                mQuickEventNameView.setText(createOrUpdateEvent.name);
                createOrUpdateEvent.description = "QuickEvent";
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

        if (createOrUpdateEvent.getParticipantCount() == 0) {
            setAlertDialog("Oops no invitee has been selected !", "Kindly select atleast one invitee");
            mAlertDialog.show();
            return false;
        }

        return true;
    }

    @Override
    protected void populateEventData() {
        Calendar calendar_start = Calendar.getInstance();
        if (createOrUpdateEvent.eventType == EventType.QUIK) {
            createOrUpdateEvent.name = mQuickEventNameView.getText().toString();
        }
        createOrUpdateEvent.startTimeInDateFormat = calendar_start.getTime();
        super.populateEventData();
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        ContactOrGroup contact = (ContactOrGroup) adapter.getItemAtPosition(position);
        //v.setSelected(true);

        if (createOrUpdateEvent.ContactOrGroups.size() < 10) {

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
                if (createOrUpdateEvent.destination != null) {
                    intent.putExtra(IntentConstants.DESTINATION_LOCATION, (Parcelable) createOrUpdateEvent.destination);
                }
                startActivityForResult(intent, LOCATION_REQUEST_CODE);
                break;

            case R.id.tracklocation_Duration_holder:
                FragmentManager fm = getSupportFragmentManager();
                DurationOffsetFragment editNameDialogFragment = DurationOffsetFragment.newInstance(createOrUpdateEvent.duration);
                editNameDialogFragment.show(fm, "Duration");
               /* intent = new Intent(TrackLocationActivity.this, DurationOffset.class);
                intent.putExtra("com.redtop.engaze.entity.Duration", (Parcelable) createOrUpdateEvent.duration);
                startActivityForResult(intent, DURATION_REQUEST_CODE);*/
                break;
            /*case R.id.btn_tracking_start:
                createOrUpdateEvent.ContactOrGroups = new ArrayList<ContactOrGroup>(mAddedMembers.values());
                hideKeyboard(v);
                SaveEvent();
                break;*/
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
                createOrUpdateEvent.ContactOrGroups = new ArrayList<ContactOrGroup>(mAddedMembers.values());
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
                createOrUpdateEvent.ContactOrGroups = new ArrayList<ContactOrGroup>(mAddedMembers.values());
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

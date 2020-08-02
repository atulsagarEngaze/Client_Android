package com.redtop.engaze;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.redtop.engaze.adapter.ContactListAutoCompleteAdapter;
import com.redtop.engaze.common.constant.IntentConstants;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.common.enums.RecurrenceType;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.customeviews.CustomAutoCompleteTextView;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.domain.NameImageItem;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

@SuppressWarnings("deprecation")
public class CreateEditEventActivity extends BaseEventActivity {

    private ViewGroup mFlowContainerView;
    private TextView mStartDateDisplayView;
    private TextView mStartTimeDisplayView;
    private CustomAutoCompleteTextView mAutoCompleteInviteeTextView;
    private int startHours, startMinutes;
    private Calendar cal;
    private EditText mEventTitleView;
    private EditText mNoteView;
    private Boolean mIsForEdit;
    private TextView mTrackingStartOffsetView, mReminderOffsetView, mDayOfMonthView;
    private TypedArray mEventTypeImages;
    private RadioButton mRdDailyView, mRdWeeklyView, mRdMonthlyView;
    private LinearLayout mLlRecurrenceView, mLlDailySettingsView, mLlWeekySettingsView, mLlMonthlySettingsView;
    private AppCompatCheckBox mChkrecurrence, mSelectedDateCheck;
    private Hashtable<Integer, AppCompatCheckBox> mWeekDaysChecboxList;
    private String mHintFriendText;
    private ImageView imgView;

    private static final int START_TIME_DIALOG_ID = 2;
    private static final int START_DATE_DIALOG_ID = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_create_edit_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_event_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            //getSupportActionBar().setTitle(mActivityTitle);
            getSupportActionBar().setTitle(R.string.title_meet_later);
            toolbar.setNavigationOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                    finish();
                }
            });
            PreffManager.setPrefArrayList("Invitees", null);
        }
        initializeElements();
        initializeClickEvents();
        populateControls();

        if (createOrUpdateEvent.tracking.getTrackingState() == false) {
            mTrackingStartOffsetView.setVisibility(View.GONE);
        } else {
            mTrackingStartOffsetView.setVisibility(View.VISIBLE);
        }
    }

    private void initializeClickEvents() {
        ///
        mStartTimeDisplayView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog(START_TIME_DIALOG_ID);
            }
        });
        ///
        mEventTypeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Intent i = new Intent(CreateEditEventActivity.this, EventTypeList.class);
                startActivityForResult(i, EVENT_TYPE_REQUEST_CODE);
            }
        });

        mEventLocationTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateEditEventActivity.this, PickLocationActivity.class);
                if (createOrUpdateEvent.destination != null) {
                    intent.putExtra(IntentConstants.DESTINATION_LOCATION, (Parcelable) createOrUpdateEvent.destination);
                }
                startActivityForResult(intent, LOCATION_REQUEST_CODE);
            }
        });
        //////
        mStartDateDisplayView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog(START_DATE_DIALOG_ID);
            }
        });

        imgView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEventLocationTextView.setText("");
                createOrUpdateEvent.destination = null;
            }
        });

        ///
        mReminderOffsetView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(CreateEditEventActivity.this, CustomReminder.class);
                intent.putExtra("com.redtop.engaze.entity.Reminder", (Parcelable) createOrUpdateEvent.reminder);

                startActivityForResult(intent, REMINDER_REQUEST_CODE);
            }
        });
        ///
        mTrackingStartOffsetView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(CreateEditEventActivity.this, TrackingOffset.class);
                intent.putExtra("com.redtop.engaze.entity.Tracking", (Parcelable) createOrUpdateEvent.tracking);

                startActivityForResult(intent, TRACKING_REQUEST_CODE);
            }
        });
        ///
        mDurationTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CreateEditEventActivity.this, DurationOffset.class);
                intent.putExtra("com.redtop.engaze.entity.Duration", (Parcelable) createOrUpdateEvent.duration);

                startActivityForResult(intent, DURATION_REQUEST_CODE);
            }
        });

        mRdDailyView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setDailyLayoutVisible();
            }
        });

        mRdWeeklyView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setWeeklyLayoutVisible();
            }
        });

        mDayOfMonthView = (TextView) findViewById(R.id.txt_day_of_month);

        mRdMonthlyView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setMonthlyLayoutVisible();
            }
        });

        mAutoCompleteInviteeTextView.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (getAutoCompleteInviteeTextView().getText().toString().length() <= 0) {
                        if (mAddedMembers.size() > 0) {
                            int index = mAddedMembers.size() - 1;
                            View view = getContactView(index);
                            String key = (String) ((LinearLayout) view).getChildAt(0).getTag();
                            mAddedMembers.remove(key);
                            removeContactView(view, index);
                        }
                    }
                }
                return false;
            }
        });

        mAutoCompleteInviteeTextView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View arg1, int position,
                                    long arg3) {
                ContactOrGroup contact = (ContactOrGroup) adapter.getItemAtPosition(position);
                //v.setSelected(true);

                if (mAddedMembers.size() < 10) {

                    if (mAddedMembers.containsKey(contact.getName())) {
                        Toast.makeText(mContext,
                                "User is already added", Toast.LENGTH_SHORT).show();
                    } else {
                        mAddedMembers.put(contact.getName(), contact);
                        createContactLayoutItem(contact);
                        clearAutoCompleteInviteeTextView();
                    }
                } else {
                    Toast.makeText(mContext,
                            "You have reached maximum limit of participants!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        mChkrecurrence.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    setRecurrenceDefaultValuesBasedOnDate();
                    mLlRecurrenceView.setVisibility(View.VISIBLE);
                    mLlRecurrenceView.setAlpha(0.0f);
                    mLlRecurrenceView.animate()
                            //.translationY(0)
                            .alpha(1.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mLlRecurrenceView.setVisibility(View.VISIBLE);
                                }
                            });
                } else {

                    mLlRecurrenceView.animate()
                            //.translationY(llRecurrence.getHeight())
                            .alpha(0.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mLlRecurrenceView.setVisibility(View.GONE);
                                }
                            });
                }
            }
        });

        for (final AppCompatCheckBox chkBox : mWeekDaysChecboxList.values()) {
            chkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked && mSelectedDateCheck == chkBox) {
                        Toast.makeText(mContext, "this day is day of selected date, to unselect, please change the date",
                                Toast.LENGTH_LONG).show();
                        chkBox.setChecked(true);
                    }
                }
            });
        }
    }

    private void setDailyLayoutVisible() {
        mLlDailySettingsView.setVisibility(View.VISIBLE);
        mLlWeekySettingsView.setVisibility(View.GONE);
        mLlMonthlySettingsView.setVisibility(View.GONE);
    }

    private void setWeeklyLayoutVisible() {
        mLlDailySettingsView.setVisibility(View.GONE);
        mLlWeekySettingsView.setVisibility(View.VISIBLE);
        mLlMonthlySettingsView.setVisibility(View.GONE);
    }

    private void setMonthlyLayoutVisible() {
        mLlDailySettingsView.setVisibility(View.GONE);
        mLlWeekySettingsView.setVisibility(View.GONE);
        mLlMonthlySettingsView.setVisibility(View.VISIBLE);
    }

    private void setRecurrenceDefaultValuesBasedOnDate() {

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        mSelectedDateCheck = null;
        for (AppCompatCheckBox chkBox : mWeekDaysChecboxList.values()) {
            chkBox.setChecked(false);
        }
        mSelectedDateCheck = mWeekDaysChecboxList.get(dayOfWeek);
        mSelectedDateCheck.setChecked(true);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        mDayOfMonthView.setText(Integer.toString(dayOfMonth));
    }

    private void initializeElements() {
        mFlowContainerView = (ViewGroup) findViewById(R.id.participant_layout);
        mAutoCompleteInviteeTextView = (CustomAutoCompleteTextView) findViewById(R.id.searchAutoComplete);
        mEventLocationTextView = (TextView) findViewById(R.id.EventLocation_Normal);
        mEventTitleView = (EditText) findViewById(R.id.Title);
        mNoteView = (EditText) findViewById(R.id.Note);
        mStartDateDisplayView = (TextView) findViewById(R.id.StartDateDisplay);
        mDurationTextView = (TextView) findViewById(R.id.Durationholder);
        mTrackingStartOffsetView = (TextView) findViewById(R.id.TrackingStartOffeset);
        mReminderOffsetView = (TextView) findViewById(R.id.ReminderOffeset);

        mEventTypeView = (ImageView) findViewById(R.id.event_type);
        mStartTimeDisplayView = (TextView) findViewById(R.id.StartTimeDisplay);
        String strIsForEdit = this.getIntent().getStringExtra("IsForEdit");
        mEventTypeImages = getResources().obtainTypedArray(R.array.event_type_image);
        mIsForEdit = false;
        if (strIsForEdit != null) {
            mIsForEdit = Boolean.parseBoolean(strIsForEdit);
            if (mIsForEdit) {
                createOrUpdateEvent = (Event) this.getIntent().getSerializableExtra("EventDetail");
            }
        }
        imgView = (ImageView) findViewById(R.id.icon_location_clear);
        mLlRecurrenceView = (LinearLayout) findViewById(R.id.ll_recurrence);
        mLlDailySettingsView = (LinearLayout) findViewById(R.id.ll_daily_settings);
        mLlWeekySettingsView = (LinearLayout) findViewById(R.id.ll_weekly_settings);
        mLlMonthlySettingsView = (LinearLayout) findViewById(R.id.ll_monthly_settings);
        mRdDailyView = (RadioButton) findViewById(R.id.rd_daily);
        mRdWeeklyView = (RadioButton) findViewById(R.id.rd_weekly);
        mRdMonthlyView = (RadioButton) findViewById(R.id.rd_monthly);
        mChkrecurrence = (AppCompatCheckBox) findViewById(R.id.chkrecurrence);

        mWeekDaysChecboxList = new Hashtable<Integer, AppCompatCheckBox>();
        mWeekDaysChecboxList.put(1, (AppCompatCheckBox) findViewById(R.id.chksunday));
        mWeekDaysChecboxList.put(2, (AppCompatCheckBox) findViewById(R.id.chkmonday));
        mWeekDaysChecboxList.put(3, (AppCompatCheckBox) findViewById(R.id.chktuesday));
        mWeekDaysChecboxList.put(4, (AppCompatCheckBox) findViewById(R.id.chkwednesday));
        mWeekDaysChecboxList.put(5, (AppCompatCheckBox) findViewById(R.id.chkthursday));
        mWeekDaysChecboxList.put(6, (AppCompatCheckBox) findViewById(R.id.chkfriday));
        mWeekDaysChecboxList.put(7, (AppCompatCheckBox) findViewById(R.id.chksaturday));


    }

    private void populateControls() {
        mHintFriendText = getResources().getString(R.string.hint_add_friends);
        //mMembers = ContactAndGroupListManager.getAllRegisteredContacts(mContext);
        mMembers = ContactAndGroupListManager.getAllContacts();
        mAdapter = new ContactListAutoCompleteAdapter(mContext, R.layout.item_contact_group_list, mMembers);
        mAutoCompleteInviteeTextView.setAdapter(mAdapter);
        mAutoCompleteInviteeTextView.setHint(mHintFriendText);
        mAddedMembers = new Hashtable<String, ContactOrGroup>();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        cal = Calendar.getInstance();
        if (mIsForEdit) {

            mCreateUpdateSuccessfulMessage = getResources().getString(R.string.event_update_successful);

            SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            try {
                createOrUpdateEvent.startTimeInDateFormat = parseFormat.parse(createOrUpdateEvent.startTime);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            cal.setTime(createOrUpdateEvent.startTimeInDateFormat);
            mEventTitleView.setText(createOrUpdateEvent.name);
            mNoteView.setText(createOrUpdateEvent.description);
            ArrayList<ContactOrGroup> contactList = new ArrayList<ContactOrGroup>();
            String currentMemUserId = createOrUpdateEvent.getCurrentParticipant().getUserId();
            ArrayList<EventParticipant> members = createOrUpdateEvent.participants;
            for (EventParticipant mem : members) {
                if (!mem.getUserId().equals(currentMemUserId)) {
                    ContactOrGroup cg = ContactAndGroupListManager.getContact(mem.getUserId());
                    contactList.add(cg);
                    mAddedMembers.put(cg.getName(), cg);
                    createContactLayoutItem(cg);
                }
            }
            if (contactList.size() > 0) {
                clearAutoCompleteInviteeTextView();
            }

            if (createOrUpdateEvent.destination != null) {
                appLocationService.displayPlace(createOrUpdateEvent.destination, mEventLocationTextView);
            }

            if (createOrUpdateEvent.IsRecurrence) {
                populateEventRecurrenceData();
            }
        } else {

            mCreateUpdateSuccessfulMessage = getResources().getString(R.string.title_create_event);

            mEventTypeId = EventType.GENERAL.GetEventTypeId();
            initializeEventWithDefaultValues();
            mEventTypeItem = new NameImageItem(R.drawable.ic_event_black_24dp, "General", 6);

            if (this.getIntent().getParcelableExtra(IntentConstants.DESTINATION_LOCATION) != null) {
                mFromEventsActivity = false;
                createOrUpdateEvent.destination = (EventPlace) this.getIntent().getParcelableExtra(IntentConstants.DESTINATION_LOCATION);
                mEventLocationTextView.setText(AppUtility.createTextForDisplay(createOrUpdateEvent.destination.getName(), Constants.EDIT_ACTIVITY_LOCATION_TEXT_LENGTH));
            }
        }
        Drawable originalDrawable = getResources().getDrawable(mEventTypeItem.getImageId());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Drawable wrappedDrawable = DrawableCompat.wrap(originalDrawable);
            DrawableCompat.setTint(wrappedDrawable, mContext.getResources().getColor(R.color.icon));
            mEventTypeView.setBackground(wrappedDrawable);
        } else {
            mEventTypeView.setBackground(originalDrawable);
        }

        SetReminderText();
        SetTrackingText();
        SetDurationText();
        startHours = cal.get(Calendar.HOUR_OF_DAY);
        startMinutes = cal.get(Calendar.MINUTE);
        updateTime(mStartTimeDisplayView, startHours, startMinutes);
        updateDate(mStartDateDisplayView);

    }

    private void populateEventRecurrenceData() {
        mIsRecurrence = "true";
        mChkrecurrence.setChecked(true);
        if (createOrUpdateEvent.RecurrenceType == RecurrenceType.DAILY) {

            mRdDailyView.setChecked(true);
            setDailyLayoutVisible();
            ((TextView) findViewById(R.id.day_frequency_input)).setText(createOrUpdateEvent.FrequencyOfOccurence);
        } else if (createOrUpdateEvent.RecurrenceType == RecurrenceType.WEEKLY) {

            mRdWeeklyView.setChecked(true);
            ((TextView) findViewById(R.id.week_frequency_input)).setText(createOrUpdateEvent.FrequencyOfOccurence);
            setWeeklyLayoutVisible();

            for (int day : createOrUpdateEvent.RecurrenceDays) {
                mWeekDaysChecboxList.get(day).setChecked(true);

            }
        } else {

            mRdMonthlyView.setChecked(true);
            setMonthlyLayoutVisible();
            ((TextView) findViewById(R.id.month_frequency_input)).setText(createOrUpdateEvent.FrequencyOfOccurence);
        }

        ((TextView) findViewById(R.id.occurece_input)).setText(createOrUpdateEvent.NumberOfOccurences);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.common_action_save:
                createOrUpdateEvent.ContactOrGroups = new ArrayList<ContactOrGroup>(mAddedMembers.values());
                this.SaveEvent();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateTime(TextView timeView, int hours, int minutes) {
        //Calendar datetime = Calendar.getInstance();
        Calendar currentDatetime = Calendar.getInstance();
        currentDatetime.set(Calendar.SECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        Date dt = cal.getTime();
        Date currentDt = currentDatetime.getTime();

        if (dt.compareTo(currentDt) < 0) {
            //System.out.println("dt is before currentdt");
            Toast.makeText(getBaseContext(),
                    getResources().getString(R.string.message_createEvent_timestampCheck),
                    Toast.LENGTH_LONG).show();
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            String timeAmPm = sdf.format(dt);
            timeView.setText(timeAmPm);
        }
    }

    private void updateDate(TextView dateView) {
        SimpleDateFormat parseFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
        dateView.setText(parseFormat.format(cal.getTime()));
    }

    private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int yr, int monthOfYear,
                              int dayOfMonth) {
            cal.set(yr, monthOfYear, dayOfMonth);
            updateDate(mStartDateDisplayView);
            showDialog(START_TIME_DIALOG_ID);
            if (mChkrecurrence.isChecked()) {
                setRecurrenceDefaultValuesBasedOnDate();
            }
        }
    };

    private TimePickerDialog.OnTimeSetListener startTimeListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            startHours = hourOfDay;
            startMinutes = minute;
            updateTime(mStartTimeDisplayView, startHours, startMinutes);
        }
    };

    protected Dialog onCreateDialog(int id) {
        Dialog dpd = null;
        switch (id) {
            case START_DATE_DIALOG_ID:
                dpd = new DatePickerDialog(this, startDateListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                ((DatePickerDialog) dpd).getDatePicker().setMinDate(new Date().getTime() - 1000);
                break;
            case START_TIME_DIALOG_ID:
                dpd = new TimePickerDialog(this, startTimeListener, startHours, startMinutes, false);
                break;

            default:
                dpd = null;

        }
        return dpd;
    }

    protected void SaveEvent() {

        populateEventData();
        if (!validateInputData()) {
            return;
        }
        super.saveEvent(false);
    }

    private Boolean validateInputData() {

        if (createOrUpdateEvent.name == null || createOrUpdateEvent.name.isEmpty()) {
            setAlertDialog("Oops event title is blank !", "Kindly give a title to your event");
            mAlertDialog.show();
            return false;
        }

        if (createOrUpdateEvent.participants.size() == 0) {
            setAlertDialog("Oops no invitee has been selected !", "Kindly select atleast one invitee");
            mAlertDialog.show();
            return false;
        }
        if (createOrUpdateEvent.IsRecurrence.equals("true")) {
            Integer mimmumOccurrences = getResources().getInteger(R.integer.minumim_reccurrence_value);
            if (createOrUpdateEvent.NumberOfOccurences < mimmumOccurrences) {
                setAlertDialog("Number of reoccurrences less than " + Integer.toOctalString(mimmumOccurrences), "Kindly select greater value");
                mAlertDialog.show();
                return false;
            }
        }

        return true;
    }

    @Override
    protected void populateEventData() {

        DateFormat writeFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm a");
        try {
            createOrUpdateEvent.startTimeInDateFormat = writeFormat.parse(mStartDateDisplayView.getText() + " " + mStartTimeDisplayView.getText());
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        createOrUpdateEvent.name = mEventTitleView.getText().toString();
        createOrUpdateEvent.description = mNoteView.getText().toString();
        //For Recurrence
        if (mChkrecurrence.isChecked()) {
            createOrUpdateEvent.IsRecurrence = true;
            if (mRdDailyView.isChecked()) {
                createOrUpdateEvent.RecurrenceType = RecurrenceType.DAILY;
                createOrUpdateEvent.FrequencyOfOccurence = Integer.parseInt(((TextView) findViewById(R.id.day_frequency_input)).getText().toString());
            } else if (mRdWeeklyView.isChecked()) {
                createOrUpdateEvent.RecurrenceType = RecurrenceType.WEEKLY;
                createOrUpdateEvent.FrequencyOfOccurence = Integer.parseInt(((TextView) findViewById(R.id.week_frequency_input)).getText().toString());
                createOrUpdateEvent.RecurrenceDays.clear();
                for (int day : mWeekDaysChecboxList.keySet()) {
                    if (mWeekDaysChecboxList.get(day).isChecked()) {
                        createOrUpdateEvent.RecurrenceDays.add(day);
                    }
                }

            } else {
                createOrUpdateEvent.RecurrenceType = RecurrenceType.MONTHLY;
                createOrUpdateEvent.FrequencyOfOccurence = Integer.parseInt(((TextView) findViewById(R.id.month_frequency_input)).getText().toString());
            }
            createOrUpdateEvent.NumberOfOccurences = Integer.parseInt(((TextView) findViewById(R.id.occurece_input)).getText().toString());

        } else {
            createOrUpdateEvent.IsRecurrence = false;
        }

        super.populateEventData();
    }

    public void createContactLayoutItem(ContactOrGroup cg) {
        int childrenCount = mFlowContainerView.getChildCount();
        LinearLayout contactLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.template_contact_item_autocomplete, null);

        TextView lblname = (TextView) contactLayout.getChildAt(0);
        lblname.setText(cg.getName());
        lblname.setTag(cg.getName());

        contactLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlowContainerView.removeView(v);
                onContactViewClicked(v);
            }
        });
        mFlowContainerView.addView(contactLayout, childrenCount - 1, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    public void onContactViewClicked(View v) {
        mAddedMembers.remove((String) ((LinearLayout) v).getChildAt(0).getTag());
    }

    public void clearAutoCompleteInviteeTextView() {
        mAutoCompleteInviteeTextView.setWidth(AppUtility.dpToPx(50, mContext));
        mAutoCompleteInviteeTextView.setText("");
        mAutoCompleteInviteeTextView.setHint("");
        mAutoCompleteInviteeTextView.clearListSelection();
    }

    public CustomAutoCompleteTextView getAutoCompleteInviteeTextView() {
        return mAutoCompleteInviteeTextView;
    }

    public View getContactView(int index) {

        return mFlowContainerView.getChildAt(index);
    }

    public void removeContactView(View view, int index) {
        mFlowContainerView.removeView(view);
        if (index == 0) {
            mAutoCompleteInviteeTextView.setHint(mHintFriendText);
            mAutoCompleteInviteeTextView.setWidth(AppUtility.dpToPx(250, mContext));
        }
    }
}

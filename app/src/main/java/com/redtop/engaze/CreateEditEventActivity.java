package com.redtop.engaze;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

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

import com.google.android.gms.maps.model.LatLng;
import com.redtop.engaze.adapter.ContactListAutoCompleteAdapter;
import com.redtop.engaze.common.PreffManager;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.customeviews.CustomAutoCompleteTextView;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.EventDetail;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.domain.NameImageItem;
import com.redtop.engaze.domain.Reminder;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;
import com.redtop.engaze.domain.service.EventParser;
import com.redtop.engaze.webservice.Routes;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

@SuppressWarnings("deprecation")
public class CreateEditEventActivity extends BaseEventActivity {

    private ArrayList<ContactOrGroup> mMembers = new ArrayList<ContactOrGroup>();
    private ContactListAutoCompleteAdapter mAdapter;
    private Hashtable<String, ContactOrGroup> mAddedMembers;
    private ViewGroup mFlowContainer;
    private TextView mStartDateDisplay;
    private TextView mStartTimeDisplay;
    private CustomAutoCompleteTextView mAutoCompleteInviteeTextView;
    private int startHours, startMinutes;
    private Calendar cal;
    private EditText mEventtitle;
    private EditText mNote;
    private Boolean mIsForEdit;
    private TextView mTrackingStartOffeset, mReminderOffeset, mDayOfMonth;
    private TypedArray mEventTypeImages;
    private RadioButton mRdDaily, mRdWeekly, mRdMonthly;
    private LinearLayout mLlRecurrence, mLlDailySettings, mLlWeekySettings, mLlMonthlySettings;
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
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
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
        populateEventData();

        if (mTracking.getTrackingState() == false) {
            mTrackingStartOffeset.setVisibility(View.GONE);
        } else {
            mTrackingStartOffeset.setVisibility(View.VISIBLE);
        }
    }

    private void initializeClickEvents() {
        ///
        mStartTimeDisplay.setOnClickListener(new OnClickListener() {

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
                if (mDestinationPlace != null) {
                    intent.putExtra("DestinatonLocation", (Parcelable) mDestinationPlace);
                }
                startActivityForResult(intent, LOCATION_REQUEST_CODE);
            }
        });
        //////
        mStartDateDisplay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog(START_DATE_DIALOG_ID);
            }
        });

        imgView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEventLocationTextView.setText("");
                mDestinationPlace = null;
            }
        });

        ///
        mReminderOffeset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(CreateEditEventActivity.this, CustomReminder.class);
                intent.putExtra("com.redtop.engaze.entity.Reminder", mReminder);

                startActivityForResult(intent, REMINDER_REQUEST_CODE);
            }
        });
        ///
        mTrackingStartOffeset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(CreateEditEventActivity.this, TrackingOffset.class);
                intent.putExtra("com.redtop.engaze.entity.Tracking", mTracking);

                startActivityForResult(intent, TRACKING_REQUEST_CODE);
            }
        });
        ///
        mDurationtext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CreateEditEventActivity.this, DurationOffset.class);
                intent.putExtra("com.redtop.engaze.entity.Duration", mDuration);

                startActivityForResult(intent, DURATION_REQUEST_CODE);
            }
        });

        mRdDaily.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setDailyLayoutVisible();
            }
        });

        mRdWeekly.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setWeeklyLayoutVisible();
            }
        });

        mDayOfMonth = (TextView) findViewById(R.id.txt_day_of_month);

        mRdMonthly.setOnClickListener(new OnClickListener() {
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
                    mLlRecurrence.setVisibility(View.VISIBLE);
                    mLlRecurrence.setAlpha(0.0f);
                    mLlRecurrence.animate()
                            //.translationY(0)
                            .alpha(1.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mLlRecurrence.setVisibility(View.VISIBLE);
                                }
                            });
                } else {

                    mLlRecurrence.animate()
                            //.translationY(llRecurrence.getHeight())
                            .alpha(0.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mLlRecurrence.setVisibility(View.GONE);
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
        mLlDailySettings.setVisibility(View.VISIBLE);
        mLlWeekySettings.setVisibility(View.GONE);
        mLlMonthlySettings.setVisibility(View.GONE);
    }

    private void setWeeklyLayoutVisible() {
        mLlDailySettings.setVisibility(View.GONE);
        mLlWeekySettings.setVisibility(View.VISIBLE);
        mLlMonthlySettings.setVisibility(View.GONE);
    }

    private void setMonthlyLayoutVisible() {
        mLlDailySettings.setVisibility(View.GONE);
        mLlWeekySettings.setVisibility(View.GONE);
        mLlMonthlySettings.setVisibility(View.VISIBLE);
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
        mDayOfMonth.setText(Integer.toString(dayOfMonth));
    }

    private void initializeElements() {
        mFlowContainer = (ViewGroup) findViewById(R.id.participant_layout);
        mAutoCompleteInviteeTextView = (CustomAutoCompleteTextView) findViewById(R.id.searchAutoComplete);
        mDestinationPlace = null;
        mEventLocationTextView = (TextView) findViewById(R.id.EventLocation_Normal);
        mEventtitle = (EditText) findViewById(R.id.Title);
        mNote = (EditText) findViewById(R.id.Note);
        mStartDateDisplay = (TextView) findViewById(R.id.StartDateDisplay);
        mDurationtext = (TextView) findViewById(R.id.Durationholder);
        mTrackingStartOffeset = (TextView) findViewById(R.id.TrackingStartOffeset);
        mReminderOffeset = (TextView) findViewById(R.id.ReminderOffeset);

        mEventTypeView = (ImageView) findViewById(R.id.event_type);
        mStartTimeDisplay = (TextView) findViewById(R.id.StartTimeDisplay);
        String strIsForEdit = this.getIntent().getStringExtra("IsForEdit");
        mEventTypeImages = getResources().obtainTypedArray(R.array.event_type_image);
        mIsForEdit = false;
        if (strIsForEdit != null) {
            mIsForEdit = Boolean.parseBoolean(strIsForEdit);
            if (mIsForEdit) {
                mEventDetailData = (EventDetail) this.getIntent().getSerializableExtra("EventDetail");
            }
        }
        imgView = (ImageView) findViewById(R.id.icon_location_clear);
        mLlRecurrence = (LinearLayout) findViewById(R.id.ll_recurrence);
        mLlDailySettings = (LinearLayout) findViewById(R.id.ll_daily_settings);
        mLlWeekySettings = (LinearLayout) findViewById(R.id.ll_weekly_settings);
        mLlMonthlySettings = (LinearLayout) findViewById(R.id.ll_monthly_settings);
        mRdDaily = (RadioButton) findViewById(R.id.rd_daily);
        mRdWeekly = (RadioButton) findViewById(R.id.rd_weekly);
        mRdMonthly = (RadioButton) findViewById(R.id.rd_monthly);
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

    private void populateEventData() {
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
            getResources().getString(R.string.title_edit_event);
            mCreateUpdateUrl = Routes.UPDATE_EVENT;
            mCreateUpdateSuccessfulMessage = getResources().getString(R.string.event_update_successful);

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                //cal.setTime(sdf.parse(DateUtil.convertUtcToLocalDateTime(mEventDetailData.getStartTime(), sdf)));
                cal.setTime(sdf.parse(mEventDetailData.getStartTime()));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mEventtitle.setText(mEventDetailData.getName());
            mNote.setText(mEventDetailData.getDescription());
            ArrayList<ContactOrGroup> contactList = new ArrayList<ContactOrGroup>();
            String currentMemUserId = mEventDetailData.getCurrentParticipant().getUserId();
            ArrayList<EventParticipant> members = mEventDetailData.getParticipants();
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

            mReminder = new Reminder(Integer.parseInt(mEventDetailData.getReminderOffset()), "minute", mEventDetailData.getReminderType());
            mTracking = new Duration(Integer.parseInt(mEventDetailData.getTrackingStartOffset()), "minute", true);
            mDuration = new Duration((int) Double.parseDouble(mEventDetailData.getDuration()), "minute", true);
            mEventTypeItem = new NameImageItem(mEventTypeImages.getResourceId(Integer.parseInt(mEventDetailData.getEventTypeId()), -1), mEventTypeImages.getString(Integer.parseInt(mEventDetailData.getEventTypeId())), Integer.parseInt(mEventDetailData.getEventTypeId()));
            if (!(mEventDetailData.getDestinationLatitude().equals("null") || mEventDetailData.getDestinationLatitude() == null || mEventDetailData.getDestinationLatitude().equals(""))) {
                mDestinationPlace = new EventPlace(mEventDetailData.getDestinationName(), mEventDetailData.getDestinationAddress(), new LatLng(Double.parseDouble(mEventDetailData.getDestinationLatitude()), Double.parseDouble(mEventDetailData.getDestinationLongitude())));
                mLh.displayPlace(mDestinationPlace, mEventLocationTextView);
            }

            if (mEventDetailData.getIsRecurrence().equals("true")) {
                populateEventRecurrenceData();
            }
        } else {
            getResources().getString(R.string.title_create_event);
            mCreateUpdateUrl = Routes.CREATE_EVENT;
            mCreateUpdateSuccessfulMessage = getResources().getString(R.string.event_create_successful);
            mEventTypeItem = new NameImageItem(R.drawable.ic_event_black_24dp, "General", 6);
            mReminder = new Reminder(Integer.parseInt(sharedPrefs.getString("ReminderInterval", getResources().getString(R.string.event_reminder_default_interval))), sharedPrefs.getString("ReminderPeriod", getResources().getString(R.string.event_reminder_default_period)), sharedPrefs.getString("ReminderNotification", getResources().getString(R.string.event_reminder_default_notification)));
            mTracking = new Duration(Integer.parseInt(sharedPrefs.getString("TrackingInterval", getResources().getString(R.string.event_tracking_default_interval))), sharedPrefs.getString("TrackingPeriod", getResources().getString(R.string.event_tracking_default_period)), Boolean.parseBoolean(sharedPrefs.getString("TrackingEnabled", getResources().getString(R.string.event_tracking_default_enabled))));
            mDuration = new Duration(Integer.parseInt(sharedPrefs.getString("defaultDuration", getResources().getString(R.string.event_default_duration))), sharedPrefs.getString("defaultPeriod", getResources().getString(R.string.event_default_period)), Boolean.parseBoolean(sharedPrefs.getString("trackingEnabled", getResources().getString(R.string.event_tracking_default_enabled))));
            if (this.getIntent().getParcelableExtra("DestinatonLocation") != null) {
                mFromEventsActivity = false;
                mDestinationPlace = (EventPlace) this.getIntent().getParcelableExtra("DestinatonLocation");
                mEventLocationTextView.setText(AppUtility.createTextForDisplay(mDestinationPlace.getName(), Constants.EDIT_ACTIVITY_LOCATION_TEXT_LENGTH));
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

        SetReminderText(mReminder);
        SetTrackingText(mTracking);
        SetDurationText(mDuration);
        startHours = cal.get(Calendar.HOUR_OF_DAY);
        startMinutes = cal.get(Calendar.MINUTE);
        updateTime(mStartTimeDisplay, startHours, startMinutes);
        updateDate(mStartDateDisplay);

    }

    private void populateEventRecurrenceData() {
        mIsRecurrence = "true";
        mChkrecurrence.setChecked(true);
        mFrequencyOfOcuurence = mEventDetailData.getFrequencyOfOccurence();
        if (mEventDetailData.getRecurrenceType().equals("1")) {
            mRecurrenceType = "1";
            mRdDaily.setChecked(true);
            setDailyLayoutVisible();
            ((TextView) findViewById(R.id.day_frequency_input)).setText(mFrequencyOfOcuurence);
        } else if (mEventDetailData.getRecurrenceType().equals("2")) {
            mRecurrenceType = "2";
            mRdWeekly.setChecked(true);
            ((TextView) findViewById(R.id.week_frequency_input)).setText(mFrequencyOfOcuurence);
            setWeeklyLayoutVisible();
            mRecurrencedays = new ArrayList<Integer>();
            for (int day : mEventDetailData.getRecurrenceDays()) {
                mWeekDaysChecboxList.get(day).setChecked(true);
                mRecurrencedays.add(day);
            }
        } else {
            mRecurrenceType = "3";
            mRdMonthly.setChecked(true);
            setMonthlyLayoutVisible();
            ((TextView) findViewById(R.id.month_frequency_input)).setText(mFrequencyOfOcuurence);
        }
        mNumberOfOccurences = mEventDetailData.getNumberOfOccurences();
        ((TextView) findViewById(R.id.occurece_input)).setText(mNumberOfOccurences);
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
                mContactsAndgroups = new ArrayList<ContactOrGroup>(mAddedMembers.values());
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
            updateDate(mStartDateDisplay);
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
            updateTime(mStartTimeDisplay, startHours, startMinutes);
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

        mEventJobj = createEventJSON();
        if (!validateInputData()) {
            return;
        }
        super.saveEvent(false);
    }

    private Boolean validateInputData() {

        try {
            if (mEventJobj.getString("Name").isEmpty()) {
                setAlertDialog("Oops event title is blank !", "Kindly give a title to your event");
                mAlertDialog.show();
                return false;
            }

            if (mEventJobj.getJSONArray("UserList").length() == 0) {
                setAlertDialog("Oops no invitee has been selected !", "Kindly select atleast one invitee");
                mAlertDialog.show();
                return false;
            }
            if (mIsRecurrence.equals("true")) {
                Integer mimmumOccurrences = getResources().getInteger(R.integer.minumim_reccurrence_value);
                if (Integer.parseInt(mNumberOfOccurences) < mimmumOccurrences) {
                    setAlertDialog("Number of reoccurrences less than " + Integer.toOctalString(mimmumOccurrences), "Kindly select greater value");
                    mAlertDialog.show();
                    return false;
                }
            }


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }

    private JSONObject createEventJSON() {

        String start = mStartDateDisplay.getText() + " " + mStartTimeDisplay.getText();
        DateFormat writeFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm a");

        try {
            mStartDate = writeFormat.parse(start);

        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (mIsForEdit) {
            mEventId = mEventDetailData.getEventId();
        }

        mEventName = mEventtitle.getText().toString();
        mEventDescription = mNote.getText().toString();
        mIsQuickEvent = "false";
        //For Recurrence
        if (mChkrecurrence.isChecked()) {
            mIsRecurrence = "true";
            if (mRdDaily.isChecked()) {
                mRecurrenceType = "1";
                mFrequencyOfOcuurence = ((TextView) findViewById(R.id.day_frequency_input)).getText().toString();
            } else if (mRdWeekly.isChecked()) {
                mRecurrenceType = "2";
                mFrequencyOfOcuurence = ((TextView) findViewById(R.id.week_frequency_input)).getText().toString();
                mRecurrencedays = new ArrayList<Integer>();
                for (int day : mWeekDaysChecboxList.keySet()) {
                    if (mWeekDaysChecboxList.get(day).isChecked()) {
                        mRecurrencedays.add(day);
                    }
                }
            } else {
                mRecurrenceType = "3";
                mFrequencyOfOcuurence = ((TextView) findViewById(R.id.month_frequency_input)).getText().toString();
            }
            mNumberOfOccurences = ((TextView) findViewById(R.id.occurece_input)).getText().toString();

        } else {
            mIsRecurrence = "false";
        }
        return super.createEventJson();
    }

    public void createContactLayoutItem(ContactOrGroup cg) {
        int childrenCount = mFlowContainer.getChildCount();
        LinearLayout contactLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.template_contact_item_autocomplete, null);

        TextView lblname = (TextView) contactLayout.getChildAt(0);
        lblname.setText(cg.getName());
        lblname.setTag(cg.getName());

        contactLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlowContainer.removeView(v);
                onContactViewClicked(v);
            }
        });
        mFlowContainer.addView(contactLayout, childrenCount - 1, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
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

        return mFlowContainer.getChildAt(index);
    }

    public void removeContactView(View view, int index) {
        mFlowContainer.removeView(view);
        if (index == 0) {
            mAutoCompleteInviteeTextView.setHint(mHintFriendText);
            mAutoCompleteInviteeTextView.setWidth(AppUtility.dpToPx(250, mContext));
        }

    }
}

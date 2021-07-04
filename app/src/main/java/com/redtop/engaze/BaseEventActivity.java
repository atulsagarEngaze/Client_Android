package com.redtop.engaze;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.Interface.FragmentToActivity;
import com.redtop.engaze.Interface.OnEventSaveCompleteListner;
import com.redtop.engaze.adapter.ContactListAutoCompleteAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.EventState;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.common.utility.AppLocationService;
import com.redtop.engaze.common.cache.DestinationCacher;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.common.utility.PermissionRequester;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.NameImageItem;
import com.redtop.engaze.domain.Reminder;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.fragment.DurationOffsetFragment;
import com.redtop.engaze.fragment.TrackingOffsetFragment;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import static com.redtop.engaze.common.constant.RequestCode.Permission.ACCESS_BACKGROUND_LOCATION;
import static com.redtop.engaze.common.constant.RequestCode.Permission.SEND_SMS;

public abstract class BaseEventActivity extends BaseActivity implements FragmentToActivity<Duration> {
    protected int mDurationTime = 0;

    protected TextView mQuickEventNameView;
    protected TextView mEventLocationTextView;
    protected NameImageItem mEventTypeItem;
    protected TextView mDurationTextView;

    protected AppLocationService appLocationService;
    protected ImageView mEventTypeView;
    protected String TAG;
    protected AlertDialog mAlertDialog;
    protected Boolean mFromEventsActivity = true;    //For Recurrence
    protected String mIsRecurrence = "false";
    public Event notificationselectedEvent;

    protected static final int REMINDER_REQUEST_CODE = 2;
    protected static final int EVENT_TYPE_REQUEST_CODE = 6;
    protected static final int TRACKING_REQUEST_CODE = 3;
    protected static final int DURATION_REQUEST_CODE = 5;
    protected static final int LOCATION_REQUEST_CODE = 7;

    protected Event createOrUpdateEvent;
    protected int mEventTypeId;
    protected String mCreateUpdateSuccessfulMessage;
    protected HashMap<String, ContactOrGroup> mAddedMembers;
    ArrayList<ContactOrGroup> mMembers = new ArrayList<ContactOrGroup>();
    ContactListAutoCompleteAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appLocationService = new AppLocationService(this, this);
    }

    @Override
    public void communicate(Duration duration, Fragment source) {
        if(source instanceof DurationOffsetFragment) {
            createOrUpdateEvent.duration = duration;
            SetDurationText();
        }
        else if (source instanceof TrackingOffsetFragment){
            createOrUpdateEvent.tracking = duration;
            SetTrackingText();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case EVENT_TYPE_REQUEST_CODE:
                    //TextView eventTypeView = (TextView)findViewById(R.id.EventType);
                    mEventTypeItem = (NameImageItem) data.getParcelableExtra("com.redtop.engaze.entity.NameImageItem");
                    if (mEventTypeItem != null) {
                        Drawable originalDrawable = getResources().getDrawable(mEventTypeItem.getImageId());
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            Drawable wrappedDrawable = DrawableCompat.wrap(originalDrawable);
                            DrawableCompat.setTint(wrappedDrawable, mContext.getResources().getColor(R.color.icon));
                            mEventTypeView.setBackground(wrappedDrawable);
                        } else {
                            mEventTypeView.setBackground(getResources().getDrawable(mEventTypeItem.getImageId()));
                        }

                        //eventTypeView.setText(eventTypeItem.getName());
                        Log.d(TAG, "insdie if " + mEventTypeItem.getName());
                        Log.d(TAG, "insdie if " + mEventTypeItem.getImageId());
                    } else {
                        Log.d(TAG, "insdie else");
                    }
                    break;

                case REMINDER_REQUEST_CODE:
                    createOrUpdateEvent.reminder = data.getParcelableExtra("com.redtop.engaze.entity.Reminder");
                    SetReminderOffset();
                    SetReminderText();
                    break;
                case TRACKING_REQUEST_CODE:
                    createOrUpdateEvent.tracking = data.getParcelableExtra("com.redtop.engaze.entity.Tracking");
                    SetTrackingText();
                    break;

                case DURATION_REQUEST_CODE:
                    createOrUpdateEvent.duration = data.getParcelableExtra("com.redtop.engaze.entity.Duration");
                    SetDurationText();
                    break;

                case LOCATION_REQUEST_CODE:
                    createOrUpdateEvent.destination = data.getParcelableExtra("DestinatonPlace");
                    appLocationService.displayPlace(createOrUpdateEvent.destination, mEventLocationTextView);

                    break;

            }
        }
    }

    protected void SetDurationText() {
        if (createOrUpdateEvent.duration != null) {
            mDurationTime = createOrUpdateEvent.duration.getTimeInterval();
            String holder = "";

            holder = DateUtil.getDurationText(createOrUpdateEvent.duration.getOffSetInMinutes()).toLowerCase();
            if (holder.equals("0")) {
                holder = "0 minute";

            } else if (!(holder.contains("minutes") || holder.contains("minute"))) {
                holder = holder.replace("min", "minute");
                holder = holder.replace("mins", "minutes");
            }

            mDurationTextView.setText(holder);

        } else {
            Log.d(TAG, "inside else");
        }
    }

    protected void SetReminderOffset() {

        if (createOrUpdateEvent.reminder != null) {
            switch (createOrUpdateEvent.reminder.getPeriod()) {
                case "minute":
                    createOrUpdateEvent.reminder.ReminderOffsetInMinute
                            = createOrUpdateEvent.reminder.getTimeInterval();
                    break;
                case "hour":
                    createOrUpdateEvent.reminder.ReminderOffsetInMinute
                            = createOrUpdateEvent.reminder.getTimeInterval() * 60;
                    break;
                case "day":
                    createOrUpdateEvent.reminder.ReminderOffsetInMinute
                            = createOrUpdateEvent.reminder.getTimeInterval() * 60 * 24;
                    break;
                case "week":
                    createOrUpdateEvent.reminder.ReminderOffsetInMinute
                            = createOrUpdateEvent.reminder.getTimeInterval() * 60 * 24 * 7;
                    break;
            }
        }
    }

    protected void SetReminderText() {
        TextView reminderOffsetText = (TextView) findViewById(R.id.ReminderOffeset);

        if (createOrUpdateEvent.reminder != null) {
            String reminderText = "";

            reminderText = DateUtil.getDurationText(createOrUpdateEvent.reminder.ReminderOffsetInMinute)
                    .toLowerCase();
            if (reminderText.equals("0")) {
                reminderText = "0 minute";

            } else if (!(reminderText.contains("minutes") || reminderText.contains("minute"))) {
                reminderText = reminderText.replace("min", "minute");
                reminderText = reminderText.replace("mins", "minutes");
            }
            reminderText += " before through " + createOrUpdateEvent.reminder.getNotificationType();

            reminderOffsetText.setText(reminderText);

        }
    }

    protected void SetTrackingText() {
        TextView trackingOffsettext = (TextView) findViewById(R.id.TrackingStartOffeset);

        if (createOrUpdateEvent.tracking != null) {
            String trackingText = "";


            trackingText = DateUtil.getDurationText(createOrUpdateEvent.tracking.getOffSetInMinutes()).toLowerCase();
            if (trackingText.equals("0")) {
                trackingText = "0 minute";

            } else if (!(trackingText.contains("minutes") || trackingText.contains("minute"))) {
                trackingText = trackingText.replace("min", "minute");
                trackingText = trackingText.replace("mins", "minutes");
            }

            trackingText += " before";
            trackingOffsettext.setText(trackingText);

        }
    }

    protected void setAlertDialog(String Title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                mContext);
        // set title
        alertDialogBuilder.setTitle(Title);
        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        dialog.cancel();
                    }
                });

        mAlertDialog = alertDialogBuilder.create();
    }

    private void setTrackingOffset() {

        long trackingOffset = 0;
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(createOrUpdateEvent.startTimeInDateFormat);
        long diffMinutes = (startCal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 60000;

        if (trackingOffset > diffMinutes) {
            trackingOffset = diffMinutes;
        }

        createOrUpdateEvent.tracking.setTimeInterval((int) trackingOffset);
    }

    private void setReminderOffset() {

        long reminderOffset = 0;
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(createOrUpdateEvent.startTimeInDateFormat);
        long diffMinutes = (startCal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 60000;
        if (reminderOffset > diffMinutes) {
            reminderOffset = diffMinutes;
        }

        createOrUpdateEvent.reminder.setTimeInterval((int) reminderOffset);
        createOrUpdateEvent.reminder.ReminderOffsetInMinute = reminderOffset;

    }

    protected void saveEvent(final Boolean isMeetNow) {

        showProgressBar(getResources().getString(R.string.message_general_progressDialog));

        EventManager.saveEvent(createOrUpdateEvent, isMeetNow, createOrUpdateEvent.reminder, new OnEventSaveCompleteListner() {

            @Override
            public void eventSaveComplete(Event event) {
                Toast.makeText(getApplicationContext(),
                        mCreateUpdateSuccessfulMessage,
                        Toast.LENGTH_LONG).show();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (createOrUpdateEvent.destination != null) {//when event is created without destination
                            DestinationCacher.cacheDestination(createOrUpdateEvent.destination, mContext);
                        }
                    }
                });

                if (createOrUpdateEvent.eventType == EventType.SHAREMYLOACTION) {
                    gotoHomePage();
                } else if (createOrUpdateEvent.eventType == EventType.TRACKBUDDY ||
                        createOrUpdateEvent.eventType == EventType.QUIK) {
                    gotoTrackingPage(event.eventId);
                } else {
                    gotoEventsPage();
                }
            }

        }, AppContext.actionHandler);

    }

    protected void gotoHomePage() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, HomeActivity.class);
                startActivity(intent);
                hideProgressBar();
                finish();
            }
        });

    }

    private void gotoTrackingPage(final String eventid) {

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, RunningEventActivity.class);
                intent.putExtra("EventId", eventid);
                startActivity(intent);
                hideProgressBar();
                finish();
                //}
            }
        });
    }

    private void gotoEventsPage() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, EventsActivity.class);
                startActivity(intent);
                hideProgressBar();
                finish();
            }
        });
    }

    protected void initializeEventWithDefaultValues() {
        createOrUpdateEvent = new Event();
        createOrUpdateEvent.eventType = EventType.getEventType(mEventTypeId);
        createOrUpdateEvent.initiatorId = AppContext.context.loginId;
        createOrUpdateEvent.initiatorName = AppContext.context.loginName;
        mAddedMembers = new HashMap<>();
        Duration defaultDuration = AppContext.context.defaultDurationSettings;
        createOrUpdateEvent.duration = new Duration(defaultDuration.getTimeInterval(),
                defaultDuration.getPeriod(),
                true);

        Reminder defaultReminder = AppContext.context.defaultReminderSettings;
        createOrUpdateEvent.reminder = new Reminder(defaultReminder.getTimeInterval(), defaultReminder.getPeriod(), defaultReminder.getNotificationType());
        createOrUpdateEvent.reminder.ReminderOffsetInMinute = defaultReminder.ReminderOffsetInMinute;

        Duration defaultTracking = AppContext.context.defaultTrackingSettings;
        createOrUpdateEvent.tracking = new Duration(defaultTracking.getTimeInterval(), defaultTracking.getPeriod(), defaultTracking.getTrackingState());

        EventParticipant currentParticipant = new EventParticipant();
        currentParticipant.userId = AppContext.context.loginId;
        currentParticipant.profileName = AppContext.context.loginName;
        currentParticipant.acceptanceStatus = AcceptanceStatus.Accepted;
        if (createOrUpdateEvent.eventType == EventType.SHAREMYLOACTION
                || createOrUpdateEvent.eventType == EventType.QUIK) {
            currentParticipant.isUserLocationShared = true;
        }
        createOrUpdateEvent.setCurrentParticipant(currentParticipant);

    }

    protected void populateEventData() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createOrUpdateEvent.startTimeInDateFormat);
        calendar.add(Calendar.MINUTE, createOrUpdateEvent.duration.getOffSetInMinutes());
        createOrUpdateEvent.endTimeInDateFormat = calendar.getTime();
        createOrUpdateEvent.state = EventState.TRACKING_ON;
        createOrUpdateEvent.trackingState = EventState.TRACKING_ON;
        createOrUpdateEvent.isTrackingRequired = true;

        //createOrUpdateEvent.eventType = EventType.getEventType(mEventTypeItem.getImageIndex());
        EventParticipant participant = null;
        setReminderOffset();
        setTrackingOffset();
        createOrUpdateEvent.participants =  new ArrayList<>();
        for (ContactOrGroup cg : createOrUpdateEvent.ContactOrGroups) {
            participant = new EventParticipant();
            participant.userId = cg.getUserId();
            participant.mobileNumber = cg.getMobileNumber();
            participant.contactOrGroup = cg;
            createOrUpdateEvent.participants.add(participant);

        }
        createOrUpdateEvent.participants.add(createOrUpdateEvent.getCurrentParticipant());

        //createOrUpdateEvent.ReminderType = (mReminder.getNotificationType());

    }

}
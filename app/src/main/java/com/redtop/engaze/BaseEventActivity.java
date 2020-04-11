package com.redtop.engaze;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.Interface.OnEventSaveCompleteListner;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.EventState;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.common.enums.TrackingType;
import com.redtop.engaze.common.utility.AppLocationService;
import com.redtop.engaze.common.cache.DestinationCacher;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.domain.NameImageItem;
import com.redtop.engaze.domain.Reminder;
import com.redtop.engaze.domain.manager.EventManager;

import androidx.core.graphics.drawable.DrawableCompat;

public abstract class BaseEventActivity extends BaseActivity {
    protected int mDurationTime = 0;

    protected TextView mQuickEventNameView;
    protected TextView mEventLocationTextView;
    protected NameImageItem mEventTypeItem;
    protected TextView mDurationTextView;

    protected AppLocationService appLocationService;
    protected ImageView mEventTypeView;
    protected String TAG;
    protected AlertDialog mAlertDialog;
    protected JSONObject mEventJobj;
    protected Boolean mFromEventsActivity = true;    //For Recurrence
    protected String mIsRecurrence = "false";
    public Event notificationselectedEvent;

    protected static final int REMINDER_REQUEST_CODE = 2;
    protected static final int EVENT_TYPE_REQUEST_CODE = 6;
    protected static final int TRACKING_REQUEST_CODE = 3;
    protected static final int DURATION_REQUEST_CODE = 5;
    protected static final int LOCATION_REQUEST_CODE = 7;

    protected Event createOrUpdateEvent;
    protected String mCreateUpdateSuccessfulMessage;

    protected Date startDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appLocationService = new AppLocationService(this, this);
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
                    createOrUpdateEvent.Reminder = data.getParcelableExtra("com.redtop.engaze.entity.Reminder");
                    SetReminderOffset();
                    SetReminderText();
                    break;
                case TRACKING_REQUEST_CODE:
                    createOrUpdateEvent.Tracking = data.getParcelableExtra("com.redtop.engaze.entity.Tracking");
                    SetTrackingOffset();
                    SetTrackingText();
                    break;

                case DURATION_REQUEST_CODE:
                    createOrUpdateEvent.Duration = data.getParcelableExtra("com.redtop.engaze.entity.Duration");
                    SetDurationOffset();
                    SetDurationText();
                    break;

                case LOCATION_REQUEST_CODE:
                    createOrUpdateEvent.Destination = data.getParcelableExtra("DestinatonPlace");
                    appLocationService.displayPlace(createOrUpdateEvent.Destination, mEventLocationTextView);

                    break;

            }
        }
    }

    protected void SetDurationOffset() {
        switch (createOrUpdateEvent.Duration.getPeriod()) {
            case "minute":
                createOrUpdateEvent.Duration.OffsetInMinutes = createOrUpdateEvent.Duration.getTimeInterval();
                break;
            case "hour":
                createOrUpdateEvent.Duration.OffsetInMinutes = createOrUpdateEvent.Duration.getTimeInterval() * 60;
                break;
            case "day":
                createOrUpdateEvent.Duration.OffsetInMinutes = createOrUpdateEvent.Duration.getTimeInterval() * 60 * 24;
                break;
            case "week":
                createOrUpdateEvent.Duration.OffsetInMinutes = createOrUpdateEvent.Duration.getTimeInterval() * 60 * 24 * 7;
                break;
        }
    }

    protected void SetDurationText() {
        if (createOrUpdateEvent.Duration != null) {
            mDurationTime = createOrUpdateEvent.Duration.getTimeInterval();
            String holder = "";

            holder = DateUtil.getDurationText(createOrUpdateEvent.Duration.OffsetInMinutes).toLowerCase();
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

        if (createOrUpdateEvent.Reminder != null) {
            switch (createOrUpdateEvent.Reminder.getPeriod()) {
                case "minute":
                    createOrUpdateEvent.Reminder.ReminderOffsetInMinute
                            = createOrUpdateEvent.Reminder.getTimeInterval();
                    break;
                case "hour":
                    createOrUpdateEvent.Reminder.ReminderOffsetInMinute
                            = createOrUpdateEvent.Reminder.getTimeInterval() * 60;
                    break;
                case "day":
                    createOrUpdateEvent.Reminder.ReminderOffsetInMinute
                            = createOrUpdateEvent.Reminder.getTimeInterval() * 60 * 24;
                    break;
                case "week":
                    createOrUpdateEvent.Reminder.ReminderOffsetInMinute
                            = createOrUpdateEvent.Reminder.getTimeInterval() * 60 * 24 * 7;
                    break;
            }
        }
    }

    protected void SetReminderText() {
        TextView reminderOffsetText = (TextView) findViewById(R.id.ReminderOffeset);

        if (createOrUpdateEvent.Reminder != null) {
            String reminderText = "";

            reminderText = DateUtil.getDurationText(createOrUpdateEvent.Reminder.ReminderOffsetInMinute)
                    .toLowerCase();
            if (reminderText.equals("0")) {
                reminderText = "0 minute";

            } else if (!(reminderText.contains("minutes") || reminderText.contains("minute"))) {
                reminderText = reminderText.replace("min", "minute");
                reminderText = reminderText.replace("mins", "minutes");
            }
            reminderText += " before through " + createOrUpdateEvent.Reminder.getNotificationType();

            reminderOffsetText.setText(reminderText);

        }
    }

    protected void SetTrackingOffset() {
        switch (createOrUpdateEvent.Tracking.getPeriod()) {
            case "minute":
                createOrUpdateEvent.Tracking.OffsetInMinutes = createOrUpdateEvent.Tracking.getTimeInterval();
                break;
            case "hour":
                createOrUpdateEvent.Tracking.OffsetInMinutes = createOrUpdateEvent.Tracking.getTimeInterval() * 60;
                break;
            case "day":
                createOrUpdateEvent.Tracking.OffsetInMinutes = createOrUpdateEvent.Tracking.getTimeInterval() * 60 * 24;
                break;
            case "week":
                createOrUpdateEvent.Tracking.OffsetInMinutes = createOrUpdateEvent.Tracking.getTimeInterval() * 60 * 24 * 7;
                break;
        }

    }

    protected void SetTrackingText() {
        TextView trackingOffsettext = (TextView) findViewById(R.id.TrackingStartOffeset);

        if (createOrUpdateEvent.Tracking != null) {
            String trackingText = "";


            trackingText = DateUtil.getDurationText(createOrUpdateEvent.Tracking.OffsetInMinutes).toLowerCase();
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
        startCal.setTime(createOrUpdateEvent.StartTimeInDateFormat);
        long diffMinutes = (startCal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 60000;

        if (trackingOffset > diffMinutes) {
            trackingOffset = diffMinutes;
        }
        createOrUpdateEvent.TrackingStartOffset = (int) trackingOffset;
    }

    private void setReminderOffset() {

        long reminderOffset = 0;
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(createOrUpdateEvent.StartTimeInDateFormat);
        long diffMinutes = (startCal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 60000;
        if (reminderOffset > diffMinutes) {
            reminderOffset = diffMinutes;
        }

        createOrUpdateEvent.ReminderOffset = (int) reminderOffset;

    }

    protected void saveEvent(final Boolean isMeetNow) {

        showProgressBar(getResources().getString(R.string.message_general_progressDialog));

        EventManager.saveEvent(createOrUpdateEvent, isMeetNow, createOrUpdateEvent.Reminder, new OnEventSaveCompleteListner() {

            @Override
            public void eventSaveComplete(Event event) {
                Toast.makeText(getApplicationContext(),
                        mCreateUpdateSuccessfulMessage,
                        Toast.LENGTH_LONG).show();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (createOrUpdateEvent.Destination != null) {//when event is created without destination
                            DestinationCacher.cacheDestination(createOrUpdateEvent.Destination, mContext);
                        }
                    }
                });

                if (createOrUpdateEvent.EventType == EventType.SHAREMYLOACTION) {
                    gotoHomePage();
                } else if (createOrUpdateEvent.EventType == EventType.TRACKBUDDY ||
                        createOrUpdateEvent.EventType == EventType.QUIK) {
                    gotoTrackingPage(event.EventId);
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

    protected void populateEventData() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createOrUpdateEvent.StartTimeInDateFormat);
        calendar.add(Calendar.MINUTE, createOrUpdateEvent.Duration.OffsetInMinutes);
        createOrUpdateEvent.EndTimeInDateFormat = calendar.getTime();
        createOrUpdateEvent.InitiatorId = AppContext.context.loginId;
        createOrUpdateEvent.State = EventState.TRACKING_ON;
        createOrUpdateEvent.TrackingState = EventState.TRACKING_ON;
        createOrUpdateEvent.IsTrackingRequired = true;

        createOrUpdateEvent.EventType = EventType.getEventType(mEventTypeItem.getImageIndex());
        EventParticipant participant = null;
        setReminderOffset();
        setTrackingOffset();
        for (ContactOrGroup cg : createOrUpdateEvent.ContactOrGroups) {
            participant = new EventParticipant();
            participant.setUserId(cg.getUserId());
            participant.setMobileNumber(cg.getMobileNumber());
            createOrUpdateEvent.Participants.add(participant);
        }

        /*createOrUpdateEvent.ReminderType = (mReminder.getNotificationType());
        createOrUpdateEvent.Destination = mDestinationPlace;
*/
    }

}
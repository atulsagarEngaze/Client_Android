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
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.domain.NameImageItem;
import com.redtop.engaze.domain.Reminder;
import com.redtop.engaze.domain.manager.EventManager;

import androidx.core.graphics.drawable.DrawableCompat;

public abstract class BaseEventActivity extends BaseActivity {
    protected int mDurationTime = 0;
    protected int mDurationOffset;
    protected TextView mQuickEventName;


    protected TextView mEventLocationTextView;
    protected NameImageItem mEventTypeItem;


    protected TextView mDurationTextView;
    protected EventPlace mDestinationPlace;
    protected AppLocationService appLocationService;
    protected ImageView mEventTypeView;
    protected ArrayList<ContactOrGroup> mContactsAndgroups;
    protected String TAG;
    protected AlertDialog mAlertDialog;
    protected long mTrackingOffset = 0;
    protected long mReminderOffset = 0;


    protected JSONObject mEventJobj;
    protected Boolean mFromEventsActivity = true;
    //For Recurrence
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
                    createOrUpdateEvent.Reminder = (Reminder) data.getParcelableExtra("com.redtop.engaze.entity.Reminder");
                    SetReminderText(createOrUpdateEvent.Reminder);
                    break;
                case TRACKING_REQUEST_CODE:
                    createOrUpdateEvent.Tracking = (Duration) data.getParcelableExtra("com.redtop.engaze.entity.Tracking");
                    SetTrackingText(createOrUpdateEvent.Tracking);
                    break;

                case DURATION_REQUEST_CODE:
                    createOrUpdateEvent.Duration = (Duration) data.getParcelableExtra("com.redtop.engaze.entity.Duration");
                    SetDurationText(createOrUpdateEvent.Duration);
                    break;

                case LOCATION_REQUEST_CODE:
                    mDestinationPlace = data.getParcelableExtra("DestinatonPlace");
                    appLocationService.displayPlace(mDestinationPlace, mEventLocationTextView);

                    break;

            }
        }
    }

    protected void SetDurationText(Duration duration) {
        if (duration != null) {
            mDurationTime = duration.getTimeInterval();
            String holder = "";
            switch (duration.getPeriod()) {
                case "minute":
                    mDurationOffset = duration.getTimeInterval();
                    break;
                case "hour":
                    mDurationOffset = duration.getTimeInterval() * 60;
                    break;
                case "day":
                    mDurationOffset = duration.getTimeInterval() * 60 * 24;
                    break;
                case "week":
                    mDurationOffset = duration.getTimeInterval() * 60 * 24 * 7;
                    break;
            }
            holder = DateUtil.getDurationText(mDurationOffset).toLowerCase();
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

    protected void SetReminderText(Reminder reminder) {
        TextView reminderOffsettext = (TextView) findViewById(R.id.ReminderOffeset);

        if (reminder != null) {
            String reminderText = "";
            switch (reminder.getPeriod()) {
                case "minute":
                    mReminderOffset = reminder.getTimeInterval();
                    break;
                case "hour":
                    mReminderOffset = reminder.getTimeInterval() * 60;
                    break;
                case "day":
                    mReminderOffset = reminder.getTimeInterval() * 60 * 24;
                    break;
                case "week":
                    mReminderOffset = reminder.getTimeInterval() * 60 * 24 * 7;
                    break;
            }

            reminderText = DateUtil.getDurationText(mReminderOffset).toLowerCase();
            if (reminderText.equals("0")) {
                reminderText = "0 minute";

            } else if (!(reminderText.contains("minutes") || reminderText.contains("minute"))) {
                reminderText = reminderText.replace("min", "minute");
                reminderText = reminderText.replace("mins", "minutes");
            }
            reminderText += " before through " + reminder.getNotificationType();

            reminderOffsettext.setText(reminderText);

        } else {
            Log.d(TAG, "insdie else");
        }

    }

    protected void SetTrackingText(Duration tracking) {
        TextView trackingOffsettext = (TextView) findViewById(R.id.TrackingStartOffeset);

        if (tracking != null) {
            String trackingText = "";
            switch (tracking.getPeriod()) {
                case "minute":
                    mTrackingOffset = tracking.getTimeInterval();
                    break;
                case "hour":
                    mTrackingOffset = tracking.getTimeInterval() * 60;
                    break;
                case "day":
                    mTrackingOffset = tracking.getTimeInterval() * 60 * 24;
                    break;
                case "week":
                    mTrackingOffset = tracking.getTimeInterval() * 60 * 24 * 7;
                    break;
            }

            trackingText = DateUtil.getDurationText(mTrackingOffset).toLowerCase();
            if (trackingText.equals("0")) {
                trackingText = "0 minute";

            } else if (!(trackingText.contains("minutes") || trackingText.contains("minute"))) {
                trackingText = trackingText.replace("min", "minute");
                trackingText = trackingText.replace("mins", "minutes");
            }

            trackingText += " before";
            trackingOffsettext.setText(trackingText);

        } else {
            Log.d(TAG, "inside else");
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
        startCal.setTime(startDate);
        long diffMinutes = (startCal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 60000;

        if (trackingOffset > diffMinutes) {
            trackingOffset = diffMinutes;
        }
        createOrUpdateEvent.TrackingStartOffset = (int) trackingOffset;
    }

    private void setReminderOffset() {

        long reminderOffset = 0;
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        long diffMinutes = (startCal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 60000;
        if (reminderOffset > diffMinutes) {
            reminderOffset = diffMinutes;
        }

        createOrUpdateEvent.ReminderOffset = (int) reminderOffset;

    }

    protected void saveEvent(final Boolean isMeetNow) {

        showProgressBar(getResources().getString(R.string.message_general_progressDialog));
        try {
            EventManager.saveEvent(new JSONObject(AppContext.jsonParser.Serialize(createOrUpdateEvent)), isMeetNow, createOrUpdateEvent.Reminder, new OnEventSaveCompleteListner() {

                @Override
                public void eventSaveComplete(Event event) {
                    Toast.makeText(getApplicationContext(),
                            mCreateUpdateSuccessfulMessage,
                            Toast.LENGTH_LONG).show();
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (mDestinationPlace != null) {//when event is created without destination
                                DestinationCacher.cacheDestination(mDestinationPlace, mContext);
                            }
                        }
                    });
                    try {
                        if (mEventJobj.getInt("EventTypeId") == 100) {
                            gotoHomePage();
                        } else if (mEventJobj.getInt("EventTypeId") == 200) {
                            gotoTrackingPage(event.EventId);
                        } else if (isMeetNow) {
                            gotoTrackingPage(event.EventId);
                        } else {
                            gotoEventsPage();
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }, AppContext.actionHandler);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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

        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");

        createOrUpdateEvent.StartTime = DateUtil.convertToUtcDateTime(parseFormat.format(createOrUpdateEvent.StartTime), parseFormat);

        Date endDate = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.MINUTE, mDurationOffset);
        endDate = calendar.getTime();
        createOrUpdateEvent.EndTime = DateUtil.convertToUtcDateTime(parseFormat.format(endDate), parseFormat);//parseFormat.format(endDate);

        createOrUpdateEvent.InitiatorId = AppContext.context.loginId;
        createOrUpdateEvent.Duration.setTimeInterval(mDurationOffset);
        createOrUpdateEvent.State = EventState.TRACKING_ON;
        createOrUpdateEvent.TrackingState = EventState.TRACKING_ON;
        createOrUpdateEvent.IsTrackingRequired = true;
        createOrUpdateEvent.EventType = EventType.getEventType(mEventTypeItem.getImageIndex());
        createOrUpdateEvent.ContactOrGroups = mContactsAndgroups;
        setReminderOffset();
        setTrackingOffset();
        /*createOrUpdateEvent.ReminderType = (mReminder.getNotificationType());
        createOrUpdateEvent.Destination = mDestinationPlace;
*/
    }

}
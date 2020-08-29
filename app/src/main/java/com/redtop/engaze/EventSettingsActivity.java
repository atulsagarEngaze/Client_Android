package com.redtop.engaze;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.Reminder;

import androidx.appcompat.widget.Toolbar;

@SuppressWarnings("deprecation")
public class EventSettingsActivity extends BaseActivity {

    private ArrayList<TextView> reminderPeriods;
    private ArrayList<TextView> notificationTypes;
    private ArrayList<TextView> trackingPeriods;

    private ArrayList<ImageView> reminderPeriodIcons;
    private ArrayList<ImageView> notificationTypeIcons;
    private ArrayList<ImageView> trackingPeriodIcons;
    private EditText mTextReminderValue;
    private EditText mTextTrackingValue;


    private Reminder mReminder = null;
    private Duration mTracking = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_event_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.event_setting_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitleTextAppearance(this, R.style.toolbarTextFontFamilyStyle);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            getSupportActionBar().setTitle(R.string.title_event_settings);
            toolbar.setNavigationOnClickListener(v -> {
                hideKeyboard(v);
                onBackPressed();
            });
        }
        ImageView icon = null;
        TextView notificationType = null;

        mReminder = AppContext.context.defaultReminderSettings;
        mTracking = AppContext.context.defaultTrackingSettings;
        mTextTrackingValue = (EditText) findViewById(R.id.setting_tracking_value);
        mTextTrackingValue.setText(Integer.toString(mTracking.getTimeInterval()));
        mTextTrackingValue.setSelection(mTextTrackingValue.getText().length());
        mTextTrackingValue.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });

        mTextReminderValue = (EditText) findViewById(R.id.setting_reminder_value);
        mTextReminderValue.setText(Integer.toString(mReminder.getTimeInterval()));
        mTextReminderValue.setSelection(mTextReminderValue.getText().length());
        mTextReminderValue.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });


        notificationTypes = new ArrayList<TextView>();
        notificationTypeIcons = new ArrayList<ImageView>();

        notificationType = (TextView) findViewById(R.id.reminder_alarm);

        icon = (ImageView) findViewById(R.id.icon_reminder_alarm);
        icon.setVisibility(View.GONE);

        setDefaultNotificationType(notificationType, icon);

        notificationTypes.add(notificationType);
        notificationTypeIcons.add(icon);

        notificationType = (TextView) findViewById(R.id.reminder_notification);

        icon = (ImageView) findViewById(R.id.icon_reminder_notification);
        icon.setVisibility(View.GONE);


        notificationTypeIcons.add(icon);

        setDefaultNotificationType(notificationType, icon);
        notificationTypes.add(notificationType);


        for (int i = 0; i < notificationTypes.size(); i++) {
            TextView nt = notificationTypes.get(i);

            nt.setOnClickListener(v -> {
                mTextReminderValue.clearFocus();
                mTextTrackingValue.clearFocus();
                ImageView selectedIcon = null;
                TextView clieckedView = ((TextView) v);
                for (int i13 = 0; i13 < notificationTypes.size(); i13++) {
                    TextView noti = notificationTypes.get(i13);
                    notificationTypeIcons.get(i13);
                    notificationTypeIcons.get(i13).setVisibility(View.GONE);
                    noti.setTextColor(getResources().getColorStateList(R.color.secondaryText));

                    if (noti.getId() == clieckedView.getId()) {
                        selectedIcon = notificationTypeIcons.get(i13);
                    }
                }
                selectedIcon.setVisibility(View.VISIBLE);
                mReminder.setNotificationType(clieckedView.getTag().toString());
                clieckedView.setTextColor(getResources().getColorStateList(R.color.primaryDark));
            });
        }


        TextView period = null;

        reminderPeriods = new ArrayList<>();
        reminderPeriodIcons = new ArrayList<ImageView>();

        period = (TextView) findViewById(R.id.reminder_minute);

        icon = (ImageView) findViewById(R.id.icon_reminder_minute);
        icon.setVisibility(View.GONE);

        setDefaultReminderPeriod(period, icon);

        reminderPeriods.add(period);
        reminderPeriodIcons.add(icon);

        period = (TextView) findViewById(R.id.reminder_hour);

        icon = (ImageView) findViewById(R.id.icon_reminder_hour);
        icon.setVisibility(View.GONE);

        setDefaultReminderPeriod(period, icon);

        reminderPeriods.add(period);
        reminderPeriodIcons.add(icon);

        reminderPeriods.add(period);
        reminderPeriodIcons.add(icon);


        for (int i = 0; i < reminderPeriods.size(); i++) {
            TextView per = reminderPeriods.get(i);

            per.setOnClickListener(v -> {
                mTextReminderValue.clearFocus();
                mTextTrackingValue.clearFocus();
                ImageView selectedIcon = null;
                TextView clieckedView = ((TextView) v);
                for (int i1 = 0; i1 < reminderPeriods.size(); i1++) {
                    TextView dv = reminderPeriods.get(i1);
                    reminderPeriodIcons.get(i1).setVisibility(View.GONE);

                    String duration = dv.getText().toString();
                    if (duration.contains(getResources().getString(R.string.before))) {
                        dv.setText(duration.substring(0, duration.indexOf(getResources().getString(R.string.before))));
                        dv.setTextColor(getResources().getColorStateList(R.color.secondaryText));
                    }
                    if (dv.getId() == clieckedView.getId()) {
                        selectedIcon = reminderPeriodIcons.get(i1);
                    }
                }
                selectedIcon.setVisibility(View.VISIBLE);

                clieckedView.setTextColor(getResources().getColorStateList(R.color.primaryDark));
                clieckedView.setText(clieckedView.getText().toString().concat(getResources().getString(R.string.before)));
                mReminder.setPeriod(clieckedView.getTag().toString());

            });
        }
        ShowHideTrackingSection(mTracking.getTrackingState());
        trackingPeriods = new ArrayList<TextView>();
        trackingPeriodIcons = new ArrayList<ImageView>();

        period = (TextView) findViewById(R.id.tracking_minute);

        icon = (ImageView) findViewById(R.id.icon_tracking_minute);
        icon.setVisibility(View.GONE);

        setDefaultTrackingPeriod(period, icon);

        trackingPeriods.add(period);
        trackingPeriodIcons.add(icon);

        period = (TextView) findViewById(R.id.tracking_hour);

        icon = (ImageView) findViewById(R.id.icon_tracking_hour);
        icon.setVisibility(View.GONE);

        setDefaultTrackingPeriod(period, icon);

        trackingPeriods.add(period);
        trackingPeriodIcons.add(icon);

        for (int i = 0; i < trackingPeriods.size(); i++) {
            TextView per = trackingPeriods.get(i);

            per.setOnClickListener(v -> {
                mTextReminderValue.clearFocus();
                mTextTrackingValue.clearFocus();
                ImageView selectedIcon = null;
                TextView clieckedView = ((TextView) v);
                for (int i12 = 0; i12 < trackingPeriods.size(); i12++) {
                    TextView dv = trackingPeriods.get(i12);
                    trackingPeriodIcons.get(i12).setVisibility(View.GONE);

                    String duration = dv.getText().toString();
                    if (duration.contains(getResources().getString(R.string.before))) {
                        dv.setText(duration.substring(0, duration.indexOf(getResources().getString(R.string.before))));
                        dv.setTextColor(getResources().getColorStateList(R.color.secondaryText));
                    }
                    if (dv.getId() == clieckedView.getId()) {
                        selectedIcon = trackingPeriodIcons.get(i12);
                    }
                }

                // TODO Auto-generated method stub
                selectedIcon.setVisibility(View.VISIBLE);

                clieckedView.setTextColor(getResources().getColorStateList(R.color.primaryDark));
                clieckedView.setText(clieckedView.getText().toString().concat(getResources().getString(R.string.before)));
                mTracking.setPeriod(clieckedView.getTag().toString());
            });
        }
    }

    @Override
    public void onBackPressed() {
        String reminder = mTextReminderValue.getText().toString();
        String tracking = mTextTrackingValue.getText().toString();

        if (!reminder.isEmpty() && !tracking.isEmpty()) {
            try {
                mReminder.setTimeInterval(Integer.parseInt(reminder));
                mTracking.setTimeInterval(Integer.parseInt(tracking));
                if (Reminder.validateReminderInput(mReminder) && Duration.validateTrackingInput(mTracking)) {
                    PreffManager.setPrefObject(Constants.DEFAULT_REMINDER_PREF_KEY, mReminder);
                    PreffManager.setPrefObject(Constants.DEFAULT_TRACKING_PREF_KEY, mTracking);
                    this.finish();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getBaseContext(),
                        getResources().getString(R.string.message_createEvent_reminderMaxAlert),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getBaseContext(),
                    getResources().getString(R.string.event_invalid_input_message),
                    Toast.LENGTH_LONG).show();
        }
    }

    ;

    private void ShowHideTrackingSection(Boolean state) {
        if (state) {
            findViewById(R.id.row_tracking_value).setVisibility(View.VISIBLE);
            findViewById(R.id.row_tracking_minute).setVisibility(View.VISIBLE);
            findViewById(R.id.row_tracking_hour).setVisibility(View.VISIBLE);
            findViewById(R.id.row_tracking_hour).setVisibility(View.VISIBLE);
            findViewById(R.id.row_setting_tracking_value_end_border).setVisibility(View.VISIBLE);
            findViewById(R.id.row_setting_tracking_value_start_border).setVisibility(View.VISIBLE);


        } else {
            findViewById(R.id.row_tracking_value).setVisibility(View.GONE);
            findViewById(R.id.row_tracking_minute).setVisibility(View.GONE);
            findViewById(R.id.row_tracking_hour).setVisibility(View.GONE);
            findViewById(R.id.row_setting_tracking_value_end_border).setVisibility(View.GONE);
            findViewById(R.id.row_setting_tracking_value_start_border).setVisibility(View.GONE);
        }

    }

    private void setDefaultTrackingPeriod(TextView period, ImageView icon) {
        if (period.getTag().equals(mTracking.getPeriod())) {
            period.setText(period.getText().toString().concat(getResources().getString(R.string.before)));
            period.setTextColor(getResources().getColorStateList(R.color.primaryDark));
            icon.setVisibility(View.VISIBLE);
        }
    }

    private void setDefaultReminderPeriod(TextView period, ImageView icon) {
        if (period.getTag().equals(mReminder.getPeriod())) {
            period.setText(period.getText().toString().concat(getResources().getString(R.string.before)));
            period.setTextColor(getResources().getColorStateList(R.color.primaryDark));
            icon.setVisibility(View.VISIBLE);
        }
    }

    private void setDefaultNotificationType(TextView notificationType, ImageView icon) {
        if (notificationType.getTag().equals(mReminder.getNotificationType())) {
            notificationType.setTextColor(getResources().getColorStateList(R.color.primaryDark));
            icon.setVisibility(View.VISIBLE);
        }
    }
}

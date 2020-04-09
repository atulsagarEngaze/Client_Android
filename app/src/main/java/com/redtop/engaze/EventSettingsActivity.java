package com.redtop.engaze;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.Duration;
import com.redtop.engaze.domain.Reminder;

import androidx.appcompat.widget.Toolbar;

@SuppressWarnings("deprecation")
public class EventSettingsActivity extends BaseActivity {

	private ArrayList<TextView> reminderPeriods;	
	private ArrayList<TextView>notificationTypes;
	private ArrayList<TextView>trackingPeriods;

	private ArrayList<ImageView> reminderPeriodIcons;
	private ArrayList<ImageView>notificationTypeIcons;
	private ArrayList<ImageView>trackingPeriodIcons;
	private EditText mTextReminderValue ;
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
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
			getSupportActionBar().setTitle(R.string.title_event_settings);
			//toolbar.setSubtitle(R.string.title_event);
			toolbar.setNavigationOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});
		}
		ImageView icon = null;		
		TextView notificationType = null;


		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String strReminderInterval = sharedPrefs.getString("ReminderInterval", getResources().getString(R.string.event_reminder_default_interval));
		mReminder = new Reminder(Integer.parseInt(strReminderInterval), sharedPrefs.getString("ReminderPeriod", getResources().getString(R.string.event_reminder_default_period)), sharedPrefs.getString("ReminderNotification", getResources().getString(R.string.event_reminder_default_notification)));
		mTracking = new Duration(Integer.parseInt(sharedPrefs.getString("TrackingInterval", getResources().getString(R.string.event_tracking_default_interval))), sharedPrefs.getString("TrackingPeriod", getResources().getString(R.string.event_tracking_default_period)), Boolean.parseBoolean( sharedPrefs.getString("TrackingEnabled", getResources().getString(R.string.event_tracking_default_enabled))));
		mTextTrackingValue = (EditText)findViewById(R.id.setting_tracking_value);
		mTextTrackingValue.setText(Integer.toString(mTracking.getTimeInterval()));
		mTextTrackingValue.setSelection(mTextTrackingValue.getText().length());
		mTextTrackingValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					hideKeyboard(v);
				}
			}
		});

		mTextReminderValue = (EditText)findViewById(R.id.setting_reminder_value);
		mTextReminderValue.setText(Integer.toString(mReminder.getTimeInterval()));
		mTextReminderValue.setSelection(mTextReminderValue.getText().length());
		mTextReminderValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					hideKeyboard(v);
				}
			}
		});



		notificationTypes= new ArrayList<TextView>();
		notificationTypeIcons = new ArrayList<ImageView>();

		notificationType = (TextView)findViewById(R.id.reminder_alarm);

		icon = (ImageView)findViewById(R.id.icon_reminder_alarm);
		icon.setVisibility(View.GONE);					

		setDefaultNotificationType(notificationType,icon);

		notificationTypes.add(notificationType);
		notificationTypeIcons.add(icon);

		notificationType = (TextView)findViewById(R.id.reminder_notification);

		icon = (ImageView)findViewById(R.id.icon_reminder_notification);
		icon.setVisibility(View.GONE);


		notificationTypeIcons.add(icon);

		setDefaultNotificationType(notificationType,icon);	    
		notificationTypes.add(notificationType);


		for(int i=0;i<notificationTypes.size();i++){
			TextView nt = notificationTypes.get(i);

			nt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mTextReminderValue.clearFocus();
					mTextTrackingValue.clearFocus();
					ImageView selectedIcon = null;
					TextView  clieckedView = ((TextView)v);	
					for(int i=0;i<notificationTypes.size();i++){
						TextView noti = notificationTypes.get(i);					
						notificationTypeIcons.get(i);
						notificationTypeIcons.get(i).setVisibility(View.GONE);
						noti.setTextColor(getResources().getColorStateList(R.color.secondaryText));

						if(noti.getId()==clieckedView.getId())
						{
							selectedIcon = notificationTypeIcons.get(i);
						}
					}
					selectedIcon.setVisibility(View.VISIBLE);
					mReminder.setNotificationType(clieckedView.getTag().toString());
					clieckedView.setTextColor(getResources().getColorStateList(R.color.primaryDark));
					PreffManager.setPref("ReminderNotification", clieckedView.getTag().toString());

				}
			});

		}


		TextView period = null;

		reminderPeriods = new ArrayList<TextView>();
		reminderPeriodIcons = new ArrayList<ImageView>();

		period = (TextView)findViewById(R.id.reminder_minute);

		icon = (ImageView)findViewById(R.id.icon_reminder_minute);
		icon.setVisibility(View.GONE);	

		setDefaultReminderPeriod(period, icon);	

		reminderPeriods.add(period);
		reminderPeriodIcons.add(icon);

		period = (TextView)findViewById(R.id.reminder_hour);

		icon = (ImageView)findViewById(R.id.icon_reminder_hour);
		icon.setVisibility(View.GONE);	

		setDefaultReminderPeriod(period, icon);

		reminderPeriods.add(period);
		reminderPeriodIcons.add(icon);

		reminderPeriods.add(period);
		reminderPeriodIcons.add(icon);


		for(int i=0;i<reminderPeriods.size();i++){
			TextView per = reminderPeriods.get(i);

			per.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mTextReminderValue.clearFocus();
					mTextTrackingValue.clearFocus();
					ImageView selectedIcon = null;
					TextView  clieckedView = ((TextView)v);	
					for(int i=0;i<reminderPeriods.size();i++){
						TextView dv = reminderPeriods.get(i);
						reminderPeriodIcons.get(i).setVisibility(View.GONE);

						String duration = dv.getText().toString();
						if(duration.contains(getResources().getString(R.string.before)))
						{
							dv.setText(duration.substring(0, duration.indexOf(getResources().getString(R.string.before))));
							dv.setTextColor(getResources().getColorStateList(R.color.secondaryText));
						}
						if(dv.getId()==clieckedView.getId())
						{
							selectedIcon = reminderPeriodIcons.get(i);
						}
					}

					// TODO Auto-generated method stub
					selectedIcon.setVisibility(View.VISIBLE);

					clieckedView.setTextColor(getResources().getColorStateList(R.color.primaryDark));
					clieckedView.setText(clieckedView.getText().toString().concat(getResources().getString(R.string.before)));
					mReminder.setPeriod(clieckedView.getTag().toString());					
					PreffManager.setPref("ReminderPeriod", clieckedView.getTag().toString());
				}
			});					    
		}

		//Switch onOffSwitch = (Switch)  findViewById(R.id.setting_tracking_switch); 
		//onOffSwitch.setChecked(tracking.getTrackingState());
		ShowHideTrackingSection(mTracking.getTrackingState());

		//		onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		//
		//			@Override
		//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {				
		//				ShowHideTrackingSection(isChecked);
		//				tracking.setTrackingState(isChecked);
		//				AppUtility.setPref("TrackingEnabled", Boolean.toString(isChecked), getApplicationContext());
		//			}  
		//		});

		trackingPeriods = new ArrayList<TextView>();
		trackingPeriodIcons = new ArrayList<ImageView>();

		period = (TextView)findViewById(R.id.tracking_minute);

		icon = (ImageView)findViewById(R.id.icon_tracking_minute);
		icon.setVisibility(View.GONE);

		setDefaultTrackingPeriod(period, icon);	

		trackingPeriods.add(period);
		trackingPeriodIcons.add(icon);

		period = (TextView)findViewById(R.id.tracking_hour);

		icon = (ImageView)findViewById(R.id.icon_tracking_hour);
		icon.setVisibility(View.GONE);

		setDefaultTrackingPeriod(period, icon);

		trackingPeriods.add(period);
		trackingPeriodIcons.add(icon);

		for(int i=0;i<trackingPeriods.size();i++){
			TextView per = trackingPeriods.get(i);

			per.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mTextReminderValue.clearFocus();
					mTextTrackingValue.clearFocus();
					ImageView selectedIcon = null;
					TextView  clieckedView = ((TextView)v);	
					for(int i=0;i<trackingPeriods.size();i++){
						TextView dv = trackingPeriods.get(i);
						trackingPeriodIcons.get(i).setVisibility(View.GONE);

						String duration = dv.getText().toString();
						if(duration.contains(getResources().getString(R.string.before)))
						{
							dv.setText(duration.substring(0, duration.indexOf(getResources().getString(R.string.before))));
							dv.setTextColor(getResources().getColorStateList(R.color.secondaryText));
						}
						if(dv.getId()==clieckedView.getId())
						{
							selectedIcon = trackingPeriodIcons.get(i);
						}
					}

					// TODO Auto-generated method stub
					selectedIcon.setVisibility(View.VISIBLE);

					clieckedView.setTextColor(getResources().getColorStateList(R.color.primaryDark));
					clieckedView.setText(clieckedView.getText().toString().concat(getResources().getString(R.string.before)));
					mTracking.setPeriod(clieckedView.getTag().toString());					
					PreffManager.setPref("TrackingPeriod", clieckedView.getTag().toString());
				}
			});					    
		}		
	}

	@Override
	public void onBackPressed() {
		String reminder = mTextReminderValue.getText().toString();
		String tracking = mTextTrackingValue.getText().toString();			

		if(!reminder.isEmpty() && !tracking.isEmpty()){
			try{
				mReminder.setTimeInterval(Integer.parseInt(reminder));
				mTracking.setTimeInterval(Integer.parseInt(tracking));
				if(Reminder.validateReminderInput(mReminder) && Duration.validateTrackingInput(mTracking)){
					PreffManager.setPref("ReminderInterval", reminder);
					PreffManager.setPref("TrackingInterval", tracking);
					this.finish();
				}
			}catch(NumberFormatException e){
				Toast.makeText(getBaseContext(),							
						getResources().getString(R.string.message_createEvent_reminderMaxAlert),
						Toast.LENGTH_LONG).show();
			}	
		}
		else{
			Toast.makeText(getBaseContext(),							
					getResources().getString(R.string.event_invalid_input_message),
					Toast.LENGTH_LONG).show();
		}			
	};

	private void ShowHideTrackingSection(Boolean state)
	{
		if(state)
		{						
			findViewById(R.id.row_tracking_value).setVisibility(View.VISIBLE);
			findViewById(R.id.row_tracking_minute).setVisibility(View.VISIBLE);
			findViewById(R.id.row_tracking_hour).setVisibility(View.VISIBLE);
			findViewById(R.id.row_tracking_hour).setVisibility(View.VISIBLE);
			findViewById(R.id.row_setting_tracking_value_end_border).setVisibility(View.VISIBLE);
			findViewById(R.id.row_setting_tracking_value_start_border).setVisibility(View.VISIBLE);


		}
		else
		{
			findViewById(R.id.row_tracking_value).setVisibility(View.GONE);
			findViewById(R.id.row_tracking_minute).setVisibility(View.GONE);
			findViewById(R.id.row_tracking_hour).setVisibility(View.GONE);
			findViewById(R.id.row_setting_tracking_value_end_border).setVisibility(View.GONE);
			findViewById(R.id.row_setting_tracking_value_start_border).setVisibility(View.GONE);
		}

	}

	private void setDefaultTrackingPeriod(TextView period, ImageView icon) {
		if(period.getTag().equals(mTracking.getPeriod()))
		{		   
			period.setText(period.getText().toString().concat(getResources().getString(R.string.before)));
			period.setTextColor(getResources().getColorStateList(R.color.primaryDark));
			icon.setVisibility(View.VISIBLE);
		}	
	}

	private void setDefaultReminderPeriod(TextView period, ImageView icon) {
		if(period.getTag().equals(mReminder.getPeriod()))
		{		   
			period.setText(period.getText().toString().concat(getResources().getString(R.string.before)));
			period.setTextColor(getResources().getColorStateList(R.color.primaryDark));
			icon.setVisibility(View.VISIBLE);
		}	
	}

	private void setDefaultNotificationType(TextView notificationType, ImageView icon) {
		if(notificationType.getTag().equals(mReminder.getNotificationType()))
		{
			// TODO Auto-generated method stub
			notificationType.setTextColor(getResources().getColorStateList(R.color.primaryDark));
			icon.setVisibility(View.VISIBLE);
		}
	}

}

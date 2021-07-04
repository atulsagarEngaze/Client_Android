package com.redtop.engaze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class EventRecurrenceInfo extends Activity {
	private LinearLayout mLlDailySettings, mLlWeekySettings, mLlMonthlySettings;	
	private HashMap<Integer, String> mWeekDays;
	private TextView mPattern, mDays;
	protected String mRecurrenceType;
	protected String mNumberOfOccurences;
	protected String mFrequencyOfOcuurence;
	protected String mRecurrenceDayOfMonth;
	protected ArrayList<Integer>mRecurrencedays;
	/** Called when the activity is first created. */
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recurrenceinfo);
		//tvHeader  = (TextView)findViewById(R.id.ChooseCategoryHeader);
		mRecurrencedays = (ArrayList<Integer>) this.getIntent().getSerializableExtra("Recurrencedays");		
		mRecurrenceType = this.getIntent().getStringExtra("RecurrenceType");
		mNumberOfOccurences = this.getIntent().getStringExtra("NumberOfOccurences");
		mFrequencyOfOcuurence = this.getIntent().getStringExtra("FrequencyOfOcuurence");		
		mRecurrenceDayOfMonth = this.getIntent().getStringExtra("RecurrenceDayOfMonth");
		mLlDailySettings = (LinearLayout)findViewById(R.id.ll_daily_settings);
		mLlWeekySettings = (LinearLayout)findViewById(R.id.ll_weekly_settings);
		mLlMonthlySettings = (LinearLayout)findViewById(R.id.ll_monthly_settings);
		mPattern = (TextView)findViewById(R.id.pattern);
		mDays = (TextView)findViewById(R.id.txtDays);

		populateEventRecurrenceData();
	}

	private void populateEventRecurrenceData() {		
		mWeekDays = new HashMap<>();
		mWeekDays.put(1, "Sun");
		mWeekDays.put(2, "Mon");
		mWeekDays.put(3, "Tues");
		mWeekDays.put(4, "Wednes");
		mWeekDays.put(5, "Thurs");
		mWeekDays.put(6, "Fri");
		mWeekDays.put(7, "Sat");

		if(mRecurrenceType.equals("1")){			
			mPattern.setText(getResources().getString(R.string.label_daily));			
			String dailyText = String.format(getResources().getString(R.string.label_daily_occurrences), mFrequencyOfOcuurence);
			((TextView)findViewById(R.id.day_frequency_input)).setText(Html.fromHtml(dailyText));
			setDailyLayoutVisible();
		}
		else if(mRecurrenceType.equals("2")){			
			mPattern.setText(getResources().getString(R.string.label_weekly));				
			String weeklyText = String.format(getResources().getString(R.string.label_weekly_occurrences), mFrequencyOfOcuurence);
			((TextView)findViewById(R.id.week_frequency_input)).setText(Html.fromHtml(weeklyText));
			setWeeklyLayoutVisible();

			String daysText ="";
			Collections.sort(mRecurrencedays);
			for(int day : mRecurrencedays){				
				daysText =  daysText + ", " + mWeekDays.get(day);		
			}

			daysText = daysText.substring(1);
			String weekDays = String.format(getResources().getString(R.string.label_weekly_days), daysText); 
			mDays.setText(Html.fromHtml(weekDays));
		}
		else{			
			mPattern.setText(getResources().getString(R.string.label_monthly));

			String monthlyText = String.format(getResources().getString(R.string.label_monthly_occurrences), mRecurrenceDayOfMonth, mFrequencyOfOcuurence);
			((TextView)findViewById(R.id.month_frequency_input)).setText(Html.fromHtml(monthlyText));
			setMonthlyLayoutVisible();
		}	

		String endAfterOccurrences = String.format(getResources().getString(R.string.label_end_after_occurrence), mNumberOfOccurences);
		((TextView)findViewById(R.id.occurece_input)).setText(Html.fromHtml(endAfterOccurrences));
	}

	private void setDailyLayoutVisible(){
		mLlDailySettings.setVisibility(View.VISIBLE);
		mLlWeekySettings.setVisibility(View.GONE);
		mLlMonthlySettings.setVisibility(View.GONE);
	}

	private void setWeeklyLayoutVisible(){
		mLlDailySettings.setVisibility(View.GONE);
		mLlWeekySettings.setVisibility(View.VISIBLE);
		mLlMonthlySettings.setVisibility(View.GONE);
	}

	private void setMonthlyLayoutVisible(){
		mLlDailySettings.setVisibility(View.GONE);
		mLlWeekySettings.setVisibility(View.GONE);
		mLlMonthlySettings.setVisibility(View.VISIBLE);
	}
}

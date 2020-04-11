package com.redtop.engaze;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.domain.Duration;
public class TrackingOffset extends Activity {

	private ArrayList<TextView> periods;

	private Duration tracking = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tracking_start_offset);
		tracking = (Duration)this.getIntent().getParcelableExtra("com.redtop.engaze.entity.Tracking");

		Button save = (Button)findViewById(R.id.save_event_track);
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText intervalEditText = (EditText)findViewById(R.id.TrackingValue);
				if (!intervalEditText.getText().toString().isEmpty()){	
					try{
						int userInput = Integer.parseInt(intervalEditText.getText().toString());				
						tracking.setTimeInterval(userInput);
						if(Duration.validateTrackingInput(tracking)){
							Intent intent = new Intent();		
							intent.putExtra("com.redtop.engaze.entity.Tracking", (Parcelable)tracking);
							setResult(RESULT_OK, intent);  
							finish();
						}
					}catch(NumberFormatException e){
						Toast.makeText(getBaseContext(),							
								getResources().getString(R.string.message_createEvent_trackingStartMaxAlert),
								Toast.LENGTH_LONG).show();
					}
				}
				else{
					Toast.makeText(getBaseContext(),							
							getResources().getString(R.string.event_invalid_input_message),
							Toast.LENGTH_LONG).show();
				}
			}
		});

		EditText text = (EditText)findViewById(R.id.TrackingValue);
		text.setText(Integer.toString(tracking.getTimeInterval()));

		periods = new ArrayList<TextView>();
		TextView period = null;

		period = (TextView)findViewById(R.id.Minutes);
		setDefaultTrackingPeriod(period);	
		periods.add(period);

		period = (TextView)findViewById(R.id.Hours);
		setDefaultTrackingPeriod(period);	
		periods.add(period);

		//		period = (TextView)findViewById(R.id.Weeks);
		//		setDefaultTrackingPeriod(period);	
		//		periods.add(period);
		//
		//		period = (TextView)findViewById(R.id.Days);
		//		setDefaultTrackingPeriod(period);	
		//		periods.add(period);


		for(int i=0;i<periods.size();i++){
			TextView pr = periods.get(i);

			pr.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					for(int i=0;i<periods.size();i++){
						TextView dv = periods.get(i);
						dv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
						String duration = dv.getText().toString();
						if(duration.contains(getResources().getString(R.string.before)))
						{
							dv.setText(duration.substring(0, duration.indexOf(getResources().getString(R.string.before))));
							dv.setTextColor(getResources().getColorStateList(R.color.primaryText));
						}

					}
					// TODO Auto-generated method stub
					TextView  dur = ((TextView)v);
					Drawable draw = getResources().getDrawable(R.drawable.primary_color_check);
					dur.setCompoundDrawablesWithIntrinsicBounds(null, null, draw, null);
					dur.setTextColor(getResources().getColorStateList(R.color.primary));
					dur.setText(dur.getText().toString().concat(getResources().getString(R.string.before)));
					tracking.setPeriod(dur.getTag().toString());
				}
			});

		}
	}
	private void setDefaultTrackingPeriod(TextView period) {
		if(period.getTag().equals(tracking.getPeriod()))
		{		   
			period.setText(period.getText().toString().concat(getResources().getString(R.string.before)));
			period.setTextColor(getResources().getColorStateList(R.color.primary));
			Drawable draw = getResources().getDrawable(R.drawable.primary_color_check);
			period.setCompoundDrawablesWithIntrinsicBounds(null, null, draw, null);
		}	
	}

}

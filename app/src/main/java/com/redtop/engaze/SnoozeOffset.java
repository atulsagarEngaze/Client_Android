package com.redtop.engaze;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.redtop.engaze.domain.Duration;

public class SnoozeOffset extends Activity {
	private Duration snoozeDuration = null;
	private EditText text;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_snooze);		
		snoozeDuration = new Duration(30, "minute", true);
		text = (EditText)findViewById(R.id.SnoozeValue);
		text.setText(Integer.toString(snoozeDuration.getTimeInterval()));

		Button save = (Button)findViewById(R.id.save_event_Snooze);
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!text.getText().toString().isEmpty()){
					try{
						int userInput = Integer.parseInt(text.getText().toString());
						//if(AppUtility.validateDurationInput(snoozeDuration, getBaseContext())){
							if(userInput >= getResources().getInteger(R.integer.runningevent_min_extend_minutes) && userInput <= getResources().getInteger(R.integer.runningevent_max_extend_minutes)){
							snoozeDuration.setTimeInterval(userInput);				
							Intent intent = new Intent();		
							intent.putExtra("com.redtop.engaze.entity.Snooze", snoozeDuration); 			
							setResult(RESULT_OK, intent);  
							finish();
						}
						else{
							Toast.makeText(getBaseContext(),							
									getResources().getString(R.string.message_runningEvent_extendDurationValidation),
									Toast.LENGTH_LONG).show();
							
						}
					}catch(NumberFormatException e){
						Toast.makeText(getBaseContext(),							
								getResources().getString(R.string.message_runningEvent_extendDurationValidation),
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

		Button cancel = (Button)findViewById(R.id.cancel_event_Snooze);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				finish();
			}
		});
	}
}

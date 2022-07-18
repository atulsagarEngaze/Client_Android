package com.redtop.engaze;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.Toast;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.utility.LogReader;
import com.redtop.engaze.restApi.FeedbackWS;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

public class FeedbackActivity extends BaseActivity {
	private static final String TAG = "FeedbackActivity";
	private EditText mFeedbacktext ;
	private JSONObject jobj;
	private boolean logcat = false;
	private CharSequence alertTitle;
	private CharSequence feedbackMessage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_feedback);
		mFeedbacktext = (EditText)findViewById(R.id.txt_feedback);
		Toolbar toolbar = (Toolbar) findViewById(R.id.feedback_toolbar);
		if (toolbar != null) {
			toolbar.setTitleTextAppearance(this, R.style.toolbarTextFontFamilyStyle);
			setSupportActionBar(toolbar);
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
			getSupportActionBar().setTitle(R.string.title_feedback);
			//toolbar.setSubtitle(R.string.title_about);
			toolbar.setNavigationOnClickListener(v -> {
				hideKeyboard(v);
				onBackPressed();
			});


			toolbar.setOnTouchListener(new OnTouchListener() {
				Handler handler = new Handler();

				int numberOfTaps = 0;
				long lastTapTimeMs = 0;
				long touchDownMs = 0;
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						touchDownMs = System.currentTimeMillis();
						break;
					case MotionEvent.ACTION_UP:
						handler.removeCallbacksAndMessages(null);

						if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
							//it was not a tap
							numberOfTaps = 0;
							lastTapTimeMs = 0;
							break;
						}

						if (numberOfTaps > 0 
								&& (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()) {
							numberOfTaps += 1;
						} else {
							numberOfTaps = 1;
						}

						lastTapTimeMs = System.currentTimeMillis();

						if (numberOfTaps == 5) {		                    
							//handle triple tap
							logcat = true;
							SaveFeedback();
						}
					}	
					return true;
				}			
			});

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_feedback, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_feedback:
			hideKeyboard(mFeedbacktext);
			if (!mFeedbacktext.getText().toString().isEmpty()){	
				SaveFeedback();
			}
			else{
				Toast.makeText(getBaseContext(),							
						getResources().getString(R.string.event_invalid_input_message),
						Toast.LENGTH_LONG).show();
			}			
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	protected void SaveFeedback() {

		showProgressBar(getResources().getString(R.string.message_general_progressDialog));

		JSONObject jsonObject =  createFeedbackJson();
		if(jsonObject!=null){
			FeedbackWS.saveFeedback(jsonObject, new OnAPICallCompleteListener<JSONObject>() {
				@Override
				public void apiCallSuccess(JSONObject response) {
					onSaveResponse(response);
				}

				@Override
				public void apiCallFailure() {
					Toast.makeText(AppContext.context,
							getResources().getString(R.string.message_feedback_saveFailure), Toast.LENGTH_SHORT).show();
					hideProgressBar();
				}
			});
		}
	}	/**
	 * 
	 */
	private JSONObject  createFeedbackJson() {
		jobj = new JSONObject();
		try {
			jobj.put("RequestorId", AppContext.context.loginId);

			if(logcat ){
				jobj.put("Feedback", LogReader.getLog());
				jobj.put("FeedbackCategory", "Logcat");
				alertTitle = "Logcat Saved!";
				feedbackMessage = "Thanks for sharing your logcat!";
			}else{
				jobj.put("Feedback", mFeedbacktext.getText());
				jobj.put("FeedbackCategory", "General");
				alertTitle = "Feedback Saved!";
				feedbackMessage = getResources().getString(R.string.message_feedback_success);
			}

			return jobj;

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return  null;
		}

	}

	private void onSaveResponse(JSONObject response) {			
		AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.setTitle(alertTitle);
		alertDialog.setMessage(feedbackMessage);
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//dialog.dismiss();
				Intent intent = null;
				intent = new Intent(mContext, HomeActivity.class);					
				startActivity(intent);	
				finish();
			}
		});		
		alertDialog.show();		                   
		hideProgressBar();

	}
}

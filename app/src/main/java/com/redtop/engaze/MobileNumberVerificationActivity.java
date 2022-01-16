package com.redtop.engaze;


import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.Toolbar;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.PermissionRequester;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.manager.SMSManager;
import com.redtop.engaze.common.constant.Constants;

import static com.redtop.engaze.common.constant.RequestCode.Permission.*;

public class MobileNumberVerificationActivity extends BaseActivity {
	private EditText mMobileNumberEdittext;
	private EditText mCountryCode;
	private EditText mOtpText;
	private static final String TAG = MobileNumberVerificationActivity.class.getName();
	private String mOTP;
	private String mMobileNumber;	
	private BroadcastReceiver mSMSReceiver;
	private Button mButtonVerify;
	private  static Context mContext;
	private Button mButtonValidateOTP;	
	private RelativeLayout mRLMobileEnter;
	private RelativeLayout mRLSmsWait;
	private ProgressCountDownTimer countDownTimer;
	private int startTime ;
	private int interval;
	private long timeElapsed;
	private TextView counter;
	private ProgressBar circularProgressBar ;
	private String countryCode;

	protected static final int REQUEST_CODE_EMAIL = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mobile_number_verification);				
		mContext = this;
		getUserEmailAccount();
		circularProgressBar = (ProgressBar) findViewById(R.id.circularProgressBar);
		mRLMobileEnter = (RelativeLayout)findViewById(R.id.rl_number_verification);
		mRLSmsWait = (RelativeLayout)findViewById(R.id.rl_verification_code);
		startTime = getResources().getInteger(R.integer.sms_timeout_period_millisecs);
		interval = getResources().getInteger(R.integer.sms_interval_millisecs);
		mMobileNumberEdittext = (EditText) findViewById(R.id.mobile_number);
		mMobileNumberEdittext.setOnEditorActionListener(new OnEditorActionListener() {		  
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					hideKeyboard(v);
					validateAndSendSMS();
					handled = true;
				}
				return handled;
			}
		});
		mButtonVerify = (Button) findViewById(R.id.verify_no);
		mCountryCode = (EditText) findViewById(R.id.country_code);
		mCountryCode.setText(GetCountryZipCode());	
		Toolbar toolbar = (Toolbar) findViewById(R.id.registration_toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			getSupportActionBar().setTitle(getResources().getString(R.string.title_mobile_verification));
			toolbar.setTitleTextColor(getResources().getColor(R.color.icon));
		}
		mButtonVerify.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hideKeyboard(v);
				validateAndSendSMS();
			}
		});
		mOtpText = (EditText)findViewById(R.id.txt_otp);
		mButtonValidateOTP = (Button)findViewById(R.id.btn_otp);
		mButtonValidateOTP.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {				
				showProgressBar("Validating OTP");
				String otp = mOtpText.getText().toString();
				if(mOTP.equals(otp)){
					PreffManager
					.setPref(Constants.USER_AUTH_TOKEN, "1");
					PreffManager.setPref(Constants.MOBILE_NUMBER, mMobileNumber);
					countDownTimer.cancel();
					LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mSMSReceiver);
					gotoProfilePage();

				}
				else{

					AlertDialog.Builder adb = null;
					adb = new AlertDialog.Builder(mContext);

					adb.setTitle("Invalid OTP");
					adb.setMessage(getResources().getString(R.string.message_mobVer_invalidOtp));
					adb.setIcon(android.R.drawable.ic_dialog_alert);

					adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {							
							mOtpText.setText("");
						} });
					adb.show();
				}
				hideProgressBar();
			}
		});
	}

	@Override
	protected void onDestroy() {
		if(mSMSReceiver!=null){
			LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mSMSReceiver);
		}
		super.onDestroy();		
	}


	private void getUserEmailAccount(){
		try {
			Intent intent = AccountPicker.newChooseAccountIntent(null, null,
					new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, null, null, null, null);
			startActivityForResult(intent, REQUEST_CODE_EMAIL);
		} catch (ActivityNotFoundException e) {
			Log.d(TAG,  e.toString());
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_EMAIL && resultCode == RESULT_OK) {
				String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				PreffManager.setPref(Constants.EMAIL_ACCOUNT, accountName);
			}
		}
	}

	private void validateAndSendSMS(){
		mMobileNumber = mMobileNumberEdittext.getText().toString();
		if (validateNo()) {
			if(AppContext.context.isInternetEnabled)
			{				
				confirmMobileNumber();
			}
			else
			{
				Log.d(TAG, "no data network");
				AppUtility.showAlert(mContext, "", getResources().getString(R.string.message_mobVer_noNetworkError));
			}
		}
	}

	private void confirmMobileNumber(){
		AlertDialog.Builder adb = null;
		adb = new AlertDialog.Builder(this);

		adb.setTitle("Confirm Number");
		adb.setMessage(getResources().getString(R.string.message_mobVer_confirmNumber) + " : " + mCountryCode.getText().toString() + " " + mMobileNumber);
		adb.setIcon(android.R.drawable.ic_dialog_alert);

		adb.setPositiveButton("OK", (dialog, which) -> checkPermissionAndSendSMS());

		adb.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
		adb.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}

	private void registerSmsReceiver(){
		mSMSReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				final Bundle bundle = intent.getExtras();

				try {
					 unregisterReceiver(mSMSReceiver);
					if (bundle != null) {

						final Object[] pdusObj = (Object[]) bundle.get("pdus");

						for (int i = 0; i < pdusObj.length; i++) {

							SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
							String phoneNumber = currentMessage.getDisplayOriginatingAddress();
							String message = currentMessage.getDisplayMessageBody();
							
							if(phoneNumber.contains(mMobileNumber)){								
								if(message.startsWith("OTP")){
									populateOTP(message);
								}
							}else{
								//SMS Gateway
								if(phoneNumber.contains("COORDI")){
								populateOTP(message);
								}
							}
						} // end for loop
					} // bundle is null

				} catch (Exception e) {
					Log.e("SmsReceiver", "Exception smsReceiver" +e);
				}
			}
			
		};

		IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(mSMSReceiver, filter);	

	}

	/**
	 * @param message
	 */
	private void populateOTP(String message) {
		mOtpText.setFocusableInTouchMode(true);
		String otp = message.substring(message.lastIndexOf(" ")+1);
		mOtpText.setText(otp);
		mButtonValidateOTP.performClick();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case SEND_SMS: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					sendSmsAndWait();
				} else {
					finish();
				}
				return;
			}
			// other 'case' lines to check for other
			// permissions this app might request.
		}
	}

	private void checkPermissionAndSendSMS(){
		if(countryCode.equals("+91")){
			if(PermissionRequester.CheckPermission(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS}, SEND_SMS,this)){
				sendSmsAndWait();
			}
		}
	}
	
	private void sendSmsAndWait() {

		mOTP = String.valueOf(AppUtility.getRandamNumber());
		String smsText = "OTP " + mOTP;
		registerSmsReceiver();
		setTimerLayout();
		SMSManager.sendSMS(countryCode, mMobileNumber,smsText, mContext);
	}

	/**
	 * 
	 */
	private void setTimerLayout() {
		mRLMobileEnter.setVisibility(View.GONE);
		mRLSmsWait.setVisibility(View.VISIBLE);
		counter   =  ((TextView) findViewById(R.id.textView1));
		counter.setText("2:00");
		countDownTimer = new ProgressCountDownTimer(startTime, interval);		
		countDownTimer.start();
	}	



	private boolean validateNo() {
		boolean flag = true;
		String no = mMobileNumber;
		countryCode = mCountryCode.getText().toString();		
		Log.d(TAG, "no " + no);

		if (countryCode =="" || countryCode.isEmpty()) {
			flag = false;
			AppUtility.showAlert(mContext, "", "Unable to Locate Country Code.");
		} else if (no == null || no.equals("")) {
			flag = false;
			AppUtility.showAlert(mContext, "", "Please enter mobile no.");
		} else if (no.length() >= 15 || no.length() <= 4) {
			flag = false;
			AppUtility.showAlert(mContext, "",
					getResources().getString(R.string.message_mobVer_validationMessage));
		}
		Log.d(TAG, "country " + countryCode);
		PreffManager
		.setPref(Constants.COUNTRY_CODE, countryCode );
		return flag;
	}	

	public void showToast(String msg) {
		Toast.makeText(this, "Toast: " + msg, Toast.LENGTH_LONG).show();
	}

	public String GetCountryZipCode(){
		String CountryID="";
		String CountryZipCode="";

		TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		//getNetworkCountryIso
		CountryID= manager.getSimCountryIso().toUpperCase();
		String[] rl=this.getResources().getStringArray(R.array.CountryCodes);
		for(int i=0;i<rl.length;i++){
			String[] g=rl[i].split(",");
			if(g[1].trim().equals(CountryID.trim())){
				CountryZipCode=g[0];
				break;  
			}
		}
		return CountryZipCode;
	}

	private void gotoProfilePage() {
		Intent intent = new Intent(mContext, ProfileActivity.class);
		startActivity(intent);
		this.finish();
	}	

	private void openAlert() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		//alertDialogBuilder.setTitle(this.getTitle()+ " decision");
		alertDialogBuilder.setMessage(mContext.getResources()
				.getString(R.string.message_mobVer_verificationFailure));
		alertDialogBuilder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				//gotoLoginPage();
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show alert
		alertDialog.show();
	}

	public class ProgressCountDownTimer extends CountDownTimer
	{
		public ProgressCountDownTimer(long startTime, long interval)
		{
			super(startTime, interval);
		}

		@Override
		public void onFinish()
		{
			counter.setText("0s");
			((MobileNumberVerificationActivity)mContext).openAlert();
			mRLSmsWait.setVisibility(View.GONE);
			mRLMobileEnter.setVisibility(View.VISIBLE);
			LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mSMSReceiver);
		}

		@Override
		public void onTick(long millisUntilFinished)
		{
			String text;
			timeElapsed = (int)(millisUntilFinished/interval);
			int progress =  (int)timeElapsed;
			circularProgressBar.setProgress(progress);
			long minute = timeElapsed/60; 
			long seconds = timeElapsed%60;
			if(minute!=0){
				text = minute + ":" + seconds + "s";
			}
			else{
				text = Long.toString(seconds) +"s";
			}			
			counter.setText(text);
		}
	}

}

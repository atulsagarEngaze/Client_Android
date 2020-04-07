package com.redtop.engaze;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.redtop.engaze.Interface.OnAPICallCompleteListner;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.service.FirstTimeInitializationService;
import com.redtop.engaze.service.RegistrationIntentService;

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.redtop.engaze.manager.ProfileManager;


public class ProfileActivity extends BaseActivity {

	private static String TAG = ProfileActivity.class.getName();
	private Button Save_Profile;
	private ProgressDialog mProgress;
	// Progress dialog
	private String profileName; 
	private static final int SELECT_PICTURE = 1;	 
	private String selectedImagePath;
	private ImageView img;
	private Uri selectedImageUri;
	private BroadcastReceiver mRegistrationBroadcastReceiver;
	private AlertDialog mAlertDialog; 
	private JSONObject mJRequestobj;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext= this;
		startInitializationService();
		setContentView(R.layout.activity_profile);
		TextView eulaTextView = (TextView)findViewById(R.id.linktermsandservice);
		//checkbox.setText("");
		eulaTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, EULAActivity.class);
				intent.putExtra("caller", getIntent().getComponent().getClassName());
				startActivity(intent);	
				finish();
			}
		});

		TextView privacyPolicyTextView = (TextView)findViewById(R.id.linkprivacypolicy);
		//checkbox.setText("");
		privacyPolicyTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, PrivacyPolicyActivity.class);
				intent.putExtra("caller", getIntent().getComponent().getClassName());
				startActivity(intent);
				finish();
			}
		});

		mRegistrationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {	                
				SaveProfile();
			}
		};
		EditText email = (EditText) findViewById(R.id.Email);
		String emailAccount = PreffManager.getPref(Constants.EMAIL_ACCOUNT);
		if(emailAccount!=null && !emailAccount.equals("")){
			email.setText(emailAccount);
		}		

		email.setOnEditorActionListener(new OnEditorActionListener() {	  
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					hideKeyboard(v);
					creatJsonAndStartService();
					handled = true;
				}
				return handled;
			}
		});

		EditText profileName = (EditText) findViewById(R.id.ProfileName);

		profileName.addTextChangedListener(new TextWatcher() {
			@Override    
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {							
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub				
			}
		});

		Toolbar toolbar = findViewById(R.id.profile_toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			getSupportActionBar().setTitle(getResources().getString(R.string.title_user_register));
			toolbar.setTitleTextColor(getResources().getColor(R.color.icon));
		}

		Save_Profile = findViewById(R.id.Save_Profile);
		Save_Profile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(AppContext.context.isInternetEnabled){
					hideKeyboard(v);
					creatJsonAndStartService();
				}
			}
		});
	}
	private void startInitializationService() {
		Intent registeredContactsRefreshServiceIntent = new Intent(mContext, FirstTimeInitializationService.class);
		startService(registeredContactsRefreshServiceIntent);		
	}
	@Override
	protected void onResume() {
		turnOnOfInternetAvailabilityMessage();
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
				new IntentFilter(Constants.REGISTRATION_COMPLETE));
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
		super.onPause();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {					        
				selectedImageUri = data.getData();
				//selectedImagePath = getRealPathFromURI(selectedImageUri);
				img.setBackgroundResource(0);				
				Bitmap bm = getBitMapFromURI(selectedImageUri);
				RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(),bm);
				dr.setCornerRadius(Math.min(dr.getMinimumWidth(), dr.getMinimumHeight()) / 2.0F);				
				dr.setAntiAlias(true);
				img.setImageDrawable(dr);								
			}			
		}
	}

	private void creatJsonAndStartService(){
		CreateJsonRequestObject();
		if(validateInputData()){
			mProgress = new ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT);
			mProgress.setMessage(getResources().getString(R.string.message_userReg_saveInProgress));
			mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			mProgress.setCancelable(false);
			mProgress.setCanceledOnTouchOutside(false);
			mProgress.setIndeterminate(true);
			mProgress.show();
			Intent intent = new Intent(mContext, RegistrationIntentService.class);
			Log.i(TAG, "Start : RegistrationIntentService" );
			startService(intent);
		}

	}
	public Bitmap getBitMapFromURI(Uri contentUri) {

		try {
			Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(contentUri));
			return bitmap;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}	

	private void CreateJsonRequestObject(){

		String encodedImage ="";

		if(selectedImagePath!=null)
		{

			Bitmap bm = BitmapFactory.decodeFile(selectedImagePath);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object   
			byte[] byteArrayImage = baos.toByteArray();

			encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
		}

		// making json object request
		mJRequestobj = new JSONObject();
		try {
			profileName = ((EditText) findViewById(R.id.ProfileName)).getText().toString().trim();
			mJRequestobj.put("ProfileName", profileName);
			mJRequestobj.put("Email", ((EditText) findViewById(R.id.Email)).getText().toString().trim());
			mJRequestobj.put("ImageUrl", encodedImage);
			mJRequestobj.put("DeviceId", PreffManager.getPref(Constants.DEVICE_ID));
			mJRequestobj.put("CountryCode", PreffManager.getPref(Constants.COUNTRY_CODE));
			mJRequestobj.put("MobileNumber", PreffManager.getPref(Constants.MOBILE_NUMBER));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	private void SaveProfile() {
		try {
			// get GCMID/DeviceID/MobileNumber from Preferences
			mJRequestobj.put("GCMClientId", PreffManager.getPref(Constants.GCM_REGISTRATION_TOKEN));
			ProfileManager.saveProfile(mContext, mJRequestobj, new OnAPICallCompleteListner() {

				@Override
				public void apiCallComplete(JSONObject response) {
					if(mProgress.isShowing()){
						mProgress.hide();
					}

					Intent intent = new Intent(mContext, SplashActivity.class);
					startActivity(intent);

				}
			}, AppContext.actionHandler);

		} catch (JSONException e) {
			e.printStackTrace();
			Log.d(TAG, e.toString());
			if(mProgress.isShowing()){
				mProgress.hide();
			}
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.message_userReg_errorSaving),
					Toast.LENGTH_LONG).show();
		}		
	}	


	private Boolean validateInputData(){

		try {
			String profileName = mJRequestobj.getString("ProfileName");
			if(profileName.isEmpty() || profileName.trim().length() == 0){
				setAlertDialog("Profile name is blank!",getResources().getString(R.string.message_userReg_name_blank));
				mAlertDialog.show();				
				return false;
			}
			else{				
				if(profileName.length() > mContext.getResources().getInteger(R.integer.profile_name_maximum_legth)){
					setAlertDialog("Profile name invalid!",getResources().getString(R.string.message_userReg_name_length));
					mAlertDialog.show();
					return false;
				}
				  else if (!profileName.matches("[a-zA-Z0-9 ]*")) {
					setAlertDialog("Profile name invalid!",getResources().getString(R.string.message_userReg_name_special_character));
					mAlertDialog.show();
					return false;
				}
			}
			String emailId = mJRequestobj.getString("Email");
			if(emailId.isEmpty() || !(android.util.Patterns.EMAIL_ADDRESS.matcher(emailId).matches())){				

				setAlertDialog("Invalid email!",getResources().getString(R.string.message_userReg_emailValidation));
				mAlertDialog.show();				
				return false;
			}


		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public void onBackPressed() {	
		super.onBackPressed();
	}


	private void setAlertDialog(String Title, String message){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				mContext);
		// set title
		alertDialogBuilder.setTitle(Title);
		// set dialog message
		alertDialogBuilder
		.setMessage(message)
		.setCancelable(false)
		.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, close
				// current activity
				dialog.cancel();
			}
		});

		mAlertDialog = alertDialogBuilder.create();
	}

}
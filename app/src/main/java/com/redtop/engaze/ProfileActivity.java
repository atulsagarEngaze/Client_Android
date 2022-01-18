package com.redtop.engaze;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.constant.Veranstaltung;
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
	private IntentFilter mFilter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext= this;
		startInitializationService();
		setContentView(R.layout.activity_profile);
		TextView eulaTextView = (TextView)findViewById(R.id.linktermsandservice);
		//checkbox.setText("");
		eulaTextView.setOnClickListener(v -> {
			Intent intent = new Intent(mContext, EULAActivity.class);
			intent.putExtra("caller", getIntent().getComponent().getClassName());
			startActivity(intent);
			finish();
		});

		TextView privacyPolicyTextView = (TextView)findViewById(R.id.linkprivacypolicy);
		//checkbox.setText("");
		privacyPolicyTextView.setOnClickListener(v -> {
			Intent intent = new Intent(mContext, PrivacyPolicyActivity.class);
			intent.putExtra("caller", getIntent().getComponent().getClassName());
			startActivity(intent);
			finish();
		});

		mRegistrationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (mProgress.isShowing()) {
					mProgress.hide();
				}
				if(intent.getAction().equals(Veranstaltung.REGISTRATION_COMPLETE)) {

					Intent splashIntent = new Intent(mContext, SplashActivity.class);
					startActivity(splashIntent);
				}
				else if(intent.getAction().equals(Veranstaltung.REGISTRATION_FAILED)){
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.message_userReg_errorSaving),
							Toast.LENGTH_LONG).show();
				}
			}
		};

		mFilter = new IntentFilter();
		mFilter.addAction(Veranstaltung.REGISTRATION_COMPLETE);
		mFilter.addAction(Veranstaltung.REGISTRATION_FAILED);

		EditText email = (EditText) findViewById(R.id.Email);
		String emailAccount = PreffManager.getPref(Constants.EMAIL_ACCOUNT);
		if(emailAccount!=null && !emailAccount.equals("")){
			email.setText(emailAccount);
		}		

		email.setOnEditorActionListener((v, actionId, event) -> {
			boolean handled = false;
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				hideKeyboard(v);
				createJsonAndStartService();
				handled = true;
			}
			return handled;
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
			toolbar.setTitleTextAppearance(this, R.style.toolbarTextFontFamilyStyle);
			setSupportActionBar(toolbar);
			getSupportActionBar().setTitle(getResources().getString(R.string.title_user_register));
		}

		Save_Profile = findViewById(R.id.Save_Profile);
		Save_Profile.setOnClickListener(v -> {
			if(AppContext.context.isInternetEnabled){
				hideKeyboard(v);
				createJsonAndStartService();
			}
		});
	}
	private void startInitializationService() {
		Intent intent = new Intent(mContext, FirstTimeInitializationService.class);
		startService(intent);
	}
	@Override
	protected void onResume() {
		turnOnOfInternetAvailabilityMessage();
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,mFilter);
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
		super.onPause();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				selectedImageUri = data.getData();
				//selectedImagePath = getRealPathFromURI(selectedImageUri);
				img.setBackgroundResource(0);
				Bitmap bm = getBitMapFromURI(selectedImageUri);
				RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(), bm);
				dr.setCornerRadius(Math.min(dr.getMinimumWidth(), dr.getMinimumHeight()) / 2.0F);
				dr.setAntiAlias(true);
				img.setImageDrawable(dr);
			}
		}
	}

	private void createJsonAndStartService(){
		JSONObject jsonProfileObject = CreateJsonRequestObject();
		if(validateInputData(jsonProfileObject)){
			mProgress = new ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT);
			mProgress.setMessage(getResources().getString(R.string.message_userReg_saveInProgress));
			mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			mProgress.setCancelable(false);
			mProgress.setCanceledOnTouchOutside(false);
			mProgress.setIndeterminate(true);
			mProgress.show();
			Intent intent = new Intent(mContext, RegistrationIntentService.class);
			intent.putExtra("profileObject", jsonProfileObject.toString());
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

	private JSONObject CreateJsonRequestObject(){

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
		JSONObject jsonProfileObject = new JSONObject();
		try {
			jsonProfileObject.put("ProfileName", ((EditText) findViewById(R.id.ProfileName)).getText().toString().trim());
			jsonProfileObject.put("Email", ((EditText) findViewById(R.id.Email)).getText().toString().trim());
			jsonProfileObject.put("ImageUrl", encodedImage);
			jsonProfileObject.put("DeviceId", PreffManager.getPref(Constants.DEVICE_ID));
			jsonProfileObject.put("CountryCode", PreffManager.getPref(Constants.COUNTRY_CODE));
			jsonProfileObject.put("MobileNumber", PreffManager.getPref(Constants.MOBILE_NUMBER));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonProfileObject;
	}

	private Boolean validateInputData(JSONObject jsonProfileObject){

		try {
			String profileName = jsonProfileObject.getString("ProfileName");
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
			String emailId = jsonProfileObject.getString("Email");
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
		.setPositiveButton("Ok", (dialog, id) -> {
			// if this button is clicked, close
			// current activity
			dialog.cancel();
		});

		mAlertDialog = alertDialogBuilder.create();
	}
}
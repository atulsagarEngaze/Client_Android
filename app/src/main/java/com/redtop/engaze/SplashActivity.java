package com.redtop.engaze;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.redtop.engaze.common.AppService;
import com.redtop.engaze.common.Constants;
import com.redtop.engaze.common.PreffManager;
import com.redtop.engaze.service.EventRefreshService;

public class SplashActivity extends BaseActivity1 {

	private ProgressDialog mProgress;
	
	@Override
	public void onBackPressed() {
		finish();
	}
	@Override	

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		mContext.activityContext = this;
		AppService.setApplicationContext(this.getApplicationContext());
		AppService.deviceDensity = getResources().getDisplayMetrics().densityDpi;
		setContentView(R.layout.activity_splash);			
		String loginValue = PreffManager.getPref(Constants.LOGIN_ID, this);
		Intent intent = null;

		if(loginValue != null){				
			String firstTimeUse = PreffManager.getPref("firstTime",  mContext);
			if( firstTimeUse != null && firstTimeUse.equals("true")){
				mProgress = new ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT);
				mProgress.setMessage(getResources().getString(R.string.message_home_initialize));
				mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

				mProgress.setCancelable(false);
				mProgress.setCanceledOnTouchOutside(false);
				mProgress.setIndeterminate(true);
				mProgress.show();
				PreffManager.setPref("firstTime", "false", mContext);
				/*new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						mProgress.hide();
						//Intent intent = new Intent(getApplicationContext(), Recurrence.class);
						Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
						startActivity(intent);
					}
				}, 3000);*/

			}
			else
			{
				Intent refreshServiceIntent = new Intent(this, EventRefreshService.class);
				startService(refreshServiceIntent);

				/*intent = new Intent(this, HomeActivity.class);
				//intent = new Intent(getApplicationContext(), Recurrence.class);
				startActivity(intent);*/

			}
		}
		else
		{

			String authToken = PreffManager.getPref(Constants.USER_AUTH_TOKEN, mContext);

			/*if(authToken!=null && authToken.equals("1"))
			{
				intent = new Intent(this, ProfileActivity.class);
			}
			else
			{
				intent = new Intent(this, MobileNumberVerificationActivity.class);
			}*/

			intent = new Intent(this, MobileNumberVerificationActivity.class);
			startActivity(intent);

		}
	}
}

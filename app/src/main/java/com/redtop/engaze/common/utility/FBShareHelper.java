package com.redtop.engaze.common.utility;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.redtop.engaze.BaseActivity1;
import com.redtop.engaze.R;

public class FBShareHelper {

	private BaseActivity1 mActivity;
	private  CallbackManager mCallbackManager;
	private ShareDialog mShareDialog;
	
	public FBShareHelper(Context context){
		mActivity = (BaseActivity1)context;
	}
	
	public CallbackManager getFBCallbackManager(){
		return mCallbackManager;
	}
	
	public void initializeFacebookInstance(){
				
		FacebookSdk.sdkInitialize(mActivity);
		mCallbackManager = CallbackManager.Factory.create();

		LoginManager.getInstance().registerCallback(mCallbackManager,
				new FacebookCallback<LoginResult>() {
			private ProfileTracker mProfileTracker;
			private String fb_profile_name;

			@Override
			public void onSuccess(LoginResult loginResult) {				
				if(Profile.getCurrentProfile() == null) {
					mProfileTracker = new ProfileTracker() {
						@Override
						protected void onCurrentProfileChanged(Profile profile, Profile profile2) {			                    

							fb_profile_name = profile2.getFirstName();
							mProfileTracker.stopTracking();
						}
					};
					mProfileTracker.startTracking();
				}
				else {
					Profile profile = Profile.getCurrentProfile();
					fb_profile_name = profile.getFirstName();
				}
				gotoFBSharing(fb_profile_name);

			}

			@Override
			public void onCancel() {				
			}

			@Override
			public void onError(FacebookException exception) {
				Toast.makeText(mActivity,						
						mActivity.getResources().getString(R.string.fb_login_error),
						Toast.LENGTH_LONG).show();
			}
		});


		mShareDialog = new ShareDialog(mActivity);
		mShareDialog.registerCallback(mCallbackManager, new FacebookCallback<Sharer.Result>() {

			@Override
			public void onSuccess(Sharer.Result result) {
				// TODO Auto-generated method stub
				ProgressBar.hideProgressBar();
				Toast.makeText(mActivity,								
						mActivity.getResources().getString(R.string.fb_share_success),
						Toast.LENGTH_LONG).show();

			}

			@Override
			public void onCancel() {				
			}

			@Override
			public void onError(FacebookException error) {
				// TODO Auto-generated method stub
				ProgressBar.hideProgressBar();
				Toast.makeText(mActivity,						
						mActivity.getResources().getString(R.string.fb_share_error),
						Toast.LENGTH_LONG).show();

			}  });
	}
	
	private void gotoFBSharing(String fb_profile_name){
		try{
			
			String title = "";
			if(fb_profile_name != null){

				title = String.format(mActivity.getResources().getString(R.string.fb_share_title),fb_profile_name);
			}else{
				title = "I caught up with my friend(s) using Coordify";
			}
			String description = mActivity.getResources().getString(R.string.fb_share_description);
			if (ShareDialog.canShow(ShareLinkContent.class)) {
				//Profile profile = Profile.getCurrentProfile();
				//String fb_profile_name = profile.getFirstName();
				ShareLinkContent linkContent = new ShareLinkContent.Builder()
				.setContentTitle(title)
				.setContentDescription(description)							
				.setContentUrl(Uri.parse(mActivity.getResources().getString(R.string.fb_share_contenturl)))					 
				//.setImageUrl(uri) //we should capture a screen shot of the event					
				.build();

				mShareDialog.show(linkContent);
			}				
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
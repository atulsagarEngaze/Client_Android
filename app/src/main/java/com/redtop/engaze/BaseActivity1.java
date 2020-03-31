package com.redtop.engaze;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class BaseActivity1 extends AppCompatActivity {
    public Context mContext;
    public AppContext mAppContext;
    private ProgressDialog mDialog;
    protected static Boolean mInternetStatus;
    protected BroadcastReceiver mNetworkUpdateBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mAppContext = AppContext.getInstance();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (mDialog ==null){
            mDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        }

        mInternetStatus = AppUtility.isNetworkAvailable(this);

        mNetworkUpdateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Constants.NETWORK_STATUS_UPDATE))
                {
                    mInternetStatus = AppUtility.isNetworkAvailable(context);
                    turnOnOfInternetAvailabilityMessage(context);
                    if(mInternetStatus){
                        onInternetConnectionResume();
                    }
                    else{
                        onInternetConnectionLost();
                    }
                }

            }
        };
    }

    protected void onInternetConnectionResume(){
    }

    protected void onInternetConnectionLost(){

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNetworkUpdateBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mNetworkUpdateBroadcastReceiver,
                new IntentFilter(Constants.NETWORK_STATUS_UPDATE));
        turnOnOfInternetAvailabilityMessage(this);
    }

    protected void showProgressBar(String message ){
        ProgressBar.showProgressBar(message);
    }

    protected void showProgressBar(String title, String message ){
        ProgressBar.showProgressBar(title, message);
    }

    protected void hideProgressBar(){
        ProgressBar.hideProgressBar();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected void turnOnOfInternetAvailabilityMessage(Context context)
    {
        View v = findViewById(R.id.internet_status);
        if(v!=null){

            LinearLayout networkStatusLayout= (LinearLayout) v;
            if(mAppContext.isInternetEnabled)
            {
                if(networkStatusLayout!=null)
                {
                    networkStatusLayout.setVisibility(View.GONE);
                }
            }
            else
            {
                if(networkStatusLayout!=null)
                {
                    networkStatusLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    protected void displayView(int position) {
        Intent intent = null ;
        switch (position) {
            case 0:
                if( this.getClass().getName().equals(EventsActivity.class.getName())){
                    finish();
                }
                else if(this.getClass().getName().equals(HomeActivity.class.getName())){
                    //do nothing
                }
                break;
            case 1:
                if(this.getClass().getName().equals(HomeActivity.class.getName())){
                    intent = new Intent(this, EventsActivity.class);
                }
                else if(this.getClass().getName().equals(EventsActivity.class.getName())){
                    //do nothing
                }
                break;

            case 2:
                inviteFriend();
                break;

            case 3:
                intent = new Intent(this, MemberListActivity.class);
                break;
            case 4:
                intent = new Intent(this, EventSettingsActivity.class);
                break;

            case 5:
                intent = new Intent(this, FeedbackActivity.class);
                break;

            case 6:
                intent = new Intent(this, AboutActivity.class);
                break;

            default:
                break;
        }
        if(intent != null){
            startActivity(intent);
        }
    }

    public void inviteFriend(){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.message_invitation_success));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.message_invitation_body));
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.label_invitation_inviteUsing)));
    }
}

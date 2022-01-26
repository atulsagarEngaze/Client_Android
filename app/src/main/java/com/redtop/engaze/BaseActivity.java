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
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.constant.Veranstaltung;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.ProgressBar;
import com.redtop.engaze.common.utility.UserMessageHandler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class BaseActivity extends AppCompatActivity {
    public Context mContext;
    private ProgressDialog mDialog;
    protected BroadcastReceiver mBbroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        AppContext.context.currentActivity = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
      /*  View decorView = this.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(uiOptions);*/

        if (mDialog == null) {
            mDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        }


        mBbroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Constants.NETWORK_STATUS_UPDATE)) {
                    AppContext.context.isInternetEnabled = AppUtility.isNetworkAvailable(context);
                    turnOnOfInternetAvailabilityMessage();
                    if (AppContext.context.isInternetEnabled) {
                        onInternetConnectionResume();
                    } else {
                        onInternetConnectionLost();
                    }
                } else if (action.equals(Veranstaltung.CONTACT_LIST_REFRESH_PROCESS_COMPLETE)) {
                    contact_list_refresh_process_complete();
                }

            }
        };
    }

    protected void onInternetConnectionResume() {
    }

    protected void onInternetConnectionLost() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBbroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppContext.context.currentActivity = this;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.NETWORK_STATUS_UPDATE);
        filter.addAction(Veranstaltung.CONTACT_LIST_REFRESH_PROCESS_COMPLETE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBbroadcastReceiver, filter);
        turnOnOfInternetAvailabilityMessage();
    }

    public void actionFailed(String msg, Action action) {
        if (msg == null) {
            msg = UserMessageHandler.getFailureMessage(action);
        }

        ProgressBar.hideProgressBar();
        Toast.makeText(AppContext.context, msg, Toast.LENGTH_SHORT).show();
    }

    public void actionCompleted(Action action) {
        String msg = UserMessageHandler.getSuccessMessage(action);
        ProgressBar.hideProgressBar();
        Toast.makeText(AppContext.context, msg, Toast.LENGTH_SHORT).show();
    }

    protected void showProgressBar(String message) {
        ProgressBar.showProgressBar(message);
    }

    protected void showProgressBar(String title, String message) {
        ProgressBar.showProgressBar(title, message);
    }

    protected void hideProgressBar() {
        ProgressBar.hideProgressBar();
    }

    public void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected void turnOnOfInternetAvailabilityMessage() {
        View v = findViewById(R.id.internet_status);
        if (v != null) {

            LinearLayout networkStatusLayout = (LinearLayout) v;
            if (AppContext.context.isInternetEnabled) {
                if (networkStatusLayout != null) {
                    networkStatusLayout.setVisibility(View.GONE);
                }
            } else {
                if (networkStatusLayout != null) {
                    networkStatusLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    protected void displayView(int position) {
        Intent intent = null;
        switch (position) {
            case 0:
                if (this.getClass().getName().equals(EventsActivity.class.getName())) {
                    finish();
                } else if (this.getClass().getName().equals(HomeActivity.class.getName())) {
                    //do nothing
                }
                break;
            case 1:
                if (this.getClass().getName().equals(HomeActivity.class.getName())) {
                    intent = new Intent(this, EventsActivity.class);
                } else if (this.getClass().getName().equals(EventsActivity.class.getName())) {
                    //do nothing
                }
                break;

            case 2:
                inviteFriend();
                break;

            case 3:
                intent = new Intent(this, ContactGroupListActivity.class);
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
        if (intent != null) {
            startActivity(intent);
        }
    }

    public void inviteFriend() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.message_invitation_success));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.message_invitation_body));
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.label_invitation_inviteUsing)));
    }

    public void contact_list_refresh_process_complete() {
    }
}

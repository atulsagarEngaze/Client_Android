package com.redtop.engaze;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.firebase.iid.FirebaseInstanceId;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.PermissionRequester;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.service.ContactListRefreshIntentService;
import com.redtop.engaze.service.EventRefreshService;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.redtop.engaze.common.constant.RequestCode.Permission.ACCESS_BACKGROUND_LOCATION;
import static com.redtop.engaze.common.constant.RequestCode.Permission.ALL_NECCESSARY;
import static com.redtop.engaze.common.constant.RequestCode.Permission.READ_CONTACTS;

public class SplashActivity extends BaseActivity {

    private ProgressDialog mProgress;

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtility.deviceDensity = getResources().getDisplayMetrics().densityDpi;
        setContentView(R.layout.activity_splash);

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.i("SplashActivity", "FCM Registration Token: " + token);


        //first time load ask for all the permissions needed
        if (AppContext.context.loginId == null) {
            initiateMobileRegistrationAndProfileCreationProcess();
        } else {

            Boolean isFirstTimeLoading = PreffManager.getPrefBoolean("IsFirstTimeLoading", true);
            if (isFirstTimeLoading) {

                if (PermissionRequester.CheckPermission(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS, this)) {
                    importContacts();
                }

            } else {
                startHomeActivity();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        ArrayList<String> permissionNotGranted = PermissionRequester.permissionsNotGranted(permissions);
        if (permissionNotGranted.size() != 0) {
            showMandatoryPermissionAlertDialogAndCloseTheApp(permissionNotGranted);
            return;
        }

        switch (requestCode) {
            case READ_CONTACTS: {
                importContacts();
                break;

            }
            case ACCESS_BACKGROUND_LOCATION: {
                AppContext.context.setDefaultValuesAndStartLocationService();
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
        return;
    }

    private void initiateMobileRegistrationAndProfileCreationProcess() {
        String authToken = PreffManager.getPref(Constants.USER_AUTH_TOKEN);
        Intent intent;
        if (authToken != null && authToken.equals("1")) {
            intent = new Intent(this, ProfileActivity.class);
        } else {
            intent = new Intent(this, MobileNumberVerificationActivity.class);
        }
        startActivity(intent);
    }

    private void checkLocationPermissionsAndStartHomeActivity() {
        List<String> permissionsList = new ArrayList<String>();
        permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            permissionsList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }

        String[] permissionArray = new String[permissionsList.size()];
        permissionArray = permissionsList.toArray(permissionArray);
        if (PermissionRequester.CheckPermission(
                permissionArray, ACCESS_BACKGROUND_LOCATION, this)) {

            AppContext.context.setDefaultValuesAndStartLocationService();
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
    }


    private void startHomeActivity() {
        EventManager.RemoveALlPastEvents();
        Intent refreshServiceIntent = new Intent(this, EventRefreshService.class);
        startService(refreshServiceIntent);

        checkLocationPermissionsAndStartHomeActivity();
    }

    private void importContacts() {
        mProgress = new ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT);
        mProgress.setMessage(getResources().getString(R.string.message_home_initialize));
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mProgress.setCancelable(false);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setIndeterminate(true);
        mProgress.show();
        AppContext.context.PerformFirstTimeInitialization();
        ContactListRefreshIntentService.start(this, false);
    }

    @Override
    public void contact_list_refresh_process_complete() {

        String contactsRefreshStatus = PreffManager.getPref(Constants.LAST_CONTACT_LIST_REFRESH_STATUS);
        String registeredContactsRefreshStatus = PreffManager.getPref(Constants.LAST_REGISTERED_CONTACT_LIST_REFRESH_STATUS);

        if (contactsRefreshStatus.equals(Constants.SUCCESS) && registeredContactsRefreshStatus.equals(Constants.SUCCESS)) {
            AppContext.context.sortedContacts = ContactAndGroupListManager.getSortedContacts();
        } else {
            Toast.makeText(AppContext.context.currentActivity, AppContext.context.getResources().getString(R.string.message_contacts_errorRetrieveData), Toast.LENGTH_SHORT).show();
        }

        PreffManager.setPrefBoolean("IsFirstTimeLoading", false);
        mProgress.hide();
        checkLocationPermissionsAndStartHomeActivity();
    }

    private void showMandatoryPermissionAlertDialogAndCloseTheApp(ArrayList<String> permissions) {
        String message;

        if(permissions.size()==1 && permissions.get(0).equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
            message="Please go to the app Location Permission settings and enable 'allow all the time' for engaze";
        }
        else {


            message = android.text.TextUtils.join(",", permissions)
                    + (permissions.size() == 1 ? " is" : " are") + " required to run the app!";
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Permission not granted");
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialogInterface, i) -> {
                    finishAffinity();
                    ((Activity)mContext).finishAndRemoveTask();

                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}

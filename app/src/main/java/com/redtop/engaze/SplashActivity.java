package com.redtop.engaze;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.PermissionRequester;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.service.EventRefreshService;

import static com.redtop.engaze.common.constant.RequestCode.Permission.ALL_NECCESSARY;

public class SplashActivity extends BaseActivity {

    private ProgressDialog mProgress;

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AcceptanceStatus status = AcceptanceStatus.Accepted;

        String strAccpet  =  "Accepted";

        AcceptanceStatus status2 = AppContext.jsonParser.deserialize(strAccpet, AcceptanceStatus.class);

        AppUtility.deviceDensity = getResources().getDisplayMetrics().densityDpi;
        setContentView(R.layout.activity_splash);
        //first time load ask for all the permissions needed
        if (AppContext.context.loginId == null) {
            checkRequiredPermissions();
        } else {
            startHomeActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ALL_NECCESSARY: {
                // If request is cancelled, the result arrays are empty.
                if (PermissionRequester.hasPermissions(permissions)) {
                    initiateMobileRegistrationAndProfileCreationProcess();
                } else {
                    finish();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
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

    private void checkRequiredPermissions() {
        if (PermissionRequester.CheckPermission(
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.READ_CONTACTS,
                }, ALL_NECCESSARY, this)) {

            initiateMobileRegistrationAndProfileCreationProcess();
        }
    }

    private void startHomeActivity() {
        if (!accessingContactsFirstTime()) {
            mProgress = new ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT);
            mProgress.setMessage(getResources().getString(R.string.message_home_initialize));
            mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            mProgress.setCancelable(false);
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.setIndeterminate(true);
            mProgress.show();
            AppContext.context.isFirstTimeLoading = false;
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mProgress.hide();
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                }
            }, 3000);

        } else {
            Intent refreshServiceIntent = new Intent(this, EventRefreshService.class);
            startService(refreshServiceIntent);

            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);

        }
    }
}

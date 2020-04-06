package com.redtop.engaze.common.utility;

import android.Manifest;
import android.content.pm.PackageManager;


import com.redtop.engaze.app.AppContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class PermissionRequester {

    public static boolean CheckPermission(String permission, int requestCode, AppCompatActivity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            //for time being we would directly ask for the permission

            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    requestCode);

            return false;

            // Permission is not granted
            // Should we show an explanation?
            /*if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        requestCode);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }*/
        } else {
            return true;
        }
    }
    // Here, thisActivity is the current activity

}

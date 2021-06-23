package com.redtop.engaze.common.utility;

import android.Manifest;
import android.content.pm.PackageManager;


import com.redtop.engaze.app.AppContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;


public class PermissionRequester {

    public static boolean   CheckPermission(String[] permissions, int requestCode, AppCompatActivity activity) {
        ArrayList<String> permissionsToBeAsked = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToBeAsked.add(permission);
            }
        }

        if (permissionsToBeAsked.size()>0) {
            //for time being we would directly ask for the permission
            String[] permissionArray = new String[permissionsToBeAsked.size()];
            ActivityCompat.requestPermissions(activity,
                    permissionsToBeAsked.toArray(permissionArray),
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

    public static boolean hasPermissions(String[] permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(AppContext.context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public static ArrayList<String> permissionsNotGranted(String[] permissions) {
        ArrayList<String>permissionNotGrantedList = new ArrayList<>();
        if (permissions != null) {

            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(AppContext.context, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionNotGrantedList.add(permission);
                }
            }
        }
        return permissionNotGrantedList;
    }
}

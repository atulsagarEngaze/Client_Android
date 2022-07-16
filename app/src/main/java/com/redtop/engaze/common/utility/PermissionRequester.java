package com.redtop.engaze.common.utility;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;


import com.redtop.engaze.app.AppContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;


public class PermissionRequester {

    public static boolean   CheckPermission(String[] permissions, int requestCode, Fragment fragment) {
        ArrayList<String> permissionsToBeAsked = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(fragment.getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToBeAsked.add(permission);
            }
        }

        if (permissionsToBeAsked.size()>0) {
            //for time being we would directly ask for the permission
            String[] permissionArray = new String[permissionsToBeAsked.size()];
            fragment.requestPermissions(permissionsToBeAsked.toArray(permissionArray),
                    requestCode);

            return false;

        } else {
            return true;
        }
    }

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

    public static void showMandatoryPermissionAlertDialogAndCloseTheApp(ArrayList<String> permissions, Activity activity) {

        String permissionMessage = android.text.TextUtils.join(",", permissions)
                + ( permissions.size()==1? " is" : " are") + " required to run the app!";

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle("Permission not granted");
        alertDialogBuilder
                .setMessage(permissionMessage)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialogInterface, i) -> {
                    activity.finishAffinity();

                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}

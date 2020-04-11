package com.redtop.engaze.common.utility;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

import com.redtop.engaze.app.AppContext;

import androidx.appcompat.app.AppCompatActivity;

public class ProgressBar {

    private static ProgressDialog mDialog;
    private static String  mCurrentActivityName = "";

    public static void showProgressBar(String message ){
        showProgressBar("",message);
    }

    public static void showProgressBar(String title, String message ){

        if(mDialog==null || !mCurrentActivityName.equals(AppContext.context.currentActivity.getClass().getSimpleName())){
            mCurrentActivityName = AppContext.context.currentActivity.getClass().getSimpleName();
            mDialog = new ProgressDialog(AppContext.context.currentActivity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        }

        if(!(title==null || title.equals(""))){
            mDialog.setTitle(title);
        }
        mDialog.setMessage(message);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setIndeterminate(true);
        mDialog.show();
    }

    public static void hideProgressBar(){
        if(mDialog!=null && mDialog.isShowing()){
            mDialog.dismiss();
        }
    }
}

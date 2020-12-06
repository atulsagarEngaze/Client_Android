package com.redtop.engaze.common.utility;

import android.app.AlertDialog;
import android.app.ProgressDialog;

import com.redtop.engaze.app.AppContext;

public class ProgressBar {

    private static ProgressDialog mDialog;
    private static int  mCurrentActivityId = 0;

    public static void showProgressBar(String message ){
        showProgressBar("",message);
    }

    public static void showProgressBar(String title, String message ){

        if(mDialog==null || mCurrentActivityId!=AppContext.context.currentActivity.hashCode()){
            mCurrentActivityId = AppContext.context.currentActivity.hashCode();
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

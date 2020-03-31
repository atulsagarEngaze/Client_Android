package com.redtop.engaze.common.utility;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

public class ProgressBar {

    private static ProgressDialog mDialog;

    public static void CreateProgressDialog(Context context){
        if(mDialog==null){
            mDialog = new ProgressDialog(context, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        }
    }
    public static void showProgressBar(String message ){
        showProgressBar("",message);
    }

    public static void showProgressBar(String title, String message ){

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

package com.redtop.engaze;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.redtop.engaze.app.AppContext;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity1 extends AppCompatActivity {
    public AppContext mContext;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = AppContext.getInstance();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (mDialog ==null){
            mDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        }
    }
    protected void showProgressBar(String message ){
        showProgressBar("",message);
    }

    protected void showProgressBar(String title, String message ){

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

    protected void hideProgressBar(){
        if(mDialog!=null && mDialog.isShowing()){
            mDialog.dismiss();
        }
    }

    protected void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected void turnOnOfInternetAvailabilityMessage(Context context)
    {
        View v = findViewById(R.id.internet_status);
        if(v!=null){

            LinearLayout networkStatusLayout= (LinearLayout) v;
            if(mContext.isInternetEnabled)
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
}

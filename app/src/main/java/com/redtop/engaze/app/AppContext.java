package com.redtop.engaze.app;

import android.app.Application;
import android.content.Context;

import com.redtop.engaze.common.PreffManager;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.ProgressBar;

public class AppContext extends Application {
    public static final String TAG = AppContext.class
            .getSimpleName();

    public boolean isFirstTimeLoading = false;
    public String loginId;
    public String loginName;
    public Context activityContext = null;
    public Boolean isInternetEnabled = true;
    public static AppContext context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        ProgressBar.CreateProgressDialog(this);
        isInternetEnabled = AppUtility.isNetworkAvailable(this);
        loginId = PreffManager.getPref(Constants.LOGIN_ID);
        if( loginId!=null){
            loginName =  PreffManager.getPref(Constants.LOGIN_NAME);
        }
    }
}

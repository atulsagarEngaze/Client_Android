package com.redtop.engaze.app;

import android.app.Application;
import android.content.Context;

import com.redtop.engaze.common.utility.JsonParser;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.utility.ActionHandler;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class AppContext extends Application {
    public static final String TAG = AppContext.class
            .getSimpleName();

    public boolean isFirstTimeLoading = false;
    public String loginId;
    public String loginName;
    public Boolean isInternetEnabled = true;
    public AppCompatActivity currentActivity;
    public static AppContext context;


    public static ActionHandler actionHandler;

    public static JsonParser jsonParser;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        isInternetEnabled = AppUtility.isNetworkAvailable(this);
        //for testing

        loginId = PreffManager.getPref(Constants.LOGIN_ID);
        isFirstTimeLoading = true;
        if (loginId != null) {
            loginName = PreffManager.getPref(Constants.LOGIN_NAME);
        }
        actionHandler = new ActionHandler();
        jsonParser = new JsonParser();
    }
}

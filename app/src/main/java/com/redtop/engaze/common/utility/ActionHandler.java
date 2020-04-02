package com.redtop.engaze.common.utility;

import android.widget.Toast;

import com.redtop.engaze.Interface.IActionHandler;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.UserMessageHandler;
import com.redtop.engaze.common.enums.Action;

public class ActionHandler implements IActionHandler {
    @Override
    public void actionFailed(String msg, Action action) {
        if (msg == null) {
            msg = UserMessageHandler.getFailureMessage(action);
        }

        ProgressBar.hideProgressBar();
        Toast.makeText(AppContext.context, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void actionComplete(Action action) {
        String msg = UserMessageHandler.getSuccessMessage(action);
        ProgressBar.hideProgressBar();
        Toast.makeText(AppContext.context, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void actionCancelled(Action action) {
    }
}

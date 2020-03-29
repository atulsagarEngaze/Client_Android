package com.redtop.engaze;

import android.annotation.SuppressLint;
import android.widget.Toast;

import com.redtop.engaze.common.UserMessageHandler;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.Interface.OnActionCancelledListner;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;


@SuppressWarnings("deprecation")
@SuppressLint("ResourceAsColor")
public abstract class ActionSuccessFailMessageActivity extends BaseActivity1 implements OnActionFailedListner, OnActionCompleteListner, OnActionCancelledListner   {

	@Override
	public void actionFailed(String msg, Action action) {
		if(msg ==null){ 
			msg= UserMessageHandler.getFailureMessage(action, mContext);
		}

		hideProgressBar();
		Toast.makeText(mContext,msg, Toast.LENGTH_SHORT).show();
	}



	@Override
	public void actionComplete(Action action) {
		String msg=UserMessageHandler.getSuccessMessage(action, mContext);
		hideProgressBar();
		Toast.makeText(mContext,msg, Toast.LENGTH_SHORT).show();
	}
	@Override
	public void actionCancelled(Action action){
	}
}

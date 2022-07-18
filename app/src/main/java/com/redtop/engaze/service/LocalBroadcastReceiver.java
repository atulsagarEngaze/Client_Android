package com.redtop.engaze.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LocalBroadcastReceiver extends BroadcastReceiver{
	protected Context mContext;
	public IntentFilter mFilter;
	
	public LocalBroadcastReceiver(Context context){
		super();
		mContext = context;		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
	}

	public IntentFilter getFilter(){
		return mFilter;
	}
}
package com.redtop.engaze.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.redtop.engaze.R;

public class ParticipantsWithNoCallSMSListAdapter extends ArrayAdapter<String>{

	private List<String>mDisplayNameList;
	//private final Integer[] imageId;
	private static LayoutInflater mInflater=null;
	private int resourceId;

	public ParticipantsWithNoCallSMSListAdapter(Activity context,int resource,
			List<String>displayNameList) {
		super(context, resource, displayNameList);
		this.mDisplayNameList = displayNameList;
		this.resourceId = resource;
		mInflater = ( LayoutInflater )context.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public class ViewHolder
	{
		TextView tv;		
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder= null;	

		if (convertView == null) {
			convertView = mInflater.inflate(this.resourceId, null);
			holder = new ViewHolder();
			holder.tv=(TextView) convertView.findViewById(R.id.txt_participant);			
			convertView.setTag(holder);

		} else 
			holder = (ViewHolder) convertView.getTag();

		String displayName = mDisplayNameList.get(position);		
		holder.tv.setText(displayName);		
		return convertView;
	}

}
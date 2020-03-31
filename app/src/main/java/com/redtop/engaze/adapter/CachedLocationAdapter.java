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
import com.redtop.engaze.entity.EventPlace;

public class CachedLocationAdapter extends ArrayAdapter<EventPlace> {

	Context context;
	public List<EventPlace> mItems;

	public CachedLocationAdapter(Context context, int resourceId,
			List<EventPlace> items) {
		super(context, resourceId, items);		
		this.context = context;	
		this.mItems = items;
	}

	/*private view holder class*/
	private class ViewHolder {	
		TextView txtName;
		TextView txtDescription;
		View divider;
	}	

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		EventPlace rowItem = getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_cached_location_list, null);
			holder = new ViewHolder();
			
			holder.txtName = (TextView) convertView.findViewById(R.id.cached_location_name);
			holder.txtDescription = (TextView) convertView.findViewById(R.id.cached_location_description);
			holder.divider = (View)convertView.findViewById(R.id.divider);	
			convertView.setTag(holder);
		} else 
			holder = (ViewHolder) convertView.getTag();


		holder.txtName.setText(rowItem.getName());
		holder.txtDescription.setText(rowItem.getAddress());
//		if(position==0)
//		{
//			holder.divider.setVisibility(View.GONE);
//		}

		return convertView;
	}	
}
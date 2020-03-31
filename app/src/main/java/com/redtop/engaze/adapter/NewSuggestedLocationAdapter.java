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
import com.redtop.engaze.domain.AutoCompletePlace;

public class NewSuggestedLocationAdapter extends ArrayAdapter<AutoCompletePlace> {

	Context context;
	public List<AutoCompletePlace> mItems;


	public NewSuggestedLocationAdapter(Context context, int resourceId,
			List<AutoCompletePlace> items) {
		super(context, resourceId, items);		
		this.context = context;	
		this.mItems = items;
	}

	/*private view holder class*/
	private class ViewHolder {	
		TextView txtName;
		View divider;
	}	

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		AutoCompletePlace rowItem = getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_suggested_location_list, null);
			holder = new ViewHolder();

			holder.txtName = (TextView) convertView.findViewById(R.id.suggested_location_name);	
			holder.divider = (View)convertView.findViewById(R.id.divider);	
			convertView.setTag(holder);
		} else 
			holder = (ViewHolder) convertView.getTag();


		holder.txtName.setText(rowItem.getDescription());
//		if(position==0)
//		{
//			holder.divider.setVisibility(View.GONE);
//		}

		return convertView;
	}	
}
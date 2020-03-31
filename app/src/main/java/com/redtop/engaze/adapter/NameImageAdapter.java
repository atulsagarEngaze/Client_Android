package com.redtop.engaze.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.redtop.engaze.R;
import com.redtop.engaze.domain.NameImageItem;

import androidx.core.graphics.drawable.DrawableCompat;

public class NameImageAdapter extends ArrayAdapter<NameImageItem> {

	Context context;

	public NameImageAdapter(Context context, int resourceId,
			List<NameImageItem> items) {
		super(context, resourceId, items);
		this.context = context;
	}
	
	/*private view holder class*/
	private class ViewHolder {
		ImageView imageView;
		TextView txtName;
		
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		NameImageItem rowItem = getItem(position);
		
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_name_image_row, null);
			holder = new ViewHolder();
		
			holder.txtName = (TextView) convertView.findViewById(R.id.name);
			holder.imageView = (ImageView) convertView.findViewById(R.id.event_type_icon);
			convertView.setTag(holder);
		} else 
			holder = (ViewHolder) convertView.getTag();
				
	
		holder.txtName.setText(rowItem.getName());
		Drawable originalDrawable = context.getResources().getDrawable(rowItem.getImageId());
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {			
			final Drawable wrappedDrawable = DrawableCompat.wrap(originalDrawable);
			DrawableCompat.setTint(wrappedDrawable, context.getResources().getColor(R.color.icon) );
			holder.imageView.setBackground(wrappedDrawable);
		}
		else{
		holder.imageView.setBackground(originalDrawable);
		}
		
		return convertView;
	}
}
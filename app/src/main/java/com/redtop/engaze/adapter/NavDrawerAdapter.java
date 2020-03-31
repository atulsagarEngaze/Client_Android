package com.redtop.engaze.adapter;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.redtop.engaze.R;
import com.redtop.engaze.domain.NavDrawerItem;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Sandy on 18-04-2015.
 */
public class NavDrawerAdapter  extends RecyclerView.Adapter<NavDrawerAdapter.MyViewHolder> {
	List<NavDrawerItem> data = Collections.emptyList();
	private LayoutInflater inflater;
	private Context mContext;
	public NavDrawerAdapter(Context context, List<NavDrawerItem> data) {
		inflater = LayoutInflater.from(context);
		this.data = data; 
		this.mContext = context;
	}

	public void delete(int position) {
		data.remove(position);
		notifyItemRemoved(position);
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.item_row, parent, false);
		MyViewHolder holder = new MyViewHolder(view);
		return holder;
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		NavDrawerItem current = data.get(position);
		holder.title.setText(current.getTitle());
		holder.titleIcon.setBackgroundResource(current.getTitleIcon());
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

			final Drawable originalDrawable = holder.titleIcon.getBackground();
			final Drawable wrappedDrawable = DrawableCompat.wrap(originalDrawable);
			DrawableCompat.setTint(wrappedDrawable, mContext.getResources().getColor(R.color.icon) );
			holder.titleIcon.setBackground(wrappedDrawable);
		}
		//holder.titleIconFont.setText(current.getTitleIconFont());
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	class MyViewHolder extends RecyclerView.ViewHolder {
		TextView title;
		ImageView titleIcon;
		//TextFont titleIconFont;
		public MyViewHolder(View itemView) {
			super(itemView);
			title = (TextView) itemView.findViewById(R.id.item_title);
			titleIcon = (ImageView) itemView.findViewById(R.id.item_icon);
			//titleIconFont = (TextFont)itemView.findViewById(R.id.item_font_icon);
		}
	}

}

package com.redtop.engaze.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.redtop.engaze.R;
import com.redtop.engaze.common.customeviews.CircularImageView;
import com.redtop.engaze.domain.ContactOrGroup;

public class ContactListAutoCompleteAdapter extends ArrayAdapter<ContactOrGroup> {
	List<ContactOrGroup> items, tempItems, suggestions;
	private Context mContext;
	public ContactListAutoCompleteAdapter(Context context, int resource,
			List<ContactOrGroup> data) {
		super(context, resource, data);
		this.items = data;
		this.mContext = context;
		tempItems = new ArrayList<ContactOrGroup>(items); // this makes the difference.
		suggestions = new ArrayList<ContactOrGroup>();
		// TODO Auto-generated constructor stub
	}

	/*private view holder class*/
	private class ViewHolder {
		CircularImageView imageView;
		TextView txtName;

	}

	public void setSelected(int position) {
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		final ContactOrGroup rowItem = getItem(position);

		LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_contact_autocomplete_list, null);
			holder = new ViewHolder();

			holder.txtName = (TextView) convertView.findViewById(R.id.contact_name);
			holder.imageView = (CircularImageView) convertView.findViewById(R.id.contact_icon);

			convertView.setTag(holder);

		} else 
			holder = (ViewHolder) convertView.getTag();

		holder.txtName.setText(rowItem.getName());
		holder.imageView.setBackground(rowItem.getImageDrawable(mContext));			

		return convertView;
	}

	@Override
	public Filter getFilter() {
		return nameFilter;
	}


	Filter nameFilter = new Filter() {
		@Override
		public CharSequence convertResultToString(Object resultValue) {
			String str = ((ContactOrGroup) resultValue).getName();
			return str;
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			if (constraint != null) {
				suggestions.clear();
				for (ContactOrGroup  contact : tempItems) {
					if (contact.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
						suggestions.add(contact);
					}
				}
				FilterResults filterResults = new FilterResults();
				filterResults.values = suggestions;
				filterResults.count = suggestions.size();
				return filterResults;
			} else {
				return new FilterResults();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			List<ContactOrGroup> filterList = (ArrayList<ContactOrGroup>) results.values;
			if (results != null && results.count > 0) {
				clear();
				addAll(filterList);
				notifyDataSetChanged();
//				for (ContactOrGroup contact : filterList) {
//					add(contact);
//					notifyDataSetChanged();
//				}
			}
		}

	};
}
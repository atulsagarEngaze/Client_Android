package com.redtop.engaze.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.redtop.engaze.R;
import com.redtop.engaze.domain.ContactOrGroup;

public class MemberAdapter extends ArrayAdapter<ContactOrGroup> {

    Context context;
    List<ContactOrGroup> rowItems;
    List<ContactOrGroup> list;

    public MemberAdapter(Context context, int resourceId,
                         List<ContactOrGroup> items) {
        super(context, resourceId, items);
        this.context = context;
        this.rowItems = items;
        list = new ArrayList<ContactOrGroup>();
        list.addAll(rowItems);
    }

    /*private view holder class*/
    private static class ViewHolder {
        public ImageView imageView;
        public TextView txtName;
        public TextView btn;

    }

    @Override
    public int getCount() {
        return rowItems.size();
    }

    @Override
    public ContactOrGroup getItem(int position) {
        return rowItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rowItems.hashCode();
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        ContactOrGroup rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            vi = mInflater.inflate(R.layout.member_list_item, null);
            holder = new ViewHolder();

            holder.txtName = (TextView) vi.findViewById(R.id.member_name);
            holder.imageView = (ImageView) vi.findViewById(R.id.group_member_img);
            holder.btn = (TextView) vi.findViewById(R.id.member_contact_btn);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        holder.txtName.setText(rowItem.getName());
        holder.imageView.setBackground(rowItem.getImageDrawable(context));

        if (rowItem.getUserId() != null) {
            holder.btn.setVisibility(View.GONE);
        } else {
            holder.btn.setVisibility(View.VISIBLE);
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.message_invitation_success);
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, R.string.message_invitation_body);
                    context.startActivity(Intent.createChooser(sharingIntent, "Invite"));
                }
            });
        }

        return vi;
    }

    public void filter(String charText) {

        charText = charText.toLowerCase(Locale.getDefault());

        rowItems.clear();
        if (charText.length() == 0) {
            rowItems.addAll(list);

        } else {
            for (ContactOrGroup postDetail : list) {
                if (charText.length() != 0 && postDetail.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    rowItems.add(postDetail);
                }
            }
        }
        notifyDataSetChanged();
    }
}
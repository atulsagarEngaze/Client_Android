package com.redtop.engaze.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.customeviews.CircularImageView;
import com.redtop.engaze.common.utility.ProgressBar;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.webservice.IUserWS;
import com.redtop.engaze.webservice.proxy.UserWSProxy;

public class ContactOrGroupListAdapter extends ArrayAdapter<ContactOrGroup> {

    private Context mContext;
    List<ContactOrGroup> rowItems;
    List<ContactOrGroup> list;
    private JSONObject mInviteJasonObj;
    private final static IUserWS contactsWS = new UserWSProxy();
    public ContactOrGroupListAdapter(Context context, int resource,
                                     List<ContactOrGroup> data) {
        super(context, resource, data);
        this.mContext = context;
        this.rowItems = data;
        list = new ArrayList<ContactOrGroup>();
        list.addAll(rowItems);
    }

    /*private view holder class*/
    private class ViewHolder {
        CircularImageView imageView;
        TextView txtName;
        TextView btnInvite;

    }

    public void setSelected(int position) {
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final ContactOrGroup rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_contact_group_list, null);
            holder = new ViewHolder();

            holder.txtName = (TextView) convertView.findViewById(R.id.contact_name);
            holder.imageView = (CircularImageView) convertView.findViewById(R.id.contact_icon);
            holder.btnInvite = (TextView) convertView.findViewById(R.id.invite_member);
            convertView.setTag(holder);

        } else
            holder = (ViewHolder) convertView.getTag();

        holder.txtName.setText(rowItem.getName());
        holder.imageView.setBackground(rowItem.getImageDrawable(mContext));
        holder.btnInvite.setVisibility(View.VISIBLE);

        if ((rowItem.getUserId() != null && rowItem.getUserId() != "") || rowItem.getGroupId() != 0) {
            holder.btnInvite.setVisibility(View.GONE);
        }

        holder.btnInvite.setOnClickListener(new OnClickListener() {
            //@Override
            public void onClick(View v) {
                ProgressBar.showProgressBar(mContext.getResources().getString(R.string.message_general_progressDialog));
                String num = rowItem.getNumbers().get(0);
                mInviteJasonObj = createInvitationJson(num.toString());
                SendInvite();

            }
        });

        //holder.imageView.setImageResource(rowItem.getGroupId()());

        return convertView;
    }

    private JSONObject createInvitationJson(String numberForInvite) {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("ContactNumberForInvite", numberForInvite);
            jobj.put("RequestorId", AppContext.context.loginId);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jobj;
    }


    private void SendInvite() {

        //showProgressBar();
        contactsWS.sendInvite(mInviteJasonObj, new OnAPICallCompleteListener<JSONObject>() {

            @Override
            public void apiCallSuccess(JSONObject response) {
                ProgressBar.hideProgressBar();

                Toast.makeText(mContext,
                        "Invitation Sent",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void apiCallFailure() {
                ProgressBar.hideProgressBar();
                Toast.makeText(mContext,
                        "Failed to send invite. Please try agan later.", Toast.LENGTH_SHORT).show();
            }
        });
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
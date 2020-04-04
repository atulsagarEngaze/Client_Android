package com.redtop.engaze.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.R;
import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.utility.ProgressBar;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.domain.service.ParticipantService;

public class HomeRunningEventListAdapter extends ArrayAdapter<Event> {
    public List<Event> items;
    private Context mContext;
    private RunningEventAdapterCallback callback;


    public HomeRunningEventListAdapter(Context context, int resourceId,
                                       List<Event> items) {
        super(context, resourceId, items);
        this.mContext = context;
        this.items = items;
        this.callback = ((RunningEventAdapterCallback) context);
    }

    /*private view holder class*/
    private class ViewHolder {
        TextView txtEventName;
        TextView txtInitiator;
        TextView txtLeave;
        TextView txtEnd;
        TextView txtView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        final Event rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) mContext
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_home_running_event_list, null);
            holder = new ViewHolder();
            holder.txtEventName = (TextView) convertView.findViewById(R.id.event_name);//  rowItem.getName()
            holder.txtInitiator = (TextView) convertView.findViewById(R.id.event_initiator);
            holder.txtLeave = (TextView) convertView.findViewById(R.id.event_leave);
            holder.txtEnd = (TextView) convertView.findViewById(R.id.event_end);
            holder.txtView = (TextView) convertView.findViewById(R.id.event_view);
            convertView.setTag(holder);

        } else
            holder = (ViewHolder) convertView.getTag();
        final String eventId = rowItem.EventId;

        if (ParticipantService.isCurrentUserInitiator(rowItem.InitiatorId)) {
            holder.txtLeave.setVisibility(View.GONE);
            holder.txtEnd.setVisibility(View.VISIBLE);
        } else {
            holder.txtEnd.setVisibility(View.GONE);
            holder.txtLeave.setVisibility(View.VISIBLE);
        }

        holder.txtEventName.setText(rowItem.Name);
        holder.txtInitiator.setText("from " + rowItem.InitiatorName);

        holder.txtLeave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ProgressBar.showProgressBar("Please wait");
                EventManager.leaveEvent(rowItem, new OnActionCompleteListner() {

                    @Override
                    public void actionComplete(Action action) {
                        rowItem.getCurrentParticipant().setAcceptanceStatus(AcceptanceStatus.DECLINED);
                        if (callback != null) {
                            callback.onEventLeaveClicked();
                        }
                        ProgressBar.hideProgressBar();
                    }
                }, AppContext.actionHandler);
            }
        });

        holder.txtEnd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ProgressBar.showProgressBar("Please wait");
                EventManager.endEvent(rowItem, new OnActionCompleteListner() {
                    @Override
                    public void actionComplete(Action action) {
                        if (callback != null) {
                            callback.onEventEndClicked();
                        }
                        ProgressBar.hideProgressBar();
                    }
                }, AppContext.actionHandler);
            }
        });

        holder.txtView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ProgressBar.showProgressBar("Please wait");
                Intent intent = new Intent(mContext, RunningEventActivity.class);
                intent.putExtra("EventId", rowItem.EventId);
                mContext.startActivity(intent);
                ProgressBar.hideProgressBar();
            }
        });
        return convertView;
    }

    public static interface RunningEventAdapterCallback {
        void onEventEndClicked();

        void onEventLeaveClicked();
    }
}
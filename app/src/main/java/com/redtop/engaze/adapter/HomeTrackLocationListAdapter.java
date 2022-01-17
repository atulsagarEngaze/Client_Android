package com.redtop.engaze.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.redtop.engaze.BaseEventActivity;
import com.redtop.engaze.HomeActivity;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.R;
import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.customeviews.CircularImageView;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.common.enums.TrackingType;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.common.utility.ProgressBar;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.TrackLocationMember;
import com.redtop.engaze.domain.manager.EventManager;
import com.redtop.engaze.domain.manager.ParticipantManager;
import com.redtop.engaze.domain.service.ParticipantService;
import com.redtop.engaze.fragment.ExtendEventFragment;

public class HomeTrackLocationListAdapter extends ArrayAdapter<TrackLocationMember> {

    protected static final int SNOOZING_REQUEST_CODE = 1;
    public List<TrackLocationMember> items;
    private Context mContext;
    private TrackLocationAdapterCallback callback;
    private TrackingType trackingType;

    public HomeTrackLocationListAdapter(Context context, int resourceId,
                                        List<TrackLocationMember> items, TrackingType trackingType) {
        super(context, resourceId, items);
        this.mContext = context;
        this.items = items;
        this.trackingType = trackingType;
        this.callback = ((TrackLocationAdapterCallback) context);
    }

    /*private view holder class*/
    private class ViewHolder {
        CircularImageView imageView;
        TextView txtName;
        TextView txtView;
        TextView txtPoke;
        TextView txtExtend;
        TextView txtStop;
        TextView txtTimeInfo;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final TrackLocationMember rowItem = getItem(position);
        final Event event = rowItem.getEvent();
        final ContactOrGroup cg = rowItem.getMember().contactOrGroup;
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_home_track_location_list, null);
            holder = new ViewHolder();

            holder.txtName = (TextView) convertView.findViewById(R.id.track_location_contact_name);
            holder.imageView = (CircularImageView) convertView.findViewById(R.id.track_location_contact_icon);
            holder.txtTimeInfo = (TextView) convertView.findViewById(R.id.track_location_time_info);
            holder.txtView = (TextView) convertView.findViewById(R.id.track_location_contact_view);
            holder.txtPoke = (TextView) convertView.findViewById(R.id.track_location_contact_poke);
            holder.txtExtend = (TextView) convertView.findViewById(R.id.track_location_contact_extend);
            holder.txtStop = (TextView) convertView.findViewById(R.id.track_location_contact_stop);
            convertView.setTag(holder);

        } else
            holder = (ViewHolder) convertView.getTag();

        if (trackingType == TrackingType.SELF) {
            holder.txtView.setVisibility(View.GONE);
        }

        //holder.txtName.setText(cg.getName());
        holder.txtName.setText(cg.getName());
        holder.imageView.setBackground(cg.getImageDrawable(mContext));
        holder.txtTimeInfo.setText(getStartTimeAndTimeLeftText(event, rowItem.getAcceptance()));
        if (rowItem.getMember().acceptanceStatus == AcceptanceStatus.Accepted) {
            holder.txtPoke.setVisibility(View.GONE);
        }
        holder.txtPoke.setOnClickListener(v -> ParticipantService.pokeParticipant(rowItem.getMember().userId, cg.getName(), event.eventId, AppContext.actionHandler));

        if (ParticipantService.isCurrentUserInitiator(event.initiatorId)) {
            holder.txtExtend.setVisibility(View.VISIBLE);
        }

        holder.txtExtend.setOnClickListener(v -> {
            ((HomeActivity) mContext).notificationSelectedEvent = event;

            FragmentManager fm =  ((HomeActivity) mContext).getSupportFragmentManager();
            ExtendEventFragment fragment = ExtendEventFragment.newInstance();
            fragment.show(fm, "Extend");
        });
        holder.txtStop.setOnClickListener(v -> {

            ProgressBar.showProgressBar("Please wait");
            if (ParticipantService.isCurrentUserInitiator(event.initiatorId)) {
                //Current user is initiator...so he can either end the event or remove the member.
                if (event.getParticipantCount() <= 2) {
                    //End the event since it is a 1 to 1 event
                    ProgressBar.showProgressBar("Please wait");
                    EventManager.endEvent(event, new OnActionCompleteListner() {
                        @Override
                        public void actionComplete(Action action) {
                            if (callback != null) {
                                callback.refreshTrackingEvents();
                            }
                            ProgressBar.hideProgressBar();
                        }
                    }, (msg, action) -> {
                        EventManager.refreshEventList(null, null);
                        AppContext.actionHandler.actionFailed(msg, action);
                    });

                } else {
                    //remove the row item member alone since there are still other members in the event.
                    ProgressBar.showProgressBar("Please wait");
                    event.ContactOrGroups.remove(event.getCurrentParticipant().contactOrGroup);

                    JSONObject jObj = ParticipantService.createUpdateParticipantsJSON(event.ContactOrGroups, event.eventId);
                    ParticipantManager.addRemoveParticipants(jObj, action -> {
                        //updateRecyclerViews();
                        if (callback != null) {
                            callback.refreshTrackingEvents();
                        }
                        //locationhandler.post(locationRunnable);
                        ProgressBar.hideProgressBar();
                    }, (msg, action) -> {
                        EventManager.refreshEventList(null, null);
                        AppContext.actionHandler.actionFailed(msg, action);
                    });
                }

            } else {
                //Current user is just a participant, so he can only leave the event.

                ProgressBar.showProgressBar("Please wait");
                EventManager.leaveEvent(event, action -> {
                    rowItem.getMember().acceptanceStatus = AcceptanceStatus.Rejected;
                    if (callback != null) {
                        callback.refreshTrackingEvents();
                    }
                    ProgressBar.hideProgressBar();
                }, (msg, action) -> {
                    EventManager.refreshEventList(null, null);
                    AppContext.actionHandler.actionFailed(msg, action);

                });

            }
        });

        holder.txtView.setOnClickListener(v -> {
            ProgressBar.showProgressBar("Please wait");
            Intent intent = new Intent(mContext, RunningEventActivity.class);
            intent.putExtra("EventId", event.eventId);
            intent.putExtra("EventTypeId", event.eventType);
            mContext.startActivity(intent);
            ProgressBar.hideProgressBar();
        });
        return convertView;
    }

    public static interface TrackLocationAdapterCallback {
        void refreshTrackingEvents();
    }

    @SuppressWarnings("static-access")
    private String getStartTimeAndTimeLeftText(Event event, AcceptanceStatus acceptanceStatus) {
        String timeInfoTxt = "";
        if (acceptanceStatus == acceptanceStatus.Accepted) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(sdf.parse(event.endTime));
                long diffMinutes = (calendar.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 60000;
                String timeLeft = DateUtil.getDurationText(diffMinutes);

                calendar = Calendar.getInstance();

                calendar.setTime(sdf.parse(event.startTime));
                String startTime = DateUtil.getTime(calendar);

                timeInfoTxt = String.format(mContext.getResources().getString(R.string.track_location_timeinfo_text),
                        startTime, timeLeft);
            } catch (ParseException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        } else {
            if (event.eventType == EventType.SHAREMYLOACTION) { // Share my Location
                timeInfoTxt = "Awaiting response to your Share My Location Request";
            } else {
                timeInfoTxt = "Awaiting response to your Track Buddy Request";
            }

        }
        return timeInfoTxt;
    }
}
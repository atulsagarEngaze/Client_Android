package com.redtop.engaze.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.redtop.engaze.BaseActivity;
import com.redtop.engaze.EventRecurrenceInfo;
import com.redtop.engaze.EventsActivity;
import com.redtop.engaze.R;
import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.ShowLocationActivity;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.common.constant.IntentConstants;
import com.redtop.engaze.common.customeviews.CircularImageView;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.manager.ContactAndGroupListManager;
import com.redtop.engaze.domain.service.ParticipantService;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.redtop.engaze.fontawesome.TextFont;
import com.redtop.engaze.fragment.ParticipantInfoFragment;

public class EventReCycleViewAdapter extends RecyclerView.Adapter<EventReCycleViewAdapter.EventViewHolder> {
    public List<Event> mEventList;
    private static Context mContext;
    private static ProgressDialog pDialog;

    public EventReCycleViewAdapter(
            List<Event> items, Context context) {
        mContext = context;
        this.mEventList = items;

        pDialog = new ProgressDialog(mContext);
        pDialog.setMessage("Saving Response...");
        pDialog.setCancelable(false);
    }

    @Override
    public void onBindViewHolder(final EventViewHolder viewHolder, final int i) {
        if (mEventList == null || mEventList.size() == 0) {
            return;
        }
        final Event ed = mEventList.get(i);
        viewHolder.event = ed;
        ContactOrGroup cg = ContactAndGroupListManager.getContact(ed.initiatorId);
        if (cg != null) {
            viewHolder.profileImage.setBackground(cg.getIconImageDrawable(mContext));
        } else {
            viewHolder.profileImage.setBackground(ContactOrGroup.getAppUserIconDrawable());
        }
        if (ParticipantService.isCurrentUserInitiator(ed.initiatorId)) {
            viewHolder.txtInitiator.setText("You");
        } else {
            viewHolder.txtInitiator.setText(ed.initiatorName);
        }

        //viewHolder.txtEventID.setText(ed.EventId);
        if (ed.destination == null) {
            viewHolder.rlLocationSection.setVisibility(View.GONE);
        } else {
            viewHolder.rlLocationSection.setVisibility(View.VISIBLE);

            viewHolder.txtLocation.setText(AppUtility.createTextForDisplay(ed.destination.getName(), Constants.EVENTS_ACTIVITY_LOCATION_TEXT_LENGTH));
        }
        String title = ed.name;
        title = title.substring(0, 1).toUpperCase() + title.substring(1);
        viewHolder.txtEventTile.setText(title);


        viewHolder.txtEventParticipant.setText(Integer.toString(ed.getParticipantCount()));
        viewHolder.imgEventTypeImage.setBackgroundResource(((EventsActivity) mContext).mEventTypeImages.getResourceId(5, -1));

        setDescriptionLayout(ed, viewHolder);

        SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date startDate = originalformat.parse(ed.startTime);
            Date endDate = originalformat.parse(ed.endTime);
            Date currentDate = Calendar.getInstance().getTime();

            if (endDate.getTime() >= currentDate.getTime() && currentDate.getTime() > startDate.getTime()) {
                viewHolder.runningStatus = true;
            } else {
                viewHolder.runningStatus = false;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);

            viewHolder.eventStartDayOfWeek = DateUtil.getDayOfWeek(cal);
            viewHolder.eventStartDayOfMonth = DateUtil.getDayOfMonth(cal);
            viewHolder.eventStartMonth = DateUtil.getShortMonth(cal);
            viewHolder.eventStartYear = DateUtil.getYear(cal);
            viewHolder.eventStartTime = DateUtil.getTime(cal);

            viewHolder.txtEventStartDayOfWeek.setText(viewHolder.eventStartDayOfWeek);
            viewHolder.txtEventStartDayOfMonth.setText(viewHolder.eventStartDayOfMonth);
            viewHolder.txtEventStartMonth.setText(viewHolder.eventStartMonth);
            viewHolder.txtEventStartYear.setText(viewHolder.eventStartYear);
            viewHolder.txtEventStartTime.setText(viewHolder.eventStartTime);

            viewHolder.txtEventTimeToStart.setText(setTimeToStartText(cal));

            cal.add(Calendar.MINUTE, ed.tracking.getOffSetInMinutes() * -1);


            if (ed.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.Accepted && cal.getTime().getTime() - currentDate.getTime() < 0) {
                viewHolder.trackingStatus = true;
                viewHolder.imgEventTrackingOn.setVisibility(View.VISIBLE);
            } else {
                viewHolder.trackingStatus = false;
                viewHolder.imgEventTrackingOn.setVisibility(View.GONE);
            }

            Calendar calEndDate = Calendar.getInstance();
            calEndDate.setTime(endDate);

            viewHolder.eventEndDayOfWeek = DateUtil.getDayOfWeek(calEndDate);
            viewHolder.eventEndDayOfMonth = DateUtil.getDayOfMonth(calEndDate);
            viewHolder.eventEndMonth = DateUtil.getShortMonth(calEndDate);
            viewHolder.eventEndYear = DateUtil.getYear(calEndDate);
            viewHolder.eventEndTime = DateUtil.getTime(calEndDate);
            if (!(viewHolder.eventEndDayOfMonth.equals(viewHolder.eventStartDayOfMonth)
                    && viewHolder.eventEndMonth.equals(viewHolder.eventStartMonth)
                    && viewHolder.eventEndYear.equals(viewHolder.eventStartYear))) {
                String dateToAppend = viewHolder.eventEndMonth + " " + viewHolder.eventEndDayOfMonth + " " + viewHolder.eventEndYear;
                viewHolder.txtEventEndTime.setText(viewHolder.eventEndTime + ", " + dateToAppend);
            } else {
                viewHolder.txtEventEndTime.setText(viewHolder.eventEndTime);
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (ed.IsRecurrence.equals("true")) {
            viewHolder.btnRecurrence.setVisibility(View.VISIBLE);
            viewHolder.btnRecurrence.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, EventRecurrenceInfo.class);
                    intent.putExtra("RecurrenceType", ed.RecurrenceType);
                    intent.putExtra("NumberOfOccurences", ed.NumberOfOccurencesLeft);// ed.getNumberOfOccurences());
                    intent.putExtra("FrequencyOfOcuurence", ed.FrequencyOfOccurence);
                    intent.putExtra("Recurrencedays", ed.RecurrenceDays);
                    intent.putExtra("RecurrenceDayOfMonth", viewHolder.eventStartDayOfMonth);
                    mContext.startActivity(intent);

                }
            });
        } else {
            viewHolder.btnRecurrence.setVisibility(View.GONE);
        }

        viewHolder.rlLocationSection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, ShowLocationActivity.class);
                intent.putExtra(IntentConstants.DESTINATION_LOCATION, ed.destination.getName());
                intent.putExtra(IntentConstants.DESTINATION_ADDRESS, ed.destination.getAddress());
                intent.putExtra(IntentConstants.DESTINATION_LATLANG, new LatLng(ed.destination.getLatitude(), ed.destination.getLongitude()));

                mContext.startActivity(intent);

                if (((EventsActivity) mContext).mActionMode != null) {
                    ((EventsActivity) mContext).mActionMode.finish();
                }
            }
        });

        viewHolder.llParticipants.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed.participants != null) {
                    FragmentManager fm = ((BaseActivity) mContext).getSupportFragmentManager();
                    ParticipantInfoFragment fragment = ParticipantInfoFragment.newInstance(RunningEventActivity.class.getName(), ed.initiatorId, ed.eventId, ed.participants);
                    fragment.show(fm, "ParticipantInfo");

                    if (((EventsActivity) mContext).mActionMode != null) {
                        ((EventsActivity) mContext).mActionMode.finish();

                    }
                }
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            Drawable originalDrawable = viewHolder.imgEventTime.getBackground();
            Drawable wrappedDrawable = DrawableCompat.wrap(originalDrawable);
            DrawableCompat.setTint(wrappedDrawable, mContext.getResources().getColor(R.color.icon));
            viewHolder.imgEventTime.setBackground(wrappedDrawable);

            originalDrawable = viewHolder.imgParticipants.getBackground();
            wrappedDrawable = DrawableCompat.wrap(originalDrawable);
            DrawableCompat.setTint(wrappedDrawable, mContext.getResources().getColor(R.color.icon));
            viewHolder.imgParticipants.setBackground(wrappedDrawable);

            originalDrawable = viewHolder.imgEventLocation.getBackground();
            wrappedDrawable = DrawableCompat.wrap(originalDrawable);
            DrawableCompat.setTint(wrappedDrawable, mContext.getResources().getColor(R.color.icon));
            viewHolder.imgEventLocation.setBackground(wrappedDrawable);

            originalDrawable = viewHolder.imgEventTypeImage.getBackground();
            wrappedDrawable = DrawableCompat.wrap(originalDrawable);
            DrawableCompat.setTint(wrappedDrawable, mContext.getResources().getColor(R.color.icon));
            viewHolder.imgEventTypeImage.setBackground(wrappedDrawable);

        }
    }

    private void setDescriptionLayout(Event ed, EventViewHolder viewHolder) {
        if (ed.description.isEmpty()) {
            viewHolder.llEventDescription.setVisibility(View.GONE);
        } else {
            viewHolder.llEventDescription.setVisibility(View.VISIBLE);
            final String description = ed.description;
            viewHolder.txtEventDesc.setText(description);
        }
    }

    private String setTimeToStartText(Calendar startCal) {

        String durationText = DateUtil.getDurationText((startCal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 60000);
        if (durationText.equals("0") || durationText.equals("")) {
            durationText = "RUNNING";
        }

        return durationText;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.item_event_list, viewGroup, false);
        return new EventViewHolder(itemView);
    }


    public class EventViewHolder extends RecyclerView.ViewHolder {
        public EventViewHolder(final View itemView) {
            super(itemView);

            //itemView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            this.rlDateSection = (RelativeLayout) itemView.findViewById(R.id.event_datesection);
            this.rlLocationSection = (RelativeLayout) itemView.findViewById(R.id.rl_event_location);
            this.imageView = (ImageView) itemView.findViewById(R.id.imageView);
            //this.txtEventID = (TextView) itemView.findViewById(R.id.txt_event_id);
            this.txtEventTile = (TextView) itemView.findViewById(R.id.txt_event_title);
            this.txtEventDesc = (TextView) itemView.findViewById(R.id.txt_event_Desc);
            this.txtLocation = (TextView) itemView.findViewById(R.id.txt_location);
            this.txtEventEndTime = (TextView) itemView.findViewById(R.id.txt_event_end_time);
            this.txtInitiator = (TextView) itemView.findViewById(R.id.txt_initiator_value);

            this.txtEventStartDayOfWeek = (TextView) itemView.findViewById(R.id.txt_event_day);
            this.txtEventStartDayOfMonth = (TextView) itemView.findViewById(R.id.txt_event_date);
            this.txtEventStartMonth = (TextView) itemView.findViewById(R.id.txt_event_month);
            this.txtEventStartYear = (TextView) itemView.findViewById(R.id.txt_event_year);
            this.txtEventStartTime = (TextView) itemView.findViewById(R.id.txt_event_start_time);
            this.txtEventParticipant = (TextView) itemView.findViewById(R.id.txt_event_participant);
            this.llParticipants = (RelativeLayout) itemView.findViewById(R.id.ll_participants);
            this.txtEventTimeToStart = (TextView) itemView.findViewById(R.id.txt_event_timeToStart);
            //this.txtEventAcceptanceStatus = (TextView)itemView.findViewById(R.id.txt_event_acceptance_status);
            this.imgEventTime = (ImageView) itemView.findViewById(R.id.ic_event_time);
            this.imgEventLocation = (ImageView) itemView.findViewById(R.id.ic_event_location);

            this.llEventDescription = (RelativeLayout) itemView.findViewById(R.id.ll_event_description);
            this.imgParticipants = (ImageView) itemView.findViewById(R.id.ic_participant);
            this.imgEventTypeImage = (ImageView) itemView.findViewById(R.id.img_event_type);
            this.profileImage = (CircularImageView) itemView.findViewById(R.id.host_contact_icon);
            this.imgEventTrackingOn = (ImageView) itemView.findViewById(R.id.ic_event_tracking_on);
            this.llDetailRectangle = (LinearLayout) itemView.findViewById(R.id.ll_detail_rectangle);
            this.llDetailRectangle.setBackground(mContext.getResources().getDrawable(R.drawable.ripple_home_buttton));
            this.llDetailRectangle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (
                            event.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.Accepted &&

                                    trackingStatus) {
                        Intent intent = new Intent(mContext, RunningEventActivity.class);
                        intent.putExtra("EventId", event.eventId);
                        mContext.startActivity(intent);
                        if (((EventsActivity) mContext).mActionMode != null) {
                            ((EventsActivity) mContext).mActionMode.finish();
                        }
                    }
                }
            });

            llDetailRectangle.setOnLongClickListener(new View.OnLongClickListener() {
                // Called when the user long-clicks on someView
                public boolean onLongClick(View view) {

                    EventsActivity activity = ((EventsActivity) mContext);
                    if (activity.mActionMode != null) {
                        return false;
                    }
                    eventOptions();
                    view.setSelected(true);
                    return true;
                }
            });

            this.btnRecurrence = (TextFont) itemView.findViewById(R.id.btn_recurrence);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CardView view = (CardView) itemView;
                view.setCardBackgroundColor(Color.TRANSPARENT);
                //view.setCardElevation(0);
                view.setRadius(0);
                view.setMaxCardElevation(0);
                view.setPreventCornerOverlap(false);

            } else {
                itemView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            }
            this.rlDateSection.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    eventOptions();
                    v.setSelected(true);
                }
            });
        }

        protected void enableDisableContextMenuItems(Menu menu) {
            MenuItem itemMuteUnmute = menu.findItem(R.id.context_action_mute_unmute);
            MenuItem itemAccept = menu.findItem(R.id.context_action_accept);
            MenuItem itemDeclined = menu.findItem(R.id.context_action_decline);
            MenuItem itemEdit = menu.findItem(R.id.context_action_edit);
            MenuItem itemDelete = menu.findItem(R.id.context_action_delete);
            Drawable dr = null;
            if (event.IsMute != null && event.IsMute) {
                dr = ((EventsActivity) mContext).getResources().getDrawable(R.drawable.event_mute);
            } else {
                dr = ((EventsActivity) mContext).getResources().getDrawable(R.drawable.event_unmute);
            }

            itemMuteUnmute.setIcon(dr);

            if (this.event.getCurrentParticipant().userId.equalsIgnoreCase(this.event.initiatorId)) {
                itemAccept.setVisible(false);
                itemDeclined.setVisible(false);
                if (this.runningStatus || this.trackingStatus) {
                    itemEdit.setVisible(false);
                    itemDelete.setVisible(false);
                } else {
                    itemEdit.setVisible(true);
                    itemDelete.setVisible(true);
                }
            } else {
                itemDelete.setVisible(false);
                itemEdit.setVisible(false);
//				if(this.runningStatus || this.trackingStatus)
//				{
//					itemDelete.setVisible(false);
//				}
//				else
//				{
//					itemDelete.setVisible(true);
//				}
                if (this.event.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.Accepted) {
                    itemAccept.setVisible(false);
                    itemDeclined.setVisible(true);
                } else if (this.event.getCurrentParticipant().acceptanceStatus == AcceptanceStatus.Pending) {
                    itemAccept.setVisible(true);
                    itemDeclined.setVisible(true);
                } else {
                    itemAccept.setVisible(true);
                    itemDeclined.setVisible(false);
                }
            }
        }

        public void eventOptions() {
            EventsActivity activity = ((EventsActivity) mContext);
            llDetailRectangle.setBackground(activity.getResources().getDrawable(R.drawable.event_detail_rectangle_long_pressed));

            // Start the CAB using the ActionMode.Callback defined above
            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.event_list_toolbar);
            activity.mActionMode = toolbar.startActionMode(activity.mActionModeCallback);
            activity.mActionMode.setTag(event);
            activity.mActionMode.setTitle("");
            activity.mActionMode.setTitleOptionalHint(false);
            activity.mCurrentItem = itemView;
            Menu menu = activity.mActionMode.getMenu();
            enableDisableContextMenuItems(menu);
        }


        public ImageView imageView;
        //public TextView txtEventID;
        public TextView txtEventTile;
        public TextView txtEventDesc;
        public TextView txtInitiator;
        public TextView txtLocation;

        public TextView txtStartDate;
        public TextView txtEventEndTime;
        public TextView txtEventStartDayOfWeek;
        public TextView txtEventStartDayOfMonth;
        public TextView txtEventStartMonth;
        public TextView txtEventStartYear;
        public TextView txtEventStartTime;
        public TextView txtEventParticipant;
        public TextView txtEventTimeToStart;
        public RelativeLayout rlLocationSection;
        public LinearLayout llDetailRectangle;
        public RelativeLayout rlDateSection;

        public RelativeLayout llEventDescription;
        public Event event;
        public ImageView imgParticipants;
        public ImageView imgEventTypeImage;
        public ImageView imgEventTrackingOn;

        public ImageView imgEventTime;
        public ImageView imgEventLocation;

        public RelativeLayout llParticipants;
        public Boolean trackingStatus;
        public Boolean runningStatus;
        public CircularImageView profileImage;

        public String eventStartDayOfWeek;
        public String eventStartDayOfMonth;
        public String eventStartMonth;
        public String eventStartYear;
        public String eventStartTime;

        public String eventEndDayOfWeek;
        public String eventEndDayOfMonth;
        public String eventEndMonth;
        public String eventEndYear;
        public String eventEndTime;
        public TextFont btnRecurrence;

    }

    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return mEventList.size();
    }
}
package com.redtop.engaze.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.redtop.engaze.R;
import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.RunningEventLocationRefresh;
import com.redtop.engaze.common.customeviews.CircularImageView;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.domain.service.ParticipantService;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

@SuppressLint({"NewApi", "SimpleDateFormat"})
public class EventUserLocationAdapter extends RecyclerView.Adapter<EventUserLocationAdapter.UserLocationViewHolder> {
    public List<UsersLocationDetail> items;
    private static Context mContext;
    private static ProgressDialog pDialog;
    private static SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static Calendar cal = Calendar.getInstance();

    public EventUserLocationAdapter(
            List<UsersLocationDetail> items, Context context, String mEventId) {
        mContext = context;
        this.items = items;
        pDialog = new ProgressDialog(mContext);
        pDialog.setMessage("Saving Response...");
        pDialog.setCancelable(false);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(final UserLocationViewHolder viewHolder, final int i) {
        UsersLocationDetail ud = items.get(i);
        if (ud != null) {
            viewHolder.uld = ud;
            viewHolder.userName.setText(ud.userName);
            if (items.size() - 1 == i) {
                viewHolder.divider.setVisibility(View.GONE);
            } else {
                viewHolder.divider.setVisibility(View.VISIBLE);

            }
            if (ParticipantService.isParticipantCurrentUser(ud.userId)) {
                viewHolder.userOptionsImage.setVisibility(View.GONE);
            }

            viewHolder.profileImage.setBackground(ud.contactOrGroup.getImageDrawable(mContext));

            if (ud.acceptanceStatus== AcceptanceStatus.ACCEPTED) {
                if (ud.latitude.equals("")) {

                    viewHolder.rlActiveWithNoLocationUser.setVisibility(View.VISIBLE);
                    viewHolder.locationImage.setVisibility(View.GONE);
                    ;
                    viewHolder.rlActiveUser.setVisibility(View.GONE);
                    viewHolder.rlDeclinedUser.setVisibility(View.GONE);
                    viewHolder.rlNotRespondedUser.setVisibility(View.GONE);

                } else {

                    try {
                        viewHolder.rlActiveWithNoLocationUser.setVisibility(View.GONE);
                        viewHolder.rlActiveUser.setVisibility(View.VISIBLE);
                        viewHolder.rlDeclinedUser.setVisibility(View.GONE);
                        viewHolder.rlNotRespondedUser.setVisibility(View.GONE);

                        String displayText = ud.currentDisplayAddress;

                        if (displayText == null || displayText.equals("")) {
                            displayText = mContext.getString(R.string.label_runningEvent_acceptedLocationNotFound);
                        } else {
                            if (!ud.createdOn.equals("")) {
                                Date createdon = originalformat.parse(ud.createdOn);
                                cal.setTime(createdon);
                                viewHolder.lastSeenTime.setText("Last seen: " + DateUtil.getTime(cal));
                            }
                        }

                        viewHolder.nearBy.setText(AppUtility.createTextForLocationDisplay(displayText, 16, 2));

                    } catch (NumberFormatException | ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else {
                viewHolder.rlActiveWithNoLocationUser.setVisibility(View.GONE);
                viewHolder.rlActiveUser.setVisibility(View.GONE);
                viewHolder.rlDeclinedUser.setVisibility(View.GONE);
                viewHolder.rlNotRespondedUser.setVisibility(View.VISIBLE);
                viewHolder.locationImage.setVisibility(View.GONE);

                if (ud.acceptanceStatus == AcceptanceStatus.DECLINED) {
                    viewHolder.rlDeclinedUser.setVisibility(View.VISIBLE);
                    viewHolder.rlNotRespondedUser.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public UserLocationViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.item_user_location_list, viewGroup, false);
        return new UserLocationViewHolder(itemView);
    }


    public class UserLocationViewHolder extends RecyclerView.ViewHolder {

        public UserLocationViewHolder(final View itemView) {
            super(itemView);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CardView view = (CardView) itemView;
                view.setCardBackgroundColor(Color.TRANSPARENT);

                view.setRadius(0);
                view.setMaxCardElevation(0);
                view.setPreventCornerOverlap(false);

                view.setUseCompatPadding(false);
                view.setContentPadding(-15, -15, -15, -15);
            } else {
                itemView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            }
            //itemView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            this.locationImage = (ImageView) itemView.findViewById(R.id.user_tracking_status);
            this.rlActiveUser = (RelativeLayout) itemView.findViewById(R.id.rl_active_user);
            this.rlActiveWithNoLocationUser = (RelativeLayout) itemView.findViewById(R.id.rl_active_user_no_location);
            this.rlDeclinedUser = (RelativeLayout) itemView.findViewById(R.id.rl_declined_user);
            this.rlNotRespondedUser = (RelativeLayout) itemView.findViewById(R.id.rl_notresponded_user);
            this.container = (LinearLayout) itemView.findViewById(R.id.ll_cv_user_location);

            this.profileImage = (CircularImageView) itemView.findViewById(R.id.user_location_profile_image);

            this.userName = (TextView) itemView.findViewById(R.id.user_location_user_name);
            this.lastSeenTime = (TextView) itemView.findViewById(R.id.user_lastSeen);
            this.nearBy = (TextView) itemView.findViewById(R.id.user_currentKnownPlace);
            this.divider = (View) itemView.findViewById(R.id.user_location_divider);
            this.userOptionsImage = (ImageView) itemView.findViewById(R.id.user_location_more);
            this.userOptionsImage.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mContext instanceof RunningEventLocationRefresh) {
                        ((RunningEventLocationRefresh) mContext).userLocationMenuClicked(itemView, uld);
                    }
                }
            });


            this.container.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mContext instanceof RunningEventActivity) {
                        ((RunningEventActivity) mContext).userLocationItemClicked(v, uld);
                    }
                }
            });
            //
            //			itemView.setOnLongClickListener(new View.OnLongClickListener() {
            //				// Called when the user long-clicks on someView
            //				public boolean onLongClick(View view) {
            //					//TODO Should be rendering only current User Phone and SMS Options.
            //					EventDetail ed = InternalCaching.getEventFromCache(mEventId, mContext);
            //					if(ed.getMember(uld.getUserId()) != null)
            //					{
            //						EventMember mem = ed.getMember(uld.getUserId());
            //						Intent intent = new Intent(mContext, EventParticipantsInfo.class);
            //						intent.putExtra("EventMembers", new ArrayList<EventMember>(Arrays.asList(mem)));
            //						intent.putExtra("InitiatorID", ed.getInitiatorId());
            //						intent.putExtra("EventId", ed.EventId);
            //						mContext.startActivity(intent);
            //					}
            //					return true;
            //				}
            //			});
        }

        public LinearLayout container;
        private View divider;
        public CircularImageView profileImage;
        public TextView userName;
        public TextView nearBy;
        public TextView lastSeenTime;
        public UsersLocationDetail uld;
        public RelativeLayout rlActiveUser;
        public RelativeLayout rlDeclinedUser;
        public RelativeLayout rlNotRespondedUser;
        public RelativeLayout rlActiveWithNoLocationUser;
        public ImageView locationImage;
        public ImageView userOptionsImage;
    }

    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return items.size();
    }
}
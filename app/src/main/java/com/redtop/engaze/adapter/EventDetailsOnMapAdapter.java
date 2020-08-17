package com.redtop.engaze.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.redtop.engaze.BaseActivity;
import com.redtop.engaze.R;
import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.domain.service.EventService;
import com.redtop.engaze.fragment.ParticipantInfoFragment;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;


public class EventDetailsOnMapAdapter extends RecyclerView.Adapter<EventDetailsOnMapAdapter.UserEventDetailsViewHolder> {
    public List<UsersLocationDetail> items;
    private Context mContext;
    public Event mEvent;

    private static String TAG = EventDetailsOnMapAdapter.class.getName();

    public EventDetailsOnMapAdapter(List<UsersLocationDetail> dataSet,
                                    Context context, Event event) {
        this.items = dataSet;
        this.mContext = context;
        this.mEvent = event;
    }

    @Override
    public void onBindViewHolder(final UserEventDetailsViewHolder viewHolder, final int i) {
        UsersLocationDetail ud = items.get(i);
        viewHolder.ud = ud;
        viewHolder.imageView.setBackgroundResource(ud.imageID);
        viewHolder.dataText.setText(ud.dataText);


        if (ud.acceptanceStatus != null) {
            Drawable background = mContext.getResources().getDrawable(ud.imageID);
//			int color ;
//			switch (ud.getAcceptanceStatus()) {
//			case ACCEPTED:				
//				color = mContext.getResources().getColor(R.color.colorGreen);
//				
//				break;
//			case DECLINED:
//				color = mContext.getResources().getColor(Color.RED);					
//				break;
//			case PENDING:
//				color = Color.parseColor("#F7CB06");
//				break;
//			default:
//				color = Color.YELLOW;				
//				break;
//			}
//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//				Drawable originalDrawable = viewHolder.imageView.getBackground();
//				Drawable wrappedDrawable = DrawableCompat.wrap(originalDrawable);
//				DrawableCompat.setTint(wrappedDrawable, color);
//				viewHolder.imageView.setBackground(wrappedDrawable);
//			}
//			else{
//				viewHolder.imageView.setBackground(background); 
//				background.setColorFilter( new  PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
//			}
            viewHolder.imageView.setBackground(background);
            viewHolder.imageView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public UserEventDetailsViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.item_user_event_details, viewGroup, false);
        return new UserEventDetailsViewHolder(itemView);
    }


    public class UserEventDetailsViewHolder extends RecyclerView.ViewHolder {
        public UserEventDetailsViewHolder(View itemView) {
            super(itemView);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CardView view = (CardView) itemView;
                view.setCardBackgroundColor(Color.TRANSPARENT);
                view.setRadius(0);
                view.setMaxCardElevation(0);
                view.setPreventCornerOverlap(false);
            } else {
                itemView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            }
            //itemView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            this.imageView = (ImageView) itemView.findViewById(R.id.img_user_event_details);
            this.dataText = (TextView) itemView.findViewById(R.id.txt_user_event_details);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    switch (ud.imageID) {
                        case R.drawable.ic_timer_black_18dp:
                            if (mContext instanceof RunningEventActivity) {
                                ((RunningEventActivity) mContext).markerRecenter(null);
                            }
                            break;
                        case R.drawable.ic_hourglass_empty_black_18dp:
                            if (mContext instanceof RunningEventActivity) {
                                ((RunningEventActivity) mContext).showAllMarkers();
                            }
                            break;
                        case R.drawable.ic_user_declined:
                        case R.drawable.ic_user_pending:
                        case R.drawable.ic_user_accepted:
                            if (ud.dataText != "0") {
                                ((RunningEventActivity) mContext).mIsActivityPauseForDialog = true;
                                ArrayList<EventParticipant> mems = new ArrayList<EventParticipant>();
                                mems.addAll(mEvent.getParticipantsbyStatus(ud.acceptanceStatus));
                                if (EventService.isEventTrackBuddyEventForCurrentUser(mEvent)) {
                                    mems.remove(mEvent.getCurrentParticipant());
                                }

                                FragmentManager fm = ((BaseActivity) mContext).getSupportFragmentManager();
                                ParticipantInfoFragment fragment = ParticipantInfoFragment.newInstance(RunningEventActivity.class.getName(), mEvent.initiatorId, mEvent.eventId, mems);
                                fragment.show(fm, "ParticipantInfo");
                            }
                            break;
                        default:
                            break;
                    }
                }
            });
        }

        public ImageView imageView;
        public TextView dataText;
        public AcceptanceStatus as;
        public UsersLocationDetail ud;

    }


    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return items.size();
    }
}

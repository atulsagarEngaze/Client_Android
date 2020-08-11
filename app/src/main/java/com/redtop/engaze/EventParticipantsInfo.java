package com.redtop.engaze;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.redtop.engaze.adapter.CustomParticipantsInfoList;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.domain.EventParticipant;

@SuppressWarnings("deprecation")
public class EventParticipantsInfo extends BaseActivity {
    private TextView tvHeader;
    private ArrayList<EventParticipant> eventMembers;

    /**
     * Called when the activity is first created.
     */
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participantsinfo);
        tvHeader = (TextView) findViewById(R.id.ChooseCategoryHeader);
        eventMembers = (ArrayList<EventParticipant>) this.getIntent().getSerializableExtra("EventMembers");
        String source = this.getIntent().getStringExtra("source");
        String initiatorID = this.getIntent().getStringExtra("InitiatorID");
        String eventId = this.getIntent().getStringExtra("EventId");
        ListView list = (ListView) findViewById(R.id.list_event_participants);
        CustomParticipantsInfoList adapter = new CustomParticipantsInfoList(EventParticipantsInfo.this, eventMembers, initiatorID, eventId, source);
        if (source != null && source.equals(RunningEventActivity.class.getName())) {
            if (eventMembers.size() > 0) {
                if (eventMembers.get(0).acceptanceStatus == AcceptanceStatus.Accepted) {
                    tvHeader.setText(getResources().getString(R.string.accepted_members_header));
                } else if (eventMembers.get(0).acceptanceStatus == AcceptanceStatus.Pending) {
                    tvHeader.setText(getResources().getString(R.string.pending_members_header));
                } else if (eventMembers.get(0).acceptanceStatus == AcceptanceStatus.Declined) {
                    tvHeader.setText(getResources().getString(R.string.declined_members_header));
                }
            }
        }

        list.setAdapter(adapter);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        View view = getWindow().getDecorView();
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
        lp.gravity = Gravity.TOP;
        lp.y = AppUtility.dpToPx(98, this);
        getWindowManager().updateViewLayout(view, lp);
    }
}

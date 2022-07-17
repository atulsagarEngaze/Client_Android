package com.redtop.engaze.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.redtop.engaze.R;
import com.redtop.engaze.RunningEventActivity;
import com.redtop.engaze.adapter.CustomParticipantsInfoList;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.utility.PermissionRequester;
import com.redtop.engaze.domain.EventParticipant;

import java.util.ArrayList;

import static com.redtop.engaze.common.constant.RequestCode.Permission.CALL_PHONE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ParticipantInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ParticipantInfoFragment extends DialogFragment {

    private Context mContext;
    private TextView tvHeader;
    private ArrayList<EventParticipant> eventMembers;
    private String participantMobileNumber;
    public ParticipantInfoFragment() {
        // Required empty public constructor
    }

    public static ParticipantInfoFragment newInstance(String source, String InitiatorId, String eventId, ArrayList<EventParticipant> eventMembers) {
        ParticipantInfoFragment fragment = new ParticipantInfoFragment();
        Bundle args = new Bundle();
        args.putString("Source", source);
        args.putString("InitiatorId", InitiatorId);
        args.putString("EventId", eventId);
        args.putSerializable("EventMembers", eventMembers);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvHeader = view.findViewById(R.id.ChooseCategoryHeader);
        eventMembers = (ArrayList<EventParticipant>) getArguments().getSerializable("EventMembers");
        String source = getArguments().getString("Source");
        String initiatorID = getArguments().getString("InitiatorId");
        String eventId = getArguments().getString("EventId");
        ListView list = view.findViewById(R.id.list_event_participants);
        CustomParticipantsInfoList adapter = new CustomParticipantsInfoList(this, getActivity(), eventMembers, initiatorID, eventId, source);
        if (source != null && source.equals(RunningEventActivity.class.getName())) {
            if (eventMembers.size() > 0) {
                if (eventMembers.get(0).acceptanceStatus == AcceptanceStatus.Accepted) {
                    tvHeader.setText(getResources().getString(R.string.accepted_members_header));
                } else if (eventMembers.get(0).acceptanceStatus == AcceptanceStatus.Pending) {
                    tvHeader.setText(getResources().getString(R.string.pending_members_header));
                } else if (eventMembers.get(0).acceptanceStatus == AcceptanceStatus.Rejected) {
                    tvHeader.setText(getResources().getString(R.string.declined_members_header));
                }
            }
        }

        list.setAdapter(adapter);
    }

    public void onCallClick(String mobileNumber) {
        participantMobileNumber = mobileNumber;
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + participantMobileNumber));

        if (PermissionRequester.CheckPermission(new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE, this)) {
            mContext.startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        ArrayList<String> permissionNotGranted = PermissionRequester.permissionsNotGranted(permissions);
        if (permissionNotGranted.size() != 0) {

            Toast.makeText(mContext,
                    "App does not have permission to call.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        switch (requestCode) {
            case CALL_PHONE: {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + participantMobileNumber));
                mContext.startActivity(callIntent);;
                break;

            }
        }
        return;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        Window window = getDialog().getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

        window.setDimAmount(0.01f);
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.x = 300;
        params.y = 210;
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        window.setAttributes(params);
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_participant_info, container, false);
        setCancelable(true);
        return  rootView;
    }
}
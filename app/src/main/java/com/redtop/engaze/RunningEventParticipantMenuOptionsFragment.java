package com.redtop.engaze;

import java.util.ArrayList;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.redtop.engaze.Interface.DialogDismissListener;
import com.redtop.engaze.Interface.IActionHandler;
import com.redtop.engaze.adapter.NameImageAdapter;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.utility.PermissionRequester;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.NameImageItem;
import com.redtop.engaze.manager.EventManager;
import com.redtop.engaze.manager.ParticipantManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static com.redtop.engaze.common.constant.RequestCode.Permission.CALL_PHONE;

@SuppressWarnings("deprecation")
public class RunningEventParticipantMenuOptionsFragment extends DialogFragment implements OnItemClickListener, IActionHandler {

    protected ArrayList<NameImageItem> mUserMenuItems;
    private String mEventId;
    private String mUserName;
    private String mUserId;
    private String mobileno;
    private Event mEvent;
    private EventParticipant member;
    private Context mContext;
    public DialogDismissListener dialogDismissListener;

    @SuppressWarnings("unchecked")

    public static RunningEventParticipantMenuOptionsFragment newInstance(String userName, String userId, String eventId, Integer acceptanceStatus) {
        RunningEventParticipantMenuOptionsFragment fragment = new RunningEventParticipantMenuOptionsFragment();
        Bundle args = new Bundle();
        args.putString("UserName", userName);
        args.putString("UserId", userId);
        args.putString("EventId", eventId);
        args.putInt("AcceptanceStatus", acceptanceStatus);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroy() {
        dialogDismissListener.onDismiss();
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        mUserName = getArguments().getString("UserName");
        mUserId = getArguments().getString("UserId");
        mEventId = getArguments().getString("EventId");
        Integer acceptanceStatusId = getArguments().getInt("AcceptanceStatus", 0);
        AcceptanceStatus status = AcceptanceStatus.getStatus(acceptanceStatusId);
        mEvent = EventManager.getEvent(mEventId, true);
        member = mEvent.getParticipant(mUserId);
        mobileno = member.contactOrGroup.getRegisteredMobileNumber();

        mUserMenuItems = new ArrayList<>();

        String[] userOptions = getResources().getStringArray(R.array.running_event_user_options);
        TypedArray images = getResources().obtainTypedArray(R.array.running_event_user_options_image);
        for (int i = 0; i < userOptions.length; i++) {
            NameImageItem item = new NameImageItem(images.getResourceId(i, -1), userOptions[i], i);
            mUserMenuItems.add(item);
        }
        if (status == AcceptanceStatus.Rejected || status == AcceptanceStatus.Pending) {
            mUserMenuItems.remove(1);
        } else {
            mUserMenuItems.remove(0);
        }



        NameImageAdapter adapter = new NameImageAdapter(mContext,
                R.layout.item_name_image_row, mUserMenuItems);
        ListView listView = view.findViewById(R.id.user_menu_options);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        Window window = getDialog().getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);

        window.setDimAmount(0.01f);
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.x = 300;
        params.y = 210;
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        window.setAttributes(params);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_running_event_participant_menu_options, container, false);
        setCancelable(true);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

        NameImageItem item = (NameImageItem) arg0.getItemAtPosition(position);
        switch (item.getImageIndex()) {
            case 0:
                onUserLocationItemMenuItemPokeClicked();
                break;

            case 1:
                onUserLocationItemMenuItemAlertClicked();
                break;
            case 2:
                onUserLocationItemMenuItemWhatsappClicked();
                break;

            case 3:
                onUserLocationItemMenuItemCallClicked();
                break;
            default:
                break;
        }
    }

    private void onUserLocationItemMenuItemWhatsappClicked() {
        boolean isWhatsappInstalled = whatsappInstalledOrNot("com.whatsapp");
        if (isWhatsappInstalled) {
            Uri uri = Uri.parse("smsto:" + mobileno);
            Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);
            sendIntent.putExtra("sms_body", "Your text here!");
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        } else {
            Toast.makeText(mContext, "WhatsApp not Installed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean whatsappInstalledOrNot(String uri) {
        PackageManager pm = mContext.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    private void onUserLocationItemMenuItemCallClicked() {
        // TODO Auto-generated method stub
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + mobileno));

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
                callIntent.setData(Uri.parse("tel:" + mobileno));
                mContext.startActivity(callIntent);;
                break;

            }
        }
        return;
    }

    public void onUserLocationItemMenuItemAlertClicked() {

        EtaDistanceAlertHelper etaHelper = new EtaDistanceAlertHelper(mEventId, mUserName, mUserId, this);
        etaHelper.showSetAlertDialog();
    }

    public void onUserLocationItemMenuItemPokeClicked() {

        ParticipantManager.pokeParticipant(mUserId, mUserName, mEventId, this);
    }

    @Override
    public void actionFailed(String msg, Action action) {
        AppContext.actionHandler.actionFailed(msg, action);

        this.dismiss();
    }

    @Override
    public void actionComplete(Action action) {
        if (action != Action.SETTIMEBASEDALERT) {
            AppContext.actionHandler.actionComplete(action);
        }

        this.dismiss();
    }

    @Override
    public void actionCancelled(Action action) {
        if (action != Action.SETTIMEBASEDALERT) {
            AppContext.actionHandler.actionCancelled(action);
        }

        this.dismiss();
    }
}

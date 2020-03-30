package com.redtop.engaze.domain.service;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.redtop.engaze.ActionSuccessFailMessageActivity;
import com.redtop.engaze.BaseActivity1;
import com.redtop.engaze.Interface.OnActionCompleteListner;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.AppService;
import com.redtop.engaze.common.ContactAndGroupListManager;
import com.redtop.engaze.common.PreffManager;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.enums.AcceptanceStatus;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.EventDetail;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.manager.ParticipantManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class ParticipantService {

    private final static String TAG = ParticipantService.class.getName();


    public static void pokeParticipant(final String userId, String userName, final String eventId, final Context context){
        try {
            String lastPokedTime = PreffManager.getPref(userId, context);
            if(lastPokedTime != null){
                SimpleDateFormat originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Calendar lastCal = Calendar.getInstance();
                Date lastpokeDate = originalformat.parse(lastPokedTime);
                lastCal.setTime(lastpokeDate);
                long diff = (Calendar.getInstance().getTimeInMillis()- lastCal.getTimeInMillis())/60000;
                long pendingfrPoke = Constants.POKE_INTERVAL- diff;
                if(diff>= Constants.POKE_INTERVAL){
                    pokeAlert(userId,userName, eventId, context);
                }else {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.message_runningEvent_pokeInterval)+ pendingfrPoke + " minutes.",
                            Toast.LENGTH_LONG).show();
                    ((ActionSuccessFailMessageActivity)context).actionCancelled(Action.POKEPARTICIPANT);
                }
            }else {
                pokeAlert(userId,userName, eventId, context);
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void pokeAlert(final String userId, String userName, final String eventId, final Context context) {
        AlertDialog.Builder adb = null;
        adb = new AlertDialog.Builder(context);

        adb.setTitle("Poke");
        adb.setMessage("Do you want to poke " + userName + "?" +"\n"+ "You can poke again only after 15 minutes.");
        adb.setIcon(android.R.drawable.ic_dialog_alert);

        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Call Poke API
                pokeParticipants(userId, eventId, context);
            } });

        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ((ActionSuccessFailMessageActivity)context).actionCancelled(Action.POKEPARTICIPANT);
            } });
        adb.show();
    }

    private  static void pokeParticipants(final String userId, String eventId, final Context context) {
        JSONObject jobj = new JSONObject();
        String[] userList = {userId};
        JSONArray mJSONArray = new JSONArray(Arrays.asList(userList));
        EventDetail ed = InternalCaching.getEventFromCache(eventId, context);
        try {
            ((BaseActivity1)context).showProgressBar(context.getString(R.string.message_general_progressDialog));
            jobj.put("RequestorId", AppContext.getInstance().loginId);
            jobj.put("RequestorName", AppContext.getInstance().loginId);
            jobj.put("UserIdsForRemind", mJSONArray);
            jobj.put("EventName", ed.getName());
            jobj.put("EventId", ed.getEventId());

            ParticipantManager.pokeParticipants(context,jobj, new OnActionCompleteListner() {

                @Override
                public void actionComplete(Action action) {
                    SimpleDateFormat  originalformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date currentdate = Calendar.getInstance().getTime();
                    String currentTimestamp = originalformat.format(currentdate);
                    PreffManager.setPref(userId, currentTimestamp, context);
                    ((ActionSuccessFailMessageActivity)context).actionComplete(Action.POKEPARTICIPANT);
                }
            }, new OnActionFailedListner() {

                @Override
                public void actionFailed(String msg, Action action) {
                   ((ActionSuccessFailMessageActivity)context).actionFailed(msg, Action.POKEPARTICIPANT);
                }
            });

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ((ActionSuccessFailMessageActivity)context).actionFailed(null, Action.POKEPARTICIPANT);
        }
    }

    public static Boolean isNotifyUser( EventDetail event){
        if(event!=null && event.getCurrentParticipant().getAcceptanceStatus()== AcceptanceStatus.DECLINED){
            return false;
        }
        return true;
    }

    public static ArrayList<EventParticipant> parseMemberList(Context context, JSONArray jsonStr){
        EventParticipant mem = null;
        ArrayList<EventParticipant> list = new ArrayList<EventParticipant>();
        try {
            for (int i = 0; i < jsonStr.length(); i++) {
                JSONObject c = jsonStr.getJSONObject(i);

                mem = new EventParticipant(
                        AppService.convertNullToEmptyString(c.getString("UserId")),
                        AppService.convertNullToEmptyString(c.getString("ProfileName")),
                        AppService.convertNullToEmptyString(c.getString("MobileNumber")),
                        AcceptanceStatus.getStatus(c.getInt("EventAcceptanceStateId"))
                );
                ContactOrGroup cg = ContactAndGroupListManager.getContact(context, c.getString("UserId"));
                if(cg!=null){
                    mem.setProfileName(cg.getName());
                    mem.setContact(cg);
                }
                else{
                    mem.setProfileName("~" + mem.getProfileName());
                }
                mem.isUserLocationShared = c.getBoolean("IsUserLocationShared");
                list.add(mem);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;

    }

}

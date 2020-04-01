package com.redtop.engaze.domain.service;

import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.PreffManager;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.EventDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventParser {

    public static JSONObject createPokeAllContactsJSON(EventDetail ed) {
        JSONObject jobj = new JSONObject();

        try {
            jobj.put("RequestorId", AppContext.context.loginId);
            jobj.put("EventId", ed.getEventId());
            jobj.put("RequestorName", AppContext.context.loginName);
            jobj.put("EventName", ed.getName());
            jobj.put("EventId", ed.getEventId());
            //			jobj.put("ContactNumbersForRemind", conactsArray);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jobj;
    }

    public static  JSONObject createEventJson(EventDetail event)
    {
        String isUserLocationShared = "true";
        if (mEventTypeId ==100){
            isUserLocationShared = "false";
        }
        JSONObject jobj = new JSONObject();
        JSONObject userListJobj;
        JSONArray jsonarr = new JSONArray();
        Date endDate = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mStartDate);
        calendar.add(Calendar.MINUTE, mDurationOffset);
        endDate = calendar.getTime();

        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String start  = DateUtil.convertToUtcDateTime(parseFormat.format(mStartDate),parseFormat); //parseFormat.format(mStartDate);
        String end  = DateUtil.convertToUtcDateTime(parseFormat.format(endDate), parseFormat);//parseFormat.format(endDate);
        try {
            String userId;
            if(event.getContactOrGroups() !=null){
                for(ContactOrGroup cg : event.getContactOrGroups()){
                    userId = cg.getUserId();
                    userListJobj = new JSONObject();
                    if(userId != null && !userId.isEmpty()){
                        userListJobj.put("UserId", userId);
                        userListJobj.put("IsUserLocationShared", isUserLocationShared);
                    }
                    else{
                        userListJobj.put("MobileNumber", cg.getMobileNumber());
                    }
                    jsonarr.put(userListJobj);
                }
            }

            jobj.put("Name", event.getName());
            jobj.put("Description", event.getDescription());
            if(event.getEventId() !=null)
            {
                jobj.put("EventId", event.getEventId());
            }
            jobj.put("UserList", jsonarr);
            jobj.put("Duration", mDurationOffset);
            jobj.put("InitiatorId", AppContext.context.loginId);
            jobj.put("RequestorId", AppContext.context.loginId);
            jobj.put("EventStateId", "1");
            jobj.put("TrackingStateId", "1");
            jobj.put("IsTrackingRequired", "True");
            jobj.put("StartTime", start);
            jobj.put("EndTime",end);

            if(mDestinationPlace!=null)
            {
                jobj.put("DestinationLatitude", mDestinationPlace.getLatLang().latitude);
                jobj.put("DestinationLongitude", mDestinationPlace.getLatLang().longitude);
                jobj.put("DestinationAddress", mDestinationPlace.getAddress());
                //jobj.put("DestinationName", mDestinationPlace.getName());
                jobj.put( "DestinationName", mEventLocationTextView.getText());
            }
            else
            {
                jobj.put("DestinationLatitude", "");
                jobj.put("DestinationLongitude", "");
                jobj.put("DestinationAddress", "");
                jobj.put("DestinationName", "");
            }

            setReminderOffset();
            if(mReminder!=null){
                jobj.put("ReminderType", mReminder.getNotificationType());
            }
            jobj.put("ReminderOffset", "" + mReminderOffset + "");
            jobj.put("EventTypeId", "" + mEventTypeItem.getImageIndex());
            jobj.put("TrackingStopTime", "");
            setTrackingOffset();
            jobj.put("TrackingStartOffset",""+ mTrackingOffset + "");
            jobj.put("IsQuickEvent",mIsQuickEvent);
            if(mIsRecurrence.equals("true")){
                jobj.put("IsRecurring", true);
                jobj.put("RecurrenceCount",mNumberOfOccurences);
                jobj.put("RecurrenceFrequency",mFrequencyOfOcuurence);
                jobj.put("RecurrenceFrequencyTypeId",mRecurrenceType);
                if(mRecurrenceType.equals("2")){
                    String days ="";
                    for( int day : mRecurrencedays){
                        days += "," + Integer.toString(day);
                    }
                    days = days.substring(1);
                    jobj.put("RecurrenceDaysOfWeek",days);
                }
            }
            else{
                jobj.put("IsRecurring",false);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jobj;
    }

    public static List<EventDetail> parseEventDetailList(JSONArray jsonStr) {
        JSONArray eventDetailJsonArray = jsonStr;
        List<EventDetail> eventDetailList = new ArrayList<EventDetail>();
        String loginUser = AppContext.context.loginId;
        try {
            for (int i = 0; i < eventDetailJsonArray.length(); i++) {
                JSONObject c = eventDetailJsonArray.getJSONObject(i);
                EventDetail dt = new EventDetail(
                        ParticipantService.parseMemberList(c.getJSONArray("UserList")),
                        AppUtility.convertNullToEmptyString(c.getString("EventId")),
                        AppUtility.convertNullToEmptyString(c.getString("Name")),
                        AppUtility.convertNullToEmptyString(c.getString("EventTypeId")),
                        AppUtility.convertNullToEmptyString(c.getString("Description")),
                        AppUtility.convertNullToEmptyString(DateUtil.convertUtcToLocalDateTime(c.getString("StartTime"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))),
                        AppUtility.convertNullToEmptyString(DateUtil.convertUtcToLocalDateTime(c.getString("EndTime"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))),
                        AppUtility.convertNullToEmptyString(c.getString("Duration")),
                        AppUtility.convertNullToEmptyString(c.getString("InitiatorId")),
                        AppUtility.convertNullToEmptyString(c.getString("InitiatorName")),
                        AppUtility.convertNullToEmptyString(c.getString("EventStateId")),
                        AppUtility.convertNullToEmptyString(c.getString("TrackingStateId")),
                        c.getString("DestinationLatitude"),
                        c.getString("DestinationLongitude"),
                        c.getString("DestinationName"),
                        c.getString("DestinationAddress"),
                        c.getString("IsTrackingRequired"),
                        c.getString("ReminderOffset"),
                        //AppUtility.checkNull(c.getString("IsTrackingRequired")),
                        //AppUtility.checkNull(c.getString("ReminderOffset")),
                        //c.getString("ReminderType"),
                        "notification",
                        c.getString("TrackingStartOffset"),
                        c.getString("IsQuickEvent"));
                dt.setCurrentParticipant(dt.getMember(loginUser));
                dt.setIsRecurrence(c.getString("IsRecurring"));
                if (c.getString("IsRecurring").equals("true")) {
                    dt.setNumberOfOccurencesLeft(c.getString("RecurrenceRemaining"));
                    dt.setNumberOfOccurences(c.getString("RecurrenceCount"));
                    dt.setFrequencyOfOcuurence(c.getString("RecurrenceFrequency"));
                    dt.setRecurrenceType(c.getString("RecurrenceFrequencyTypeId"));

                    if (c.getString("RecurrenceFrequencyTypeId").equals("2")) {

                        ArrayList<String> strRecurrencedays = new ArrayList<String>(Arrays.asList(c.getString("RecurrenceDaysOfWeek")
                                .split(",")));
                        ArrayList<Integer> recurrencedays = new ArrayList<Integer>();
                        for (String strDay : strRecurrencedays) {
                            recurrencedays.add(Integer.parseInt(strDay));
                        }
                        dt.setRecurrenceDays(recurrencedays);
                    }
                    dt.setRecurrenceActualStartTime(DateUtil.convertUtcToLocalDateTime(c.getString("StartTime"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")));
                }

                eventDetailList.add(dt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eventDetailList;
    }
}

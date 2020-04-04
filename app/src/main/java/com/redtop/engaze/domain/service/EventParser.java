package com.redtop.engaze.domain.service;

import com.google.gson.reflect.TypeToken;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.DateUtil;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventParser {

    public static JSONObject createPokeAllContactsJSON(Event ed) {
        JSONObject jobj = new JSONObject();

        try {
            jobj.put("RequestorId", AppContext.context.loginId);
            jobj.put("EventId", ed.EventId);
            jobj.put("RequestorName", AppContext.context.loginName);
            jobj.put("EventName", ed.Name);
            jobj.put("EventId", ed.EventId);
            //			jobj.put("ContactNumbersForRemind", conactsArray);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jobj;
    }

    public static List<Event> parseEventDetailList(JSONArray jsonStr) {
        JSONArray eventDetailJsonArray = jsonStr;
        List<Event> eventList = new ArrayList<Event>();
        String loginUser = AppContext.context.loginId;
        Event event = null;


        try {
            for (int i = 0; i < eventDetailJsonArray.length(); i++) {
                eventList.add(AppContext.jsonParser.deserialize
                        (eventDetailJsonArray.getJSONObject(i).toString(),
                                Event.class));
            }

            for (Event ev : eventList) {
                ev.setCurrentParticipant(ev.getMember(AppContext.context.loginId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eventList;
    }
}
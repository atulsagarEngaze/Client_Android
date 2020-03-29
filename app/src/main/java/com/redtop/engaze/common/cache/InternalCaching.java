package com.redtop.engaze.common.cache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.EventDetail;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.domain.service.EventService;

import android.content.Context;

public final class InternalCaching{

	public static final String CACHE_EVENTS ="events";
	public static final String CACHE_TRACK_EVENTS ="trackevents";
	public static final String CACHE_CONTACTS ="contacts";
	public static final String CACHE_REGISTERED_CONTACTS ="registeredcontacts";
	public static final String CACHE_DESTINATIONS ="destinations";

	private InternalCaching() {}

	private static void writeObject(Context context, String key, Object object) {
		FileOutputStream fos;
		try {
			fos = context.openFileOutput(key, Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static Object readObject(Context context, String key)
	{
		FileInputStream fis;
		Object object = null;
		try {
			fis = context.openFileInput(key);
			ObjectInputStream ois = new ObjectInputStream(fis);
			object = ois.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return object;
	}

	public static void initializeCache(Context context){
		Hashtable<String, EventDetail> cachedEvents = new Hashtable<String, EventDetail>();
		writeObject(context, CACHE_EVENTS, cachedEvents);
		writeObject(context, CACHE_TRACK_EVENTS, cachedEvents);
		Hashtable<String, ContactOrGroup> contacts = new Hashtable<String, ContactOrGroup>();
		writeObject(context, CACHE_CONTACTS, contacts);
		writeObject(context, CACHE_REGISTERED_CONTACTS, contacts);
		ArrayList<EventPlace> cachedD = new ArrayList<EventPlace>();
		writeObject(context, CACHE_DESTINATIONS, cachedD);
	}

	@SuppressWarnings("unchecked")
	public static EventDetail getEventFromCache(String eventId, Context context){
		EventDetail event = null;
		Hashtable<String, EventDetail> cachedEntries = getcachedEventsHashMap(context);
		event = cachedEntries.get(eventId);
		if (event==null)
		{
			cachedEntries = getcachedTrackEventsHashMap(context);
			event = cachedEntries.get(eventId);
		}
		return event;	
	}	

	@SuppressWarnings("unchecked")
	public static List<EventDetail> getEventListFromCache( Context context){		
		ArrayList<EventDetail> events = null;
		Hashtable<String, EventDetail> cachedEntries = (Hashtable<String, EventDetail>) readObject(context, CACHE_EVENTS);
		if(cachedEntries!=null && cachedEntries.size()!=0){
			events = new ArrayList<EventDetail>(cachedEntries.values());
		}
		else{
			events = new ArrayList<EventDetail>() ;
		}
		return events;	
	}

	@SuppressWarnings("unchecked")
	public static List<EventDetail> getTrackEventListFromCache( Context context){		
		List<EventDetail> events = null;
		Hashtable<String, EventDetail> cachedEntries = (Hashtable<String, EventDetail>) readObject(context, CACHE_TRACK_EVENTS);
		if(cachedEntries!=null &&  cachedEntries.size()!=0){
			events = new ArrayList<EventDetail>(cachedEntries.values());
		}
		else{
			events = new ArrayList<EventDetail>();
		}
		return events;	
	}

	@SuppressWarnings("unchecked")
	private static Hashtable<String, EventDetail> getcachedEventsHashMap(Context context){

		Hashtable<String, EventDetail> cachedEntries = (Hashtable<String, EventDetail>) readObject(context, CACHE_EVENTS);
		return cachedEntries;
	}

	@SuppressWarnings("unchecked")
	private static Hashtable<String, EventDetail> getcachedTrackEventsHashMap(Context context){

		Hashtable<String, EventDetail> cachedEntries = (Hashtable<String, EventDetail>) readObject(context, CACHE_TRACK_EVENTS);
		return cachedEntries;
	}

	@SuppressWarnings("unchecked")
	public static void saveEventToCache(EventDetail event, Context context){
		Hashtable<String, EventDetail> cachedEntries;	
		String eventTypeId = event.getEventTypeId();
		if(eventTypeId.equals("100")||eventTypeId.equals("200")){
			cachedEntries = getcachedTrackEventsHashMap(context);
			cachedEntries.put(event.getEventId(), event);
			writeObject(context, CACHE_TRACK_EVENTS, cachedEntries);
		}
		else{
			cachedEntries = getcachedEventsHashMap(context);
			cachedEntries.put(event.getEventId(), event);
			writeObject(context, CACHE_EVENTS, cachedEntries);
		}				
	}

	public static void removeEventFromCache(String eventId, Context context){
		Hashtable<String, EventDetail> cachedEntries = getcachedEventsHashMap(context);	
		if(cachedEntries.containsKey(eventId)){
			cachedEntries.remove(eventId);
			writeObject(context, CACHE_EVENTS, cachedEntries);
		}
		else{
			cachedEntries = getcachedTrackEventsHashMap(context);	
			if(cachedEntries.containsKey(eventId)){
				cachedEntries.remove(eventId);
				writeObject(context, CACHE_TRACK_EVENTS, cachedEntries);
			}
		}
	}

	public static void removeEventsFromCache(List<String>eventIdList, Context context){
		Hashtable<String, EventDetail> cachedEntries = getcachedEventsHashMap(context);
		Hashtable<String, EventDetail>  cachedEntriesForTE = getcachedTrackEventsHashMap(context);
		int size = cachedEntries.size();
		int sizeTE = cachedEntriesForTE.size();
		for(String evenId : eventIdList){
			if(cachedEntries.containsKey(evenId)){
				cachedEntries.remove(evenId);
			}
			else if(cachedEntriesForTE.containsKey(evenId)){
				cachedEntriesForTE.remove(evenId);
			}
		}
		if(size!=cachedEntries.size()){
			writeObject(context, CACHE_EVENTS, cachedEntries);
		}
		if(sizeTE!=cachedEntriesForTE.size()){
			writeObject(context, CACHE_TRACK_EVENTS, cachedEntriesForTE);
		}
	}

	public static void RemovePastEvents(Context context) {

		List<EventDetail> eventDetailList = getEventListFromCache(context);
		if(eventDetailList!=null){
			List<String> tobeRemoved = new  ArrayList<String>();
			for(EventDetail event : eventDetailList){
				if(EventService.isEventPast(context,event)){
					tobeRemoved.add(event.getEventId());
				}
			}
			if(tobeRemoved.size()<0){
				removeEventsFromCache(tobeRemoved, context);				
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void saveEventListToCache(List<EventDetail> events, Context context){
		if(events!=null && events.size()>0){
			EventDetail ed;
			String eventId;
			Hashtable<String, EventDetail> oldcachedEntries = getcachedEventsHashMap(context);
			Hashtable<String, EventDetail> oldcachedEntriesForTE = getcachedTrackEventsHashMap(context);
			Hashtable<String, EventDetail> cachedEntries = new Hashtable<String, EventDetail>();
			Hashtable<String, EventDetail> cachedEntriesForTE = new Hashtable<String, EventDetail>();
			int eventTypeId;
			for(EventDetail event : events){
				eventId = event.getEventId();
				eventTypeId = Integer.parseInt(event.getEventTypeId());
				if(eventTypeId==100 || eventTypeId==200){
					ed = oldcachedEntriesForTE.get(eventId);
				}
				else{
					ed = oldcachedEntries.get(eventId);
				}				
				if(ed!=null){
					event.setUsersLocationDetailList(ed.getUsersLocationDetailList());
					event.acceptNotificationid = ed.acceptNotificationid;
					event.snoozeNotificationId = ed.snoozeNotificationId;
					event.notificationIds = ed.notificationIds;
					event.isMute = ed.isMute;
					event.isDistanceReminderSet = ed.isDistanceReminderSet;	
					ArrayList<EventParticipant>newMembers = new ArrayList<EventParticipant>();
					ArrayList<EventParticipant> reminderMems = ed.getReminderEnabledMembers();
					if(reminderMems != null && reminderMems.size()>0){
						for(EventParticipant mem : reminderMems){
							EventParticipant newMem = event.getMember(mem.getUserId());
							if(newMem!=null){
								newMem.setDistanceReminderDistance(mem.getDistanceReminderDistance());
								newMem.setDistanceReminderId(mem.getDistanceReminderId());
								newMem.setReminderFrom(mem.getReminderFrom());
								newMembers.add(newMem);
							}
						}
						if(newMembers.size()>0){
							event.setReminderEnabledMembers(newMembers);
							event.isDistanceReminderSet = true;
						}
					}
				}
				if(eventTypeId==100 || eventTypeId==200){
					cachedEntriesForTE.put(eventId, event);
				}
				else{
					cachedEntries.put(eventId, event);
				}
			}

			writeObject(context, CACHE_EVENTS, cachedEntries);
			writeObject(context, CACHE_TRACK_EVENTS, cachedEntriesForTE);
		}
	}

	@SuppressWarnings("unchecked")
	public static void saveContactListToCache(HashMap<String, ContactOrGroup> contacts, Context context){
		if(contacts!=null){				
			writeObject(context, CACHE_CONTACTS, contacts);
		}
	}	

	@SuppressWarnings("unchecked")
	public static void saveRegisteredContactListToCache(Hashtable<String, ContactOrGroup> contacts, Context context){
		if(contacts!=null){				
			writeObject(context, CACHE_REGISTERED_CONTACTS, contacts);
		}
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String,ContactOrGroup> getContactListFromCache( Context context){		
		HashMap<String, ContactOrGroup> cachedEntries = (HashMap<String, ContactOrGroup>) readObject(context, CACHE_CONTACTS);
		return cachedEntries;	
	}

	@SuppressWarnings("unchecked")
	public static Hashtable<String, ContactOrGroup> getRegisteredContactListFromCache( Context context){
		try{
			Hashtable<String, ContactOrGroup> cachedEntries = (Hashtable<String, ContactOrGroup>) readObject(context, CACHE_REGISTERED_CONTACTS);
			return cachedEntries;
		}
		catch(ClassCastException ex){
			Hashtable<String, ContactOrGroup> cachedEntries = new Hashtable<String, ContactOrGroup>();
			ArrayList<ContactOrGroup> CacheArray = (ArrayList<ContactOrGroup>)readObject(context, CACHE_REGISTERED_CONTACTS);
			for(ContactOrGroup cg : CacheArray){
				cachedEntries.put(cg.getUserId(), cg);
			}
			writeObject(context, CACHE_REGISTERED_CONTACTS, cachedEntries);
			return cachedEntries;
		}
	}

	@SuppressWarnings("unchecked")	
	public static ArrayList<EventPlace> getDestinationListFromCache(Context context) {
		ArrayList<EventPlace> cachedEntries = (ArrayList<EventPlace>) readObject(context, CACHE_DESTINATIONS);
		return cachedEntries;
	}

	public static void saveDestinationListToCache(Context context, ArrayList<EventPlace> locations) {		
		if(locations!=null){				
			writeObject(context, CACHE_DESTINATIONS, locations);
		}		
	}
}

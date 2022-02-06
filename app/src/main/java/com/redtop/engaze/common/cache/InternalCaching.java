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
import java.util.List;

import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.enums.EventType;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.Event;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.EventPlace;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.domain.service.EventService;

import android.content.Context;

public final class InternalCaching {

    public static final String CACHE_EVENTS = "events";
    public static final String CACHE_TRACK_EVENTS = "trackevents";
    public static final String CACHE_CONTACTS = "contacts";
    public static final String CACHE_REGISTERED_CONTACTS = "registeredcontacts";
    public static final String CACHE_DESTINATIONS = "destinations";

    private InternalCaching() {
    }

    private static void writeObject(String key, Object object) {
        FileOutputStream fos;
        try {
            fos = AppContext.context.openFileOutput(key, Context.MODE_PRIVATE);
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

    private static Object readObject(String key) {
        FileInputStream fis;
        Object object = null;
        try {
            fis = AppContext.context.openFileInput(key);
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

    public static void initializeCache() {
        writeObject(CACHE_EVENTS, new HashMap<String, Event>());
        writeObject(CACHE_TRACK_EVENTS, new HashMap<String, Event>());
        writeObject(CACHE_CONTACTS, new ArrayList<ContactOrGroup>());
        writeObject(CACHE_REGISTERED_CONTACTS, new HashMap<String, ContactOrGroup>());
        writeObject(CACHE_DESTINATIONS, new ArrayList<EventPlace>());
    }

    @SuppressWarnings("unchecked")
    public static Event getEventFromCache(String eventId) {
        Event event = null;
        HashMap<String, Event> cachedEntries = getCachedEventHashMap();
        event = cachedEntries.get(eventId);
        if (event == null) {
            cachedEntries = getCachedTrackEventHashMap();
            event = cachedEntries.get(eventId);
        }
        return event;
    }

    @SuppressWarnings("unchecked")
    public static List<Event> getEventListFromCache() {
        ArrayList<Event> events;
        HashMap<String, Event> cachedEntries = (HashMap<String, Event>) readObject(CACHE_EVENTS);
        if (cachedEntries != null && cachedEntries.size() != 0) {
            events = new ArrayList<>(cachedEntries.values());
        } else {
            events = new ArrayList<>();
        }
        return events;
    }

    @SuppressWarnings("unchecked")
    public static List<Event> getTrackEventListFromCache() {
        List<Event> events;
        HashMap<String, Event> cachedEntries = (HashMap<String, Event>) readObject(CACHE_TRACK_EVENTS);
        if (cachedEntries != null && cachedEntries.size() != 0) {
            events = new ArrayList<>(cachedEntries.values());
        } else {
            events = new ArrayList<>();
        }
        return events;
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, Event> getCachedEventHashMap() {

       return (HashMap<String, Event>) readObject(CACHE_EVENTS);
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, Event> getCachedTrackEventHashMap() {

        HashMap<String, Event> cachedEntries = (HashMap<String, Event>) readObject(CACHE_TRACK_EVENTS);
        return cachedEntries;
    }

    @SuppressWarnings("unchecked")
    public static void saveEventToCache(Event event) {
        HashMap<String, Event> cachedEntries;
        EventType eventType = event.eventType;
        if (eventType == EventType.SHAREMYLOACTION || eventType == EventType.TRACKBUDDY) {
            cachedEntries = getCachedTrackEventHashMap();
            cachedEntries.put(event.eventId, event);
            writeObject(CACHE_TRACK_EVENTS, cachedEntries);
        } else {
            cachedEntries = getCachedEventHashMap();
            cachedEntries.put(event.eventId, event);
            writeObject(CACHE_EVENTS, cachedEntries);
        }
    }

    public static void removeEventFromCache(String eventId) {
        HashMap<String, Event> cachedEntries = getCachedEventHashMap();
        if (cachedEntries.containsKey(eventId)) {
            cachedEntries.remove(eventId);
            writeObject(CACHE_EVENTS, cachedEntries);
        } else {
            cachedEntries = getCachedTrackEventHashMap();
            if (cachedEntries.containsKey(eventId)) {
                cachedEntries.remove(eventId);
                writeObject(CACHE_TRACK_EVENTS, cachedEntries);
            }
        }
    }

    public static void removeEventsFromCache(List<String> eventIdList) {
        HashMap<String, Event> cachedEntries = getCachedEventHashMap();
        HashMap<String, Event> cachedEntriesForTE = getCachedTrackEventHashMap();
        int size = cachedEntries.size();
        int sizeTE = cachedEntriesForTE.size();
        for (String evenId : eventIdList) {
            if (cachedEntries.containsKey(evenId)) {
                cachedEntries.remove(evenId);
            } else if (cachedEntriesForTE.containsKey(evenId)) {
                cachedEntriesForTE.remove(evenId);
            }
        }
        if (size != cachedEntries.size()) {
            writeObject(CACHE_EVENTS, cachedEntries);
        }
        if (sizeTE != cachedEntriesForTE.size()) {
            writeObject(CACHE_TRACK_EVENTS, cachedEntriesForTE);
        }
    }

    @SuppressWarnings("unchecked")
    public static void saveEventListToCache(List<Event> events) {
        if (events != null && events.size() > 0) {
            Event ed;
            String eventId;
            HashMap<String, Event> oldcachedEntries = getCachedEventHashMap();
            HashMap<String, Event> oldcachedEntriesForTE = getCachedTrackEventHashMap();
            HashMap<String, Event> cachedEntries = new HashMap<String, Event>();
            HashMap<String, Event> cachedEntriesForTE = new HashMap<String, Event>();
            EventType eventType;
            for (Event event : events) {
                eventId = event.eventId;
                eventType = event.eventType;
                if (eventType == EventType.SHAREMYLOACTION || eventType == EventType.TRACKBUDDY) {
                    ed = oldcachedEntriesForTE.get(eventId);
                } else {
                    ed = oldcachedEntries.get(eventId);
                }
                if (ed != null) {
                    event.UsersLocationDetailList = new ArrayList<UsersLocationDetail>();
                    if(ed.UsersLocationDetailList!=null) {
                        for (UsersLocationDetail ud : ed.UsersLocationDetailList) {
                            event.UsersLocationDetailList.add(
                                    AppContext.jsonParser.deserialize(AppContext.jsonParser.Serialize(ud), UsersLocationDetail.class));
                        }
                    }

                    event.AcceptNotificationId = ed.AcceptNotificationId;
                    event.SnoozeNotificationId = ed.SnoozeNotificationId;
                    event.NotificationIds = ed.NotificationIds;
                    event.IsMute = ed.IsMute;
                    event.IsDistanceReminderSet = ed.IsDistanceReminderSet;
                    ArrayList<EventParticipant> newMembers = new ArrayList<EventParticipant>();
                    ArrayList<EventParticipant> reminderMems = ed.ReminderEnabledMembers;
                    if (reminderMems != null && reminderMems.size() > 0) {
                        for (EventParticipant mem : reminderMems) {
                            EventParticipant newMem = event.getParticipant(mem.userId);
                            if (newMem != null) {
                                newMem.distanceReminderDistance = mem.distanceReminderDistance;
                                newMem.distanceReminderId = mem.distanceReminderId;
                                newMem.reminderFrom = mem.reminderFrom;
                                newMembers.add(newMem);
                            }
                        }
                        if (newMembers.size() > 0) {
                            event.ReminderEnabledMembers = newMembers;
                            event.IsDistanceReminderSet = true;
                        }
                    }
                }
                if (eventType == EventType.SHAREMYLOACTION || eventType == EventType.TRACKBUDDY) {
                    cachedEntriesForTE.put(eventId, event);
                } else {
                    cachedEntries.put(eventId, event);
                }
            }

            writeObject(CACHE_EVENTS, cachedEntries);
            writeObject(CACHE_TRACK_EVENTS, cachedEntriesForTE);
        }
    }

    @SuppressWarnings("unchecked")
    public static void saveContactListToCache(ArrayList<ContactOrGroup> contacts) {
        if (contacts != null) {
            writeObject(CACHE_CONTACTS, contacts);
        }
    }

    @SuppressWarnings("unchecked")
    public static void saveRegisteredContactListToCache(HashMap<String, ContactOrGroup> contacts) {
        if (contacts != null) {
            writeObject(CACHE_REGISTERED_CONTACTS, contacts);
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<ContactOrGroup> getContactListFromCache() {
        return (ArrayList<ContactOrGroup>) readObject(CACHE_CONTACTS);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, ContactOrGroup>  getRegisteredContactListFromCache() {

        return  (HashMap<String, ContactOrGroup>) readObject(CACHE_REGISTERED_CONTACTS);
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<EventPlace> getDestinationListFromCache() {
        ArrayList<EventPlace> cachedEntries = (ArrayList<EventPlace>) readObject(CACHE_DESTINATIONS);
        return cachedEntries;
    }

    public static void saveDestinationListToCache(ArrayList<EventPlace> locations) {
        if (locations != null) {
            writeObject(CACHE_DESTINATIONS, locations);
        }
    }
}

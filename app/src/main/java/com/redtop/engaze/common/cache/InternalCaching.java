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
        Hashtable<String, Event> cachedEvents = new Hashtable<String, Event>();
        writeObject(CACHE_EVENTS, cachedEvents);
        writeObject(CACHE_TRACK_EVENTS, cachedEvents);
        Hashtable<String, ContactOrGroup> contacts = new Hashtable<String, ContactOrGroup>();
        writeObject(CACHE_CONTACTS, contacts);
        writeObject(CACHE_REGISTERED_CONTACTS, contacts);
        ArrayList<EventPlace> cachedD = new ArrayList<EventPlace>();
        writeObject(CACHE_DESTINATIONS, cachedD);
    }

    @SuppressWarnings("unchecked")
    public static Event getEventFromCache(String eventId) {
        Event event = null;
        Hashtable<String, Event> cachedEntries = getCachedEventHashMap();
        event = cachedEntries.get(eventId);
        if (event == null) {
            cachedEntries = getCachedTrackEventHashMap();
            event = cachedEntries.get(eventId);
        }
        return event;
    }

    @SuppressWarnings("unchecked")
    public static List<Event> getEventListFromCache() {
        ArrayList<Event> events = null;
        Hashtable<String, Event> cachedEntries = (Hashtable<String, Event>) readObject(CACHE_EVENTS);
        if (cachedEntries != null && cachedEntries.size() != 0) {
            events = new ArrayList<Event>(cachedEntries.values());
        } else {
            events = new ArrayList<Event>();
        }
        return events;
    }

    @SuppressWarnings("unchecked")
    public static List<Event> getTrackEventListFromCache() {
        List<Event> events = null;
        Hashtable<String, Event> cachedEntries = (Hashtable<String, Event>) readObject(CACHE_TRACK_EVENTS);
        if (cachedEntries != null && cachedEntries.size() != 0) {
            events = new ArrayList<Event>(cachedEntries.values());
        } else {
            events = new ArrayList<Event>();
        }
        return events;
    }

    @SuppressWarnings("unchecked")
    private static Hashtable<String, Event> getCachedEventHashMap() {

        Hashtable<String, Event> cachedEntries = (Hashtable<String, Event>) readObject(CACHE_EVENTS);
        return cachedEntries;
    }

    @SuppressWarnings("unchecked")
    private static Hashtable<String, Event> getCachedTrackEventHashMap() {

        Hashtable<String, Event> cachedEntries = (Hashtable<String, Event>) readObject(CACHE_TRACK_EVENTS);
        return cachedEntries;
    }

    @SuppressWarnings("unchecked")
    public static void saveEventToCache(Event event) {
        Hashtable<String, Event> cachedEntries;
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
        Hashtable<String, Event> cachedEntries = getCachedEventHashMap();
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
        Hashtable<String, Event> cachedEntries = getCachedEventHashMap();
        Hashtable<String, Event> cachedEntriesForTE = getCachedTrackEventHashMap();
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

    public static void RemovePastEvents() {

        List<Event> eventList = getEventListFromCache();
        if (eventList != null) {
            List<String> tobeRemoved = new ArrayList<String>();
            for (Event event : eventList) {
                if (EventService.isEventPast(event)) {
                    tobeRemoved.add(event.eventId);
                }
            }
            if (tobeRemoved.size() < 0) {
                removeEventsFromCache(tobeRemoved);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void saveEventListToCache(List<Event> events) {
        if (events != null && events.size() > 0) {
            Event ed;
            String eventId;
            Hashtable<String, Event> oldcachedEntries = getCachedEventHashMap();
            Hashtable<String, Event> oldcachedEntriesForTE = getCachedTrackEventHashMap();
            Hashtable<String, Event> cachedEntries = new Hashtable<String, Event>();
            Hashtable<String, Event> cachedEntriesForTE = new Hashtable<String, Event>();
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
                    for (UsersLocationDetail ud : ed.UsersLocationDetailList) {
                        event.UsersLocationDetailList.add(
                                AppContext.jsonParser.deserialize(AppContext.jsonParser.Serialize(ud), UsersLocationDetail.class));
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
    public static void saveContactListToCache(HashMap<String, ContactOrGroup> contacts) {
        if (contacts != null) {
            writeObject(CACHE_CONTACTS, contacts);
        }
    }

    @SuppressWarnings("unchecked")
    public static void saveRegisteredContactListToCache(Hashtable<String, ContactOrGroup> contacts) {
        if (contacts != null) {
            writeObject(CACHE_REGISTERED_CONTACTS, contacts);
        }
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, ContactOrGroup> getContactListFromCache() {
        HashMap<String, ContactOrGroup> cachedEntries = (HashMap<String, ContactOrGroup>) readObject(CACHE_CONTACTS);
        return cachedEntries;
    }

    @SuppressWarnings("unchecked")
    public static Hashtable<String, ContactOrGroup> getRegisteredContactListFromCache() {
        try {
            Hashtable<String, ContactOrGroup> cachedEntries = (Hashtable<String, ContactOrGroup>) readObject(CACHE_REGISTERED_CONTACTS);
            return cachedEntries;
        } catch (ClassCastException ex) {
            Hashtable<String, ContactOrGroup> cachedEntries = new Hashtable<String, ContactOrGroup>();
            ArrayList<ContactOrGroup> CacheArray = (ArrayList<ContactOrGroup>) readObject(CACHE_REGISTERED_CONTACTS);
            for (ContactOrGroup cg : CacheArray) {
                cachedEntries.put(cg.getUserId(), cg);
            }
            writeObject(CACHE_REGISTERED_CONTACTS, cachedEntries);
            return cachedEntries;
        }
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

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
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.domain.EventDetail;
import com.redtop.engaze.domain.EventParticipant;
import com.redtop.engaze.domain.EventPlace;
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
        Hashtable<String, EventDetail> cachedEvents = new Hashtable<String, EventDetail>();
        writeObject(CACHE_EVENTS, cachedEvents);
        writeObject(CACHE_TRACK_EVENTS, cachedEvents);
        Hashtable<String, ContactOrGroup> contacts = new Hashtable<String, ContactOrGroup>();
        writeObject(CACHE_CONTACTS, contacts);
        writeObject(CACHE_REGISTERED_CONTACTS, contacts);
        ArrayList<EventPlace> cachedD = new ArrayList<EventPlace>();
        writeObject(CACHE_DESTINATIONS, cachedD);
    }

    @SuppressWarnings("unchecked")
    public static EventDetail getEventFromCache(String eventId) {
        EventDetail event = null;
        Hashtable<String, EventDetail> cachedEntries = getcachedEventsHashMap();
        event = cachedEntries.get(eventId);
        if (event == null) {
            cachedEntries = getcachedTrackEventsHashMap();
            event = cachedEntries.get(eventId);
        }
        return event;
    }

    @SuppressWarnings("unchecked")
    public static List<EventDetail> getEventListFromCache() {
        ArrayList<EventDetail> events = null;
        Hashtable<String, EventDetail> cachedEntries = (Hashtable<String, EventDetail>) readObject(CACHE_EVENTS);
        if (cachedEntries != null && cachedEntries.size() != 0) {
            events = new ArrayList<EventDetail>(cachedEntries.values());
        } else {
            events = new ArrayList<EventDetail>();
        }
        return events;
    }

    @SuppressWarnings("unchecked")
    public static List<EventDetail> getTrackEventListFromCache() {
        List<EventDetail> events = null;
        Hashtable<String, EventDetail> cachedEntries = (Hashtable<String, EventDetail>) readObject(CACHE_TRACK_EVENTS);
        if (cachedEntries != null && cachedEntries.size() != 0) {
            events = new ArrayList<EventDetail>(cachedEntries.values());
        } else {
            events = new ArrayList<EventDetail>();
        }
        return events;
    }

    @SuppressWarnings("unchecked")
    private static Hashtable<String, EventDetail> getcachedEventsHashMap() {

        Hashtable<String, EventDetail> cachedEntries = (Hashtable<String, EventDetail>) readObject(CACHE_EVENTS);
        return cachedEntries;
    }

    @SuppressWarnings("unchecked")
    private static Hashtable<String, EventDetail> getcachedTrackEventsHashMap() {

        Hashtable<String, EventDetail> cachedEntries = (Hashtable<String, EventDetail>) readObject(CACHE_TRACK_EVENTS);
        return cachedEntries;
    }

    @SuppressWarnings("unchecked")
    public static void saveEventToCache(EventDetail event) {
        Hashtable<String, EventDetail> cachedEntries;
        String eventTypeId = event.getEventTypeId();
        if (eventTypeId.equals("100") || eventTypeId.equals("200")) {
            cachedEntries = getcachedTrackEventsHashMap();
            cachedEntries.put(event.getEventId(), event);
            writeObject(CACHE_TRACK_EVENTS, cachedEntries);
        } else {
            cachedEntries = getcachedEventsHashMap();
            cachedEntries.put(event.getEventId(), event);
            writeObject(CACHE_EVENTS, cachedEntries);
        }
    }

    public static void removeEventFromCache(String eventId) {
        Hashtable<String, EventDetail> cachedEntries = getcachedEventsHashMap();
        if (cachedEntries.containsKey(eventId)) {
            cachedEntries.remove(eventId);
            writeObject(CACHE_EVENTS, cachedEntries);
        } else {
            cachedEntries = getcachedTrackEventsHashMap();
            if (cachedEntries.containsKey(eventId)) {
                cachedEntries.remove(eventId);
                writeObject(CACHE_TRACK_EVENTS, cachedEntries);
            }
        }
    }

    public static void removeEventsFromCache(List<String> eventIdList) {
        Hashtable<String, EventDetail> cachedEntries = getcachedEventsHashMap();
        Hashtable<String, EventDetail> cachedEntriesForTE = getcachedTrackEventsHashMap();
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

        List<EventDetail> eventDetailList = getEventListFromCache();
        if (eventDetailList != null) {
            List<String> tobeRemoved = new ArrayList<String>();
            for (EventDetail event : eventDetailList) {
                if (EventService.isEventPast(event)) {
                    tobeRemoved.add(event.getEventId());
                }
            }
            if (tobeRemoved.size() < 0) {
                removeEventsFromCache(tobeRemoved);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void saveEventListToCache(List<EventDetail> events) {
        if (events != null && events.size() > 0) {
            EventDetail ed;
            String eventId;
            Hashtable<String, EventDetail> oldcachedEntries = getcachedEventsHashMap();
            Hashtable<String, EventDetail> oldcachedEntriesForTE = getcachedTrackEventsHashMap();
            Hashtable<String, EventDetail> cachedEntries = new Hashtable<String, EventDetail>();
            Hashtable<String, EventDetail> cachedEntriesForTE = new Hashtable<String, EventDetail>();
            int eventTypeId;
            for (EventDetail event : events) {
                eventId = event.getEventId();
                eventTypeId = Integer.parseInt(event.getEventTypeId());
                if (eventTypeId == 100 || eventTypeId == 200) {
                    ed = oldcachedEntriesForTE.get(eventId);
                } else {
                    ed = oldcachedEntries.get(eventId);
                }
                if (ed != null) {
                    event.setUsersLocationDetailList(ed.getUsersLocationDetailList());
                    event.acceptNotificationid = ed.acceptNotificationid;
                    event.snoozeNotificationId = ed.snoozeNotificationId;
                    event.notificationIds = ed.notificationIds;
                    event.isMute = ed.isMute;
                    event.isDistanceReminderSet = ed.isDistanceReminderSet;
                    ArrayList<EventParticipant> newMembers = new ArrayList<EventParticipant>();
                    ArrayList<EventParticipant> reminderMems = ed.getReminderEnabledMembers();
                    if (reminderMems != null && reminderMems.size() > 0) {
                        for (EventParticipant mem : reminderMems) {
                            EventParticipant newMem = event.getMember(mem.getUserId());
                            if (newMem != null) {
                                newMem.setDistanceReminderDistance(mem.getDistanceReminderDistance());
                                newMem.setDistanceReminderId(mem.getDistanceReminderId());
                                newMem.setReminderFrom(mem.getReminderFrom());
                                newMembers.add(newMem);
                            }
                        }
                        if (newMembers.size() > 0) {
                            event.setReminderEnabledMembers(newMembers);
                            event.isDistanceReminderSet = true;
                        }
                    }
                }
                if (eventTypeId == 100 || eventTypeId == 200) {
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

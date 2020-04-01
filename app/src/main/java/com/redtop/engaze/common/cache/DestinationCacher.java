package com.redtop.engaze.common.cache;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.location.Location;

import com.redtop.engaze.common.constant.Constants;
import com.redtop.engaze.domain.EventPlace;

public class DestinationCacher {

    private final static String TAG = DestinationCacher.class.getName();

    public static void cacheDestination(EventPlace mDestinationPlace, Context context) {
        Integer maxDestinationCount = Constants.MAX_DESTINATION_CACHE_COUNT;
        if (maxDestinationCount == 0) {
            return;
        }
        //Queue<EventPlace>
        ArrayList<EventPlace> cachedEntries = InternalCaching.getDestinationListFromCache();
        if (!isLocationExistIncache(mDestinationPlace, cachedEntries)) {
            int arraySize = cachedEntries.size();
            while (arraySize >= maxDestinationCount) {
                cachedEntries.remove(0);
                arraySize = arraySize - 1;
            }
            cachedEntries.add(mDestinationPlace);
            InternalCaching.saveDestinationListToCache(cachedEntries);
        }
    }

    public static ArrayList<EventPlace> getDestinationsFromCache(Context context) {
        ArrayList<EventPlace> eventPlaces = InternalCaching.getDestinationListFromCache();

        for (EventPlace ep : eventPlaces) {
            ep.createLatLangFromLatLangField();
        }
        Collections.reverse(eventPlaces);
        return eventPlaces;
    }

    private static boolean isLocationExistIncache(EventPlace place, ArrayList<EventPlace> cachedEntries) {
        boolean isExist = false;
        Location newLoc = new Location("");
        Location existingLoc = new Location("");
        newLoc.setLatitude(place.getLatLang().latitude);//your coords of course
        newLoc.setLongitude(place.getLatLang().longitude);

        for (EventPlace ep : cachedEntries) {
            existingLoc.setLatitude(ep.getLatitude());//your coords of course
            existingLoc.setLongitude(ep.getLongitude());
            if (newLoc.distanceTo(existingLoc) <= Constants.DESTINATION_RADIUS) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }
}

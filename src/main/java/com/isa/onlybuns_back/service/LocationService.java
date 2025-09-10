package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.model.Address;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocationService {
    private final Map<String, Address> locationCache = new ConcurrentHashMap<>();

    // Pomogne da se napravi ključ za keš na osnovu koordinata
    private String getKey(Address address) {
        double roundedLat = Math.round(address.getLatitude() * 1000.0) / 1000.0;
        double roundedLng = Math.round(address.getLongitude() * 1000.0) / 1000.0;
        return roundedLat + ":" + roundedLng;
    }

    // Metoda koja vraća iz keša ili stavlja ako ne postoji
    public Address cachePostLocation(Address address) {
        String key = getKey(address);

        if (locationCache.containsKey(key)) {
            System.out.println("[CACHE HIT] Postoji keš za lokaciju " + key);
            System.out.println("[CACHE] Veličina keša: " + locationCache.size());
            System.out.println("[CACHE] Ključevi: " + locationCache.keySet());
            return locationCache.get(key);
        } else {
            locationCache.put(key, address);
            System.out.println("[CACHE MISS] Dodata nova lokacija u keš: " + key);
            System.out.println("[CACHE] Veličina keša: " + locationCache.size());
            System.out.println("[CACHE] Ključevi: " + locationCache.keySet());
            return address;
        }
    }
}
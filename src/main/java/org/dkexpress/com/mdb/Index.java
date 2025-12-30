package org.dkexpress.com.mdb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Index {
    private static final Map<String, Location> index = new ConcurrentHashMap<>();

    public void put(String key, Location location) {
        index.put(key, location);
    }
    public Location get(String key) {
        return index.get(key);
    }
}

package com.paradoxwebsolutions.core;

import java.util.HashMap;
import java.util.Map;


/**
 * Utility map class.
 *
 * @author Peter Smith
 */
public class StringMap extends HashMap<String, String> {

    /**
     * Sets a name/value pair in this map.
     * @param key the key to store the value under
     * @param value the value to store in the map
     * @return a reference to this map
     */
    public StringMap set(String key, String value) {
        put(key, value);
        return this;
    }
    
}

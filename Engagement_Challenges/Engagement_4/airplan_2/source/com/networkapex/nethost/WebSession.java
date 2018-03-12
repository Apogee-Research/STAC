package com.networkapex.nethost;

import java.util.HashMap;
import java.util.Map;

/**
 * Helps keep track of the current user.s
 */
public class WebSession {
    private final String personId;
    private final Map<String, String> propertyMap = new HashMap<>();

    public WebSession(String personId) {
        this.personId = personId;
    }

    public String getPersonId() {
        return personId;
    }

    public String getProperty(String name) {
        return propertyMap.get(name);
    }

    public String obtainProperty(String name, String defaultReturn) {
        if (propertyMap.containsKey(name)) {
            return propertyMap.get(name);
        } else {
            return defaultReturn;
        }
    }

    public void assignProperty(String name, String value) {
        propertyMap.put(name, value);
    }
}

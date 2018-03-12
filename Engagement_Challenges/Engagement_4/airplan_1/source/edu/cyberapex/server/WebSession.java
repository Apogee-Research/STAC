package edu.cyberapex.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Helps keep track of the current user.s
 */
public class WebSession {
    private final String memberId;
    private final Map<String, String> propertyMap = new HashMap<>();

    public WebSession(String memberId) {
        this.memberId = memberId;
    }

    public String grabMemberId() {
        return memberId;
    }

    public String getProperty(String name) {
        return propertyMap.get(name);
    }

    public String fetchProperty(String name, String defaultReturn) {
        if (propertyMap.containsKey(name)) {
            return propertyMap.get(name);
        } else {
            return defaultReturn;
        }
    }

    public void defineProperty(String name, String value) {
        propertyMap.put(name, value);
    }
}

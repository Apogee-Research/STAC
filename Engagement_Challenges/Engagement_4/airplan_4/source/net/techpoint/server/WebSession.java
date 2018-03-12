package net.techpoint.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Helps keep track of the current user.s
 */
public class WebSession {
    private final String userId;
    private final Map<String, String> propertyMap = new HashMap<>();

    public WebSession(String userId) {
        this.userId = userId;
    }

    public String takeUserId() {
        return userId;
    }

    public String grabProperty(String name) {
        return propertyMap.get(name);
    }

    public String grabProperty(String name, String defaultReturn) {
        if (propertyMap.containsKey(name)) {
            return propertyMap.get(name);
        } else {
            return defaultReturn;
        }
    }

    public void fixProperty(String name, String value) {
        propertyMap.put(name, value);
    }
}

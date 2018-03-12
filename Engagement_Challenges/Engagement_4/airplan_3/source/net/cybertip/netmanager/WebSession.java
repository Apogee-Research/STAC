package net.cybertip.netmanager;

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

    public String obtainMemberId() {
        return memberId;
    }

    public String pullProperty(String name) {
        return propertyMap.get(name);
    }

    public String obtainProperty(String name, String defaultReturn) {
        if (propertyMap.containsKey(name)) {
            return propertyMap.get(name);
        } else {
            return defaultReturn;
        }
    }

    public void setProperty(String name, String value) {
        propertyMap.put(name, value);
    }
}
